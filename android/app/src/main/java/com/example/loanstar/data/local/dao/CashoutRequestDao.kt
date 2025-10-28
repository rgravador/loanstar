package com.example.loanstar.data.local.dao

import androidx.room.*
import com.example.loanstar.data.local.entities.CashoutRequestEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CashoutRequest entities
 */
@Dao
interface CashoutRequestDao {

    @Query("SELECT * FROM cashout_requests WHERE id = :requestId")
    suspend fun getCashoutRequestById(requestId: String): CashoutRequestEntity?

    @Query("SELECT * FROM cashout_requests WHERE id = :requestId")
    fun getCashoutRequestByIdFlow(requestId: String): Flow<CashoutRequestEntity?>

    @Query("SELECT * FROM cashout_requests WHERE agentId = :agentId ORDER BY requestDate DESC")
    suspend fun getCashoutRequestsByAgent(agentId: String): List<CashoutRequestEntity>

    @Query("SELECT * FROM cashout_requests WHERE agentId = :agentId ORDER BY requestDate DESC")
    fun getCashoutRequestsByAgentFlow(agentId: String): Flow<List<CashoutRequestEntity>>

    @Query("SELECT * FROM cashout_requests WHERE status = :status ORDER BY requestDate DESC")
    suspend fun getCashoutRequestsByStatus(status: String): List<CashoutRequestEntity>

    @Query("SELECT * FROM cashout_requests WHERE status = :status ORDER BY requestDate DESC")
    fun getCashoutRequestsByStatusFlow(status: String): Flow<List<CashoutRequestEntity>>

    @Query("SELECT * FROM cashout_requests WHERE agentId = :agentId AND status = :status ORDER BY requestDate DESC")
    suspend fun getCashoutRequestsByAgentAndStatus(agentId: String, status: String): List<CashoutRequestEntity>

    @Query("SELECT * FROM cashout_requests WHERE approvedByAdminId = :adminId ORDER BY requestDate DESC")
    suspend fun getCashoutRequestsApprovedByAdmin(adminId: String): List<CashoutRequestEntity>

    @Query("SELECT * FROM cashout_requests ORDER BY requestDate DESC")
    suspend fun getAllCashoutRequests(): List<CashoutRequestEntity>

    @Query("SELECT * FROM cashout_requests ORDER BY requestDate DESC")
    fun getAllCashoutRequestsFlow(): Flow<List<CashoutRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashoutRequest(request: CashoutRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashoutRequests(requests: List<CashoutRequestEntity>)

    @Update
    suspend fun updateCashoutRequest(request: CashoutRequestEntity)

    @Delete
    suspend fun deleteCashoutRequest(request: CashoutRequestEntity)

    @Query("DELETE FROM cashout_requests WHERE id = :requestId")
    suspend fun deleteCashoutRequestById(requestId: String)

    @Query("DELETE FROM cashout_requests WHERE agentId = :agentId")
    suspend fun deleteCashoutRequestsByAgent(agentId: String)

    @Query("DELETE FROM cashout_requests")
    suspend fun deleteAllCashoutRequests()

    @Query("SELECT COUNT(*) FROM cashout_requests WHERE status = 'PENDING'")
    suspend fun getPendingCashoutRequestCount(): Int

    @Query("SELECT COUNT(*) FROM cashout_requests WHERE status = 'PENDING'")
    fun getPendingCashoutRequestCountFlow(): Flow<Int>
}
