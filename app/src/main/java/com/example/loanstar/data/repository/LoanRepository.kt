package com.example.loanstar.data.repository

import com.example.loanstar.data.local.dao.LoanDao
import com.example.loanstar.data.local.entities.LoanEntity
import com.example.loanstar.data.local.entities.toDomain
import com.example.loanstar.data.local.entities.toEntity
import com.example.loanstar.data.model.Loan
import com.example.loanstar.data.model.LoanStatus
import com.example.loanstar.data.model.PaymentFrequency
import com.example.loanstar.data.remote.SupabaseClientProvider
import com.example.loanstar.util.LoanCalculator
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
 * Repository for loan operations
 * Handles loan creation, approval, amortization, and payments
 */
@Singleton
class LoanRepository @Inject constructor(
    private val supabaseClient: SupabaseClientProvider,
    private val loanDao: LoanDao,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository
) {

    /**
     * Get loan by ID with Flow
     */
    fun getLoanByIdFlow(loanId: String): Flow<Loan?> {
        return loanDao.getLoanByIdFlow(loanId).map { it?.toDomain() }
    }

    /**
     * Get loans by account with Flow
     */
    fun getLoansByAccountFlow(accountId: String): Flow<List<Loan>> {
        return loanDao.getLoansByAccountFlow(accountId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get loans by agent with Flow
     */
    fun getLoansByAgentFlow(agentId: String): Flow<List<Loan>> {
        return loanDao.getLoansByAgentFlow(agentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get loans by status with Flow
     */
    fun getLoansByStatusFlow(status: LoanStatus): Flow<List<Loan>> {
        return loanDao.getLoansByStatusFlow(status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Create new loan
     */
    suspend fun createLoan(
        accountId: String,
        principalAmount: Double,
        interestRate: Double,
        tenureMonths: Int,
        paymentFrequency: PaymentFrequency
    ): Result<Loan> = safeCall {
        // Validate loan parameters
        val errors = LoanCalculator.validateLoanParameters(principalAmount, interestRate, tenureMonths)
        if (errors.isNotEmpty()) {
            throw Exception("Invalid loan parameters: ${errors.joinToString(", ")}")
        }

        val currentUserId = authRepository.getCurrentUserId()
            ?: throw Exception("User not authenticated")

        // Generate amortization schedule
        val amortizationSchedule = LoanCalculator.generateAmortizationSchedule(
            principal = principalAmount,
            monthlyInterestRate = interestRate,
            tenureMonths = tenureMonths,
            paymentFrequency = paymentFrequency,
            startDate = System.currentTimeMillis()
        )

        val loan = Loan(
            id = java.util.UUID.randomUUID().toString(),
            accountId = accountId,
            principalAmount = principalAmount,
            interestRate = interestRate,
            tenureMonths = tenureMonths,
            paymentFrequency = paymentFrequency,
            status = LoanStatus.PENDING_APPROVAL,
            amortizationSchedule = amortizationSchedule,
            createdByAgentId = currentUserId,
            outstandingBalance = principalAmount,
            totalPaid = 0.0
        )

        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            // Create in Supabase
            supabaseClient.postgrest
                .from("loans")
                .insert(loan.toEntity())

            // Log transaction
            logTransaction("CREATE_LOAN", loan.id, "Created loan for account $accountId with principal $principalAmount")
        }

        // Save locally
        loanDao.insertLoan(loan.toEntity())

        loan
    }

    /**
     * Approve loan (admin only)
     */
    suspend fun approveLoan(loanId: String): Result<Loan> = safeCall {
        val currentUserId = authRepository.getCurrentUserId()
            ?: throw Exception("User not authenticated")

        val loan = loanDao.getLoanById(loanId)?.toDomain()
            ?: throw Exception("Loan not found")

        val updatedLoan = loan.copy(
            status = LoanStatus.ACTIVE,
            approvalDate = System.currentTimeMillis(),
            approvedByAdminId = currentUserId
        )

        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            // Update in Supabase
            supabaseClient.postgrest
                .from("loans")
                .update(updatedLoan.toEntity()) {
                    filter {
                        eq("id", loanId)
                    }
                }

            // Log transaction
            logTransaction("APPROVE_LOAN", loanId, "Loan approved by admin")

            // TODO: Send notification to agent
        }

        // Update locally
        loanDao.updateLoan(updatedLoan.toEntity())

        updatedLoan
    }

    /**
     * Reject loan (admin only)
     */
    suspend fun rejectLoan(loanId: String, reason: String): Result<Loan> = safeCall {
        val loan = loanDao.getLoanById(loanId)?.toDomain()
            ?: throw Exception("Loan not found")

        val updatedLoan = loan.copy(
            status = LoanStatus.REJECTED,
            rejectionReason = reason
        )

        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            // Update in Supabase
            supabaseClient.postgrest
                .from("loans")
                .update(updatedLoan.toEntity()) {
                    filter {
                        eq("id", loanId)
                    }
                }

            // Log transaction
            logTransaction("REJECT_LOAN", loanId, "Loan rejected: $reason")

            // TODO: Send notification to agent
        }

        // Update locally
        loanDao.updateLoan(updatedLoan.toEntity())

        updatedLoan
    }

    /**
     * Update loan balance after payment
     */
    suspend fun updateLoanBalance(loanId: String, paidAmount: Double): Result<Loan> = safeCall {
        val loan = loanDao.getLoanById(loanId)?.toDomain()
            ?: throw Exception("Loan not found")

        val updatedLoan = loan.copy(
            outstandingBalance = maxOf(0.0, loan.outstandingBalance - paidAmount),
            totalPaid = loan.totalPaid + paidAmount,
            status = if (loan.outstandingBalance - paidAmount <= 0) LoanStatus.CLOSED else loan.status
        )

        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            // Update in Supabase
            supabaseClient.postgrest
                .from("loans")
                .update(updatedLoan.toEntity()) {
                    filter {
                        eq("id", loanId)
                    }
                }
        }

        // Update locally
        loanDao.updateLoan(updatedLoan.toEntity())

        updatedLoan
    }

    /**
     * Get loans by agent and status
     */
    suspend fun getLoansByAgentAndStatus(agentId: String, status: LoanStatus): Result<List<Loan>> = safeCall {
        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            // Fetch from Supabase
            val loans = supabaseClient.postgrest
                .from("loans")
                .select {
                    filter {
                        eq("created_by_agent_id", agentId)
                        eq("status", status.name)
                    }
                }
                .decodeList<LoanEntity>()

            // Update local cache
            loanDao.insertLoans(loans)

            return@safeCall loans.map { it.toDomain() }
        }

        // Return from local cache
        loanDao.getLoansByAgentAndStatus(agentId, status.name).map { it.toDomain() }
    }

    /**
     * Get total outstanding balance
     */
    suspend fun getTotalOutstandingBalance(): Result<Double> = safeCall {
        loanDao.getTotalOutstandingBalance() ?: 0.0
    }

    /**
     * Get loan count by status
     */
    suspend fun getLoanCountByStatus(status: LoanStatus): Result<Int> = safeCall {
        loanDao.getLoanCountByStatus(status.name)
    }

    /**
     * Sync loans from remote
     */
    suspend fun syncLoans(agentId: String? = null): Result<Unit> = safeCall {
        val isOnline = networkMonitor.isConnected.first()
        if (!isOnline) {
            throw Exception("No network connection")
        }

        if (agentId != null) {
            // Sync specific agent's loans
            val loans = supabaseClient.postgrest
                .from("loans")
                .select {
                    filter {
                        eq("created_by_agent_id", agentId)
                    }
                }
                .decodeList<LoanEntity>()

            loanDao.insertLoans(loans)
        } else {
            // Sync all loans
            val loans = supabaseClient.postgrest
                .from("loans")
                .select()
                .decodeList<LoanEntity>()

            loanDao.insertLoans(loans)
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
                "related_entity_type" to "loan",
                "timestamp" to System.currentTimeMillis()
            )
            supabaseClient.postgrest.from("transactions").insert(transaction)
        } catch (e: Exception) {
            android.util.Log.e("loanstar", "Error logging transaction in LoanRepository: ${e.message}", e)
            e.printStackTrace()
        }
    }
}
