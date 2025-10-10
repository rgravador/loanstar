package com.example.loanstar.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanstar.data.model.ApprovalStatus
import com.example.loanstar.data.model.User
import com.example.loanstar.data.model.UserRole
import com.example.loanstar.data.repository.AuthRepository
import com.example.loanstar.ui.home.AgentItemData
import com.example.loanstar.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Status filter options
 */
enum class StatusFilter {
    ALL,
    PENDING,
    APPROVED,
    REJECTED
}

/**
 * ViewModel for Admin Dashboard
 * Displays agents with status filtering
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _agents = MutableLiveData<List<AgentItemData>>()
    val agents: LiveData<List<AgentItemData>> = _agents

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _currentFilter = MutableLiveData<StatusFilter>(StatusFilter.PENDING)
    val currentFilter: LiveData<StatusFilter> = _currentFilter

    // Store all agents for filtering
    private var allAgents: List<User> = emptyList()

    init {
        loadAgents()
    }

    /**
     * Load all agents from repository
     */
    fun loadAgents() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = authRepository.getUsersByRole(UserRole.AGENT)) {
                is Result.Success -> {
                    allAgents = result.data
                    applyFilter(_currentFilter.value ?: StatusFilter.PENDING)
                }
                is Result.Error -> {
                    _errorMessage.value = result.exception.message ?: "Failed to load agents"
                    _agents.value = emptyList()
                }
                is Result.Loading -> {
                    // Loading state is already handled by _isLoading
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Set status filter
     */
    fun setFilter(filter: StatusFilter) {
        _currentFilter.value = filter
        applyFilter(filter)
    }

    /**
     * Apply filter to agents list
     */
    private fun applyFilter(filter: StatusFilter) {
        val filteredAgents = when (filter) {
            StatusFilter.ALL -> allAgents
            StatusFilter.PENDING -> allAgents.filter { it.approvalStatus == ApprovalStatus.PENDING }
            StatusFilter.APPROVED -> allAgents.filter { it.approvalStatus == ApprovalStatus.APPROVED }
            StatusFilter.REJECTED -> allAgents.filter { it.approvalStatus == ApprovalStatus.REJECTED }
        }

        // Convert to AgentItemData
        _agents.value = filteredAgents.map { user ->
            AgentItemData(
                user = user,
                accountsCount = 0,
                totalEarnings = 0.0
            )
        }
    }

    /**
     * Approve an agent
     */
    fun approveAgent(agentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId == null) {
                _errorMessage.value = "Not logged in"
                _isLoading.value = false
                return@launch
            }

            when (val result = authRepository.approveAgent(agentId, currentUserId)) {
                is Result.Success -> {
                    // Reload agents
                    loadAgents()
                }
                is Result.Error -> {
                    _errorMessage.value = result.exception.message ?: "Failed to approve agent"
                    _isLoading.value = false
                }
                is Result.Loading -> {
                    // Loading state is already handled by _isLoading
                }
            }
        }
    }

    /**
     * Reject an agent
     */
    fun rejectAgent(agentId: String, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId == null) {
                _errorMessage.value = "Not logged in"
                _isLoading.value = false
                return@launch
            }

            when (val result = authRepository.rejectAgent(agentId, currentUserId, reason)) {
                is Result.Success -> {
                    // Reload agents
                    loadAgents()
                }
                is Result.Error -> {
                    _errorMessage.value = result.exception.message ?: "Failed to reject agent"
                    _isLoading.value = false
                }
                is Result.Loading -> {
                    // Loading state is already handled by _isLoading
                }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
