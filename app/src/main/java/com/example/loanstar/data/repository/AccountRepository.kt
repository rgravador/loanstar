package com.example.loanstar.data.repository

import com.example.loanstar.data.local.dao.AccountDao
import com.example.loanstar.data.local.entities.AccountEntity
import com.example.loanstar.data.local.entities.toDomain
import com.example.loanstar.data.local.entities.toEntity
import com.example.loanstar.data.model.Account
import com.example.loanstar.data.model.AccountStatus
import com.example.loanstar.data.remote.SupabaseClientProvider
import com.example.loanstar.util.NetworkMonitor
import com.example.loanstar.util.Result
import com.example.loanstar.util.safeCall
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for account (borrower) operations
 * Supports offline-first architecture with automatic sync
 */
@Singleton
class AccountRepository @Inject constructor(
    private val supabaseClient: SupabaseClientProvider,
    private val accountDao: AccountDao,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository
) {

    /**
     * Get accounts by agent ID with Flow
     */
    fun getAccountsByAgentFlow(agentId: String): Flow<List<Account>> {
        return accountDao.getAccountsByAgentFlow(agentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get account by ID with Flow
     */
    fun getAccountByIdFlow(accountId: String): Flow<Account?> {
        return accountDao.getAccountByIdFlow(accountId).map { it?.toDomain() }
    }

    /**
     * Create new account
     */
    suspend fun createAccount(account: Account): Result<Account> = safeCall {
        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            // Create in Supabase
            supabaseClient.postgrest
                .from("accounts")
                .insert(account.toEntity())

            // Log transaction
            logTransaction("CREATE_ACCOUNT", account.id, "Created account: ${account.name}")
        }

        // Save locally (immediate for offline, cache for online)
        accountDao.insertAccount(account.toEntity())

        account
    }

    /**
     * Update account
     */
    suspend fun updateAccount(account: Account): Result<Account> = safeCall {
        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            // Update in Supabase
            supabaseClient.postgrest
                .from("accounts")
                .update(account.toEntity()) {
                    filter {
                        eq("id", account.id)
                    }
                }

            // Log transaction
            logTransaction("UPDATE_ACCOUNT", account.id, "Updated account: ${account.name}")
        }

        // Update locally
        accountDao.updateAccount(account.toEntity())

        account
    }

    /**
     * Delete account
     */
    suspend fun deleteAccount(accountId: String): Result<Unit> = safeCall {
        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            // Delete from Supabase
            supabaseClient.postgrest
                .from("accounts")
                .delete {
                    filter {
                        eq("id", accountId)
                    }
                }
        }

        // Delete locally
        accountDao.deleteAccountById(accountId)
    }

    /**
     * Get account by ID
     */
    suspend fun getAccountById(accountId: String): Result<Account> = safeCall {
        // Try local cache first
        val cachedAccount = accountDao.getAccountById(accountId)
        if (cachedAccount != null) {
            return@safeCall cachedAccount.toDomain()
        }

        // Fetch from Supabase if online
        val isOnline = networkMonitor.isConnected.first()
        if (isOnline) {
            val accountEntity = supabaseClient.postgrest
                .from("accounts")
                .select {
                    filter {
                        eq("id", accountId)
                    }
                }
                .decodeSingle<AccountEntity>()

            // Cache locally
            accountDao.insertAccount(accountEntity)

            return@safeCall accountEntity.toDomain()
        }

        throw Exception("Account not found in local cache and no network connection")
    }

    /**
     * Get accounts by agent
     */
    suspend fun getAccountsByAgent(agentId: String, forceRefresh: Boolean = false): Result<List<Account>> = safeCall {
        val isOnline = networkMonitor.isConnected.first()

        if (isOnline && forceRefresh) {
            // Fetch from Supabase
            val accounts = supabaseClient.postgrest
                .from("accounts")
                .select {
                    filter {
                        eq("assigned_agent_id", agentId)
                    }
                }
                .decodeList<AccountEntity>()

            // Update local cache
            accountDao.insertAccounts(accounts)

            return@safeCall accounts.map { it.toDomain() }
        }

        // Return from local cache
        accountDao.getAccountsByAgent(agentId).map { it.toDomain() }
    }

    /**
     * Get accounts by status
     */
    suspend fun getAccountsByStatus(status: AccountStatus): Result<List<Account>> = safeCall {
        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            // Fetch from Supabase
            val accounts = supabaseClient.postgrest
                .from("accounts")
                .select {
                    filter {
                        eq("status", status.name)
                    }
                }
                .decodeList<AccountEntity>()

            // Update local cache
            accountDao.insertAccounts(accounts)

            return@safeCall accounts.map { it.toDomain() }
        }

        // Return from local cache
        accountDao.getAccountsByStatus(status.name).map { it.toDomain() }
    }

    /**
     * Search accounts by name
     */
    suspend fun searchAccountsByName(query: String): Result<List<Account>> = safeCall {
        // Search in local cache
        accountDao.searchAccountsByName(query).map { it.toDomain() }
    }

    /**
     * Get all accounts (admin only)
     */
    suspend fun getAllAccounts(forceRefresh: Boolean = false): Result<List<Account>> = safeCall {
        val isOnline = networkMonitor.isConnected.first()

        if (isOnline && forceRefresh) {
            // Fetch all from Supabase
            val accounts = supabaseClient.postgrest
                .from("accounts")
                .select()
                .decodeList<AccountEntity>()

            // Update local cache
            accountDao.insertAccounts(accounts)

            return@safeCall accounts.map { it.toDomain() }
        }

        // Return from local cache
        accountDao.getAllAccounts().map { it.toDomain() }
    }

    /**
     * Sync accounts from remote
     */
    suspend fun syncAccounts(agentId: String? = null): Result<Unit> = safeCall {
        val isOnline = networkMonitor.isConnected.first()
        if (!isOnline) {
            throw Exception("No network connection")
        }

        if (agentId != null) {
            // Sync specific agent's accounts
            val accounts = supabaseClient.postgrest
                .from("accounts")
                .select {
                    filter {
                        eq("assigned_agent_id", agentId)
                    }
                }
                .decodeList<AccountEntity>()

            accountDao.insertAccounts(accounts)
        } else {
            // Sync all accounts
            val accounts = supabaseClient.postgrest
                .from("accounts")
                .select()
                .decodeList<AccountEntity>()

            accountDao.insertAccounts(accounts)
        }
    }

    /**
     * Log transaction for audit trail
     */
    private suspend fun logTransaction(type: String, entityId: String, details: String) {
        try {
            val userId = authRepository.getCurrentUserId() ?: return
            val transaction = mapOf(
                "type" to type,
                "user_id" to userId,
                "details" to details,
                "related_entity_id" to entityId,
                "related_entity_type" to "account",
                "timestamp" to System.currentTimeMillis()
            )
            supabaseClient.postgrest.from("transactions").insert(transaction)
        } catch (e: Exception) {
            android.util.Log.e("loanstar", "Error logging transaction in AccountRepository: ${e.message}", e)
            e.printStackTrace()
        }
    }
}
