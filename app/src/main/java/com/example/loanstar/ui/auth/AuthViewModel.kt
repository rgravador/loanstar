package com.example.loanstar.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanstar.data.model.User
import com.example.loanstar.data.model.UserRole
import com.example.loanstar.data.repository.AuthRepository
import com.example.loanstar.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication screens
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        checkAuthStatus()
    }

    /**
     * Check if user is already logged in
     */
    private fun checkAuthStatus() {
        if (authRepository.isLoggedIn()) {
            viewModelScope.launch {
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    when (val result = authRepository.getUserById(userId)) {
                        is Result.Success -> {
                            _currentUser.value = result.data
                            _authState.value = AuthState.Authenticated(result.data)
                        }
                        is Result.Error -> {
                            _authState.value = AuthState.Error(result.exception.message ?: "Unknown error")
                        }
                        is Result.Loading -> {}
                    }
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        // Validate inputs
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password are required")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.exception.message ?: "Login failed")
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Register new user
     */
    fun register(email: String, password: String, username: String, role: UserRole) {
        // Validate inputs
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.register(email, password, username, role)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error(result.exception.message ?: "Registration failed")
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Logout
     */
    fun logout() {
        viewModelScope.launch {
            when (authRepository.logout()) {
                is Result.Success -> {
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error("Logout failed")
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Reset password
     */
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Email is required")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (authRepository.resetPassword(email)) {
                is Result.Success -> {
                    _authState.value = AuthState.PasswordResetSent
                }
                is Result.Error -> {
                    _authState.value = AuthState.Error("Failed to send password reset email")
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}

/**
 * Authentication state
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordResetSent : AuthState()
}
