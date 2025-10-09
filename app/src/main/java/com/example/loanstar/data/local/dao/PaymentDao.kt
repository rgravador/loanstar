package com.example.loanstar.data.local.dao

import androidx.room.*
import com.example.loanstar.data.local.entities.PaymentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Payment entities
 */
@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE id = :paymentId")
    suspend fun getPaymentById(paymentId: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE id = :paymentId")
    fun getPaymentByIdFlow(paymentId: String): Flow<PaymentEntity?>

    @Query("SELECT * FROM payments WHERE loanId = :loanId ORDER BY paymentDate DESC")
    suspend fun getPaymentsByLoan(loanId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE loanId = :loanId ORDER BY paymentDate DESC")
    fun getPaymentsByLoanFlow(loanId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE receivedByAgentId = :agentId ORDER BY paymentDate DESC")
    suspend fun getPaymentsByAgent(agentId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE receivedByAgentId = :agentId ORDER BY paymentDate DESC")
    fun getPaymentsByAgentFlow(agentId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE status = :status ORDER BY paymentDate DESC")
    suspend fun getPaymentsByStatus(status: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE type = :type ORDER BY paymentDate DESC")
    suspend fun getPaymentsByType(type: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE paymentDate BETWEEN :startDate AND :endDate ORDER BY paymentDate DESC")
    suspend fun getPaymentsByDateRange(startDate: Long, endDate: Long): List<PaymentEntity>

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    suspend fun getAllPayments(): List<PaymentEntity>

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :paymentId")
    suspend fun deletePaymentById(paymentId: String)

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()

    @Query("SELECT SUM(amount) FROM payments WHERE loanId = :loanId AND status = 'RECEIVED'")
    suspend fun getTotalPaidForLoan(loanId: String): Double?

    @Query("SELECT SUM(appliedToInterest) FROM payments WHERE loanId = :loanId AND status = 'RECEIVED'")
    suspend fun getTotalInterestPaidForLoan(loanId: String): Double?
}
