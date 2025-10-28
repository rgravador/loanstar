package com.example.loanstar.data.local.dao

import androidx.room.*
import com.example.loanstar.data.local.entities.AccountEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Account entities
 */
@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    fun getAccountByIdFlow(accountId: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE assignedAgentId = :agentId")
    suspend fun getAccountsByAgent(agentId: String): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE assignedAgentId = :agentId")
    fun getAccountsByAgentFlow(agentId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE status = :status")
    suspend fun getAccountsByStatus(status: String): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE status = :status")
    fun getAccountsByStatusFlow(status: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE assignedAgentId = :agentId AND status = :status")
    suspend fun getAccountsByAgentAndStatus(agentId: String, status: String): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE name LIKE '%' || :searchQuery || '%'")
    suspend fun searchAccountsByName(searchQuery: String): List<AccountEntity>

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: String)

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()
}
