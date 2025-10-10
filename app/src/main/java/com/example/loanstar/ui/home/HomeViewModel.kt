package com.example.loanstar.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanstar.data.model.UserRole
import com.example.loanstar.data.repository.AuthRepository
import com.example.loanstar.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Admin Dashboard (Home screen)
 * Fetches and manages dashboard statistics and agents list
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _dashboardState = MutableLiveData<DashboardState>(DashboardState.Loading)
    val dashboardState: LiveData<DashboardState> = _dashboardState

    private val _statistics = MutableLiveData<DashboardStatistics>()
    val statistics: LiveData<DashboardStatistics> = _statistics

    private val _agents = MutableLiveData<List<AgentItemData>>()
    val agents: LiveData<List<AgentItemData>> = _agents

    init {
        loadDashboardData()
    }

    /**
     * Load dashboard data including statistics and agents list
     */
    fun loadDashboardData() {
        _dashboardState.value = DashboardState.Loading

        viewModelScope.launch {
            try {
                // Fetch agents list
                when (val result = authRepository.getUsersByRole(UserRole.AGENT)) {
                    is Result.Success -> {
                        val agentsList = result.data.map { user ->
                            // TODO: Fetch actual accounts count and earnings from repositories
                            // For now, using placeholder data
                            AgentItemData(
                                user = user,
                                accountsCount = (0..50).random(),
                                totalEarnings = (5000..50000).random().toDouble()
                            )
                        }

                        _agents.value = agentsList

                        // Calculate statistics
                        val stats = DashboardStatistics(
                            agentsCount = agentsList.size,
                            totalLoanReleases = calculateTotalLoanReleases(),
                            totalCollectables = calculateTotalCollectables(),
                            activeAccountsCount = calculateActiveAccounts()
                        )

                        _statistics.value = stats
                        _dashboardState.value = DashboardState.Success
                    }
                    is Result.Error -> {
                        _dashboardState.value = DashboardState.Error(
                            result.exception.message ?: "Failed to load dashboard data"
                        )
                    }
                    is Result.Loading -> {
                        // Already set to Loading at the beginning
                    }
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(
                    e.message ?: "An error occurred"
                )
            }
        }
    }

    /**
     * TODO: Implement actual calculation from loan repository
     * Calculate total loan releases
     */
    private fun calculateTotalLoanReleases(): Double {
        // Placeholder: This should fetch from LoanRepository
        return (100000..1000000).random().toDouble()
    }

    /**
     * TODO: Implement actual calculation from loan repository
     * Calculate total collectables (outstanding balances)
     */
    private fun calculateTotalCollectables(): Double {
        // Placeholder: This should fetch from LoanRepository
        return (50000..500000).random().toDouble()
    }

    /**
     * TODO: Implement actual calculation from account repository
     * Calculate number of active accounts
     */
    private fun calculateActiveAccounts(): Int {
        // Placeholder: This should fetch from AccountRepository
        return (50..200).random()
    }

    /**
     * Retry loading dashboard data after an error
     */
    fun retry() {
        loadDashboardData()
    }

    /**
     * Approve an agent by their ID
     */
    fun approveAgent(agentId: String) {
        viewModelScope.launch {
            try {
                // Get current admin ID
                val adminId = authRepository.getCurrentUserId()
                    ?: throw Exception("Admin not logged in")

                when (val result = authRepository.approveAgent(agentId, adminId)) {
                    is Result.Success -> {
                        // Reload dashboard to reflect changes
                        loadDashboardData()
                    }
                    is Result.Error -> {
                        _dashboardState.value = DashboardState.Error(
                            result.exception.message ?: "Failed to approve agent"
                        )
                    }
                    is Result.Loading -> {
                        // No action needed
                    }
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(
                    e.message ?: "An error occurred while approving agent"
                )
            }
        }
    }

    /**
     * Reject an agent by their ID
     */
    fun rejectAgent(agentId: String, reason: String = "No reason provided") {
        viewModelScope.launch {
            try {
                // Get current admin ID
                val adminId = authRepository.getCurrentUserId()
                    ?: throw Exception("Admin not logged in")

                when (val result = authRepository.rejectAgent(agentId, adminId, reason)) {
                    is Result.Success -> {
                        // Reload dashboard to reflect changes
                        loadDashboardData()
                    }
                    is Result.Error -> {
                        _dashboardState.value = DashboardState.Error(
                            result.exception.message ?: "Failed to reject agent"
                        )
                    }
                    is Result.Loading -> {
                        // No action needed
                    }
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(
                    e.message ?: "An error occurred while rejecting agent"
                )
            }
        }
    }
}

/**
 * Dashboard state sealed class
 */
sealed class DashboardState {
    object Loading : DashboardState()
    object Success : DashboardState()
    data class Error(val message: String) : DashboardState()
}

/**
 * Dashboard statistics data class
 */
data class DashboardStatistics(
    val agentsCount: Int,
    val totalLoanReleases: Double,
    val totalCollectables: Double,
    val activeAccountsCount: Int
)
