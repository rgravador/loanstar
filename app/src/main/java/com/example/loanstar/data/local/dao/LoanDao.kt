package com.example.loanstar.data.local.dao

import androidx.room.*
import com.example.loanstar.data.local.entities.LoanEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Loan entities
 */
@Dao
interface LoanDao {

    @Query("SELECT * FROM loans WHERE id = :loanId")
    suspend fun getLoanById(loanId: String): LoanEntity?

    @Query("SELECT * FROM loans WHERE id = :loanId")
    fun getLoanByIdFlow(loanId: String): Flow<LoanEntity?>

    @Query("SELECT * FROM loans WHERE accountId = :accountId")
    suspend fun getLoansByAccount(accountId: String): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE accountId = :accountId")
    fun getLoansByAccountFlow(accountId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE status = :status")
    suspend fun getLoansByStatus(status: String): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE status = :status")
    fun getLoansByStatusFlow(status: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE createdByAgentId = :agentId")
    suspend fun getLoansByAgent(agentId: String): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE createdByAgentId = :agentId")
    fun getLoansByAgentFlow(agentId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE createdByAgentId = :agentId AND status = :status")
    suspend fun getLoansByAgentAndStatus(agentId: String, status: String): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE approvedByAdminId = :adminId")
    suspend fun getLoansApprovedByAdmin(adminId: String): List<LoanEntity>

    @Query("SELECT * FROM loans")
    suspend fun getAllLoans(): List<LoanEntity>

    @Query("SELECT * FROM loans")
    fun getAllLoansFlow(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Delete
    suspend fun deleteLoan(loan: LoanEntity)

    @Query("DELETE FROM loans WHERE id = :loanId")
    suspend fun deleteLoanById(loanId: String)

    @Query("DELETE FROM loans")
    suspend fun deleteAllLoans()

    @Query("SELECT SUM(outstandingBalance) FROM loans WHERE status IN ('ACTIVE', 'APPROVED')")
    suspend fun getTotalOutstandingBalance(): Double?

    @Query("SELECT COUNT(*) FROM loans WHERE status = :status")
    suspend fun getLoanCountByStatus(status: String): Int
}
