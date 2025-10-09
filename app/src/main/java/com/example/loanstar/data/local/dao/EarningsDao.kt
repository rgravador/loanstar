package com.example.loanstar.data.local.dao

import androidx.room.*
import com.example.loanstar.data.local.entities.EarningsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Earnings entities
 */
@Dao
interface EarningsDao {

    @Query("SELECT * FROM earnings WHERE id = :earningsId")
    suspend fun getEarningsById(earningsId: String): EarningsEntity?

    @Query("SELECT * FROM earnings WHERE id = :earningsId")
    fun getEarningsByIdFlow(earningsId: String): Flow<EarningsEntity?>

    @Query("SELECT * FROM earnings WHERE agentId = :agentId")
    suspend fun getEarningsByAgent(agentId: String): EarningsEntity?

    @Query("SELECT * FROM earnings WHERE agentId = :agentId")
    fun getEarningsByAgentFlow(agentId: String): Flow<EarningsEntity?>

    @Query("SELECT * FROM earnings")
    suspend fun getAllEarnings(): List<EarningsEntity>

    @Query("SELECT * FROM earnings")
    fun getAllEarningsFlow(): Flow<List<EarningsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarnings(earnings: EarningsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarningsList(earningsList: List<EarningsEntity>)

    @Update
    suspend fun updateEarnings(earnings: EarningsEntity)

    @Delete
    suspend fun deleteEarnings(earnings: EarningsEntity)

    @Query("DELETE FROM earnings WHERE id = :earningsId")
    suspend fun deleteEarningsById(earningsId: String)

    @Query("DELETE FROM earnings WHERE agentId = :agentId")
    suspend fun deleteEarningsByAgent(agentId: String)

    @Query("DELETE FROM earnings")
    suspend fun deleteAllEarnings()

    @Query("SELECT SUM(totalEarnings) FROM earnings")
    suspend fun getTotalAllAgentsEarnings(): Double?

    @Query("SELECT SUM(collectibleEarnings) FROM earnings")
    suspend fun getTotalCollectibleEarnings(): Double?

    @Query("SELECT SUM(cashedOutAmount) FROM earnings")
    suspend fun getTotalCashedOut(): Double?
}
