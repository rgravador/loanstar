package com.example.loanstar.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.loanstar.data.model.ApprovalStatus
import com.example.loanstar.data.model.ProfileDetails
import com.example.loanstar.data.model.User
import com.example.loanstar.data.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class for profile statistics
 */
data class ProfileStats(
    val totalAccounts: Int,
    val activeLoans: Int
)

class ProfileViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _stats = MutableLiveData<ProfileStats>()
    val stats: LiveData<ProfileStats> = _stats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadUserProfile()
        loadProfileStats()
    }

    /**
     * Load user profile from database
     * TODO: Replace with actual Supabase API call when backend is implemented
     */
    fun loadUserProfile() {
        _isLoading.value = true

        // Simulate loading - replace with actual Supabase call
        // For now, creating mock data
        val mockUser = User(
            id = "123e4567-e89b-12d3-a456-426614174000",
            username = "johndoe",
            email = "john.doe@loanstar.com",
            role = UserRole.AGENT,
            approvalStatus = ApprovalStatus.APPROVED,
            profileDetails = ProfileDetails(
                fullName = null,
                phone = null,
                address = null,
                dateOfBirth = null
            ),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        _user.value = mockUser
        _isLoading.value = false
    }

    /**
     * Load profile statistics
     * TODO: Replace with actual Supabase API call when backend is implemented
     */
    private fun loadProfileStats() {
        // Mock stats data - replace with actual API call
        val mockStats = ProfileStats(
            totalAccounts = 24,
            activeLoans = 15
        )

        _stats.value = mockStats
    }

    /**
     * Update user profile details
     * TODO: Replace with actual Supabase API call when backend is implemented
     */
    fun updateProfile(fullName: String?, phone: String?, address: String?, dateOfBirth: String?) {
        _isLoading.value = true

        try {
            val currentUser = _user.value
            if (currentUser == null) {
                _errorMessage.value = "User data not available"
                _isLoading.value = false
                return
            }

            // Validate date format if provided
            if (!dateOfBirth.isNullOrBlank()) {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    dateFormat.isLenient = false
                    dateFormat.parse(dateOfBirth)
                } catch (e: Exception) {
                    _errorMessage.value = "Invalid date format. Use YYYY-MM-DD"
                    _isLoading.value = false
                    return
                }
            }

            // Create updated profile details
            val updatedProfileDetails = ProfileDetails(
                fullName = fullName?.takeIf { it.isNotBlank() },
                phone = phone?.takeIf { it.isNotBlank() },
                address = address?.takeIf { it.isNotBlank() },
                dateOfBirth = dateOfBirth?.takeIf { it.isNotBlank() }
            )

            // Update user with new profile details
            val updatedUser = currentUser.copy(
                profileDetails = updatedProfileDetails,
                updatedAt = System.currentTimeMillis()
            )

            // TODO: Save to Supabase database
            // For now, just update the local state
            _user.value = updatedUser
            _errorMessage.value = null
            _isLoading.value = false

        } catch (e: Exception) {
            _errorMessage.value = "Failed to update profile: ${e.message}"
            _isLoading.value = false
        }
    }

    /**
     * Get formatted member since date
     */
    fun getFormattedMemberSince(): String {
        val createdAt = _user.value?.createdAt ?: return "N/A"
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date(createdAt))
    }

    /**
     * Get user initials for avatar
     */
    fun getUserInitials(): String {
        val username = _user.value?.username ?: return "U"
        return if (username.isNotEmpty()) {
            username.first().uppercase()
        } else {
            "U"
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
