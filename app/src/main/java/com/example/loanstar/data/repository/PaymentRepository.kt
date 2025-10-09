package com.example.loanstar.data.repository

import com.example.loanstar.data.local.dao.PaymentDao
import com.example.loanstar.data.local.entities.PaymentEntity
import com.example.loanstar.data.local.entities.toDomain
import com.example.loanstar.data.local.entities.toEntity
import com.example.loanstar.data.model.Payment
import com.example.loanstar.data.model.PaymentStatus
import com.example.loanstar.data.model.PaymentType
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
 * Repository for payment operations
 */
@Singleton
class PaymentRepository @Inject constructor(
    private val supabaseClient: SupabaseClientProvider,
    private val paymentDao: PaymentDao,
    private val loanRepository: LoanRepository,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository
) {

    fun getPaymentsByLoanFlow(loanId: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsByLoanFlow(loanId).map { it.map { entity -> entity.toDomain() } }
    }

    suspend fun recordPayment(
        loanId: String,
        amount: Double,
        type: PaymentType = PaymentType.REGULAR,
        notes: String? = null
    ): Result<Payment> = safeCall {
        val currentUserId = authRepository.getCurrentUserId()
            ?: throw Exception("User not authenticated")

        val payment = Payment(
            id = java.util.UUID.randomUUID().toString(),
            loanId = loanId,
            amount = amount,
            paymentDate = System.currentTimeMillis(),
            type = type,
            status = PaymentStatus.RECEIVED,
            receivedByAgentId = currentUserId,
            notes = notes,
            appliedToPrincipal = amount * 0.7, // Simplified - should calculate properly
            appliedToInterest = amount * 0.3,
            appliedToPenalty = 0.0
        )

        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            supabaseClient.postgrest.from("payments").insert(payment.toEntity())
            logTransaction("RECEIVE_PAYMENT", payment.id, "Payment of $amount recorded for loan $loanId")
        }

        paymentDao.insertPayment(payment.toEntity())

        // Update loan balance
        loanRepository.updateLoanBalance(loanId, amount)

        payment
    }

    suspend fun getPaymentsByDateRange(startDate: Long, endDate: Long): Result<List<Payment>> = safeCall {
        paymentDao.getPaymentsByDateRange(startDate, endDate).map { it.toDomain() }
    }

    suspend fun getTotalPaidForLoan(loanId: String): Result<Double> = safeCall {
        paymentDao.getTotalPaidForLoan(loanId) ?: 0.0
    }

    private suspend fun logTransaction(type: String, entityId: String, details: String) {
        try {
            val userId = authRepository.getCurrentUserId() ?: return
            val transaction = mapOf(
                "type" to type,
                "user_id" to userId,
                "details" to details,
                "related_entity_id" to entityId,
                "related_entity_type" to "payment",
                "timestamp" to System.currentTimeMillis()
            )
            supabaseClient.postgrest.from("transactions").insert(transaction)
        } catch (e: Exception) {
            android.util.Log.e("loanstar", "Error logging transaction in PaymentRepository: ${e.message}", e)
            e.printStackTrace()
        }
    }
}
