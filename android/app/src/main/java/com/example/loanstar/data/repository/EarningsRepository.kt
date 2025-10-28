package com.example.loanstar.data.repository

import com.example.loanstar.data.local.dao.EarningsDao
import com.example.loanstar.data.local.entities.EarningsEntity
import com.example.loanstar.data.local.entities.toDomain
import com.example.loanstar.data.local.entities.toEntity
import com.example.loanstar.data.model.Earnings
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

@Singleton
class EarningsRepository @Inject constructor(
    private val supabaseClient: SupabaseClientProvider,
    private val earningsDao: EarningsDao,
    private val networkMonitor: NetworkMonitor
) {

    fun getEarningsByAgentFlow(agentId: String): Flow<Earnings?> {
        return earningsDao.getEarningsByAgentFlow(agentId).map { it?.toDomain() }
    }

    suspend fun getEarningsByAgent(agentId: String, forceRefresh: Boolean = false): Result<Earnings> = safeCall {
        val isOnline = networkMonitor.isConnected.first()

        if (isOnline && forceRefresh) {
            val earningsEntity = supabaseClient.postgrest
                .from("earnings")
                .select { filter { eq("agent_id", agentId) } }
                .decodeSingle<EarningsEntity>()

            earningsDao.insertEarnings(earningsEntity)
            return@safeCall earningsEntity.toDomain()
        }

        val cached = earningsDao.getEarningsByAgent(agentId)
        if (cached != null) {
            return@safeCall cached.toDomain()
        }

        throw Exception("Earnings not found in cache and no network connection")
    }

    suspend fun updateEarnings(earnings: Earnings): Result<Earnings> = safeCall {
        val isOnline = networkMonitor.isConnected.first()

        if (isOnline) {
            supabaseClient.postgrest
                .from("earnings")
                .update(earnings.toEntity()) {
                    filter { eq("agent_id", earnings.agentId) }
                }
        }

        earningsDao.updateEarnings(earnings.toEntity())
        earnings
    }
}
