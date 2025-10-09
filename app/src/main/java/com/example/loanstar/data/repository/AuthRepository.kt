package com.example.loanstar.data.repository

import com.example.loanstar.data.local.dao.UserDao
import com.example.loanstar.data.local.entities.UserEntity
import com.example.loanstar.data.local.entities.toDomain
import com.example.loanstar.data.local.entities.toEntity
import com.example.loanstar.data.model.ApprovalStatus
import com.example.loanstar.data.model.User
import com.example.loanstar.data.model.UserRole
import com.example.loanstar.data.remote.SupabaseClientProvider
import com.example.loanstar.util.Result
import com.example.loanstar.util.safeCall
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for authentication operations
 * Handles user login, registration, session management with Supabase Auth
 * Caches user data locally using Room
 */
@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClientProvider,
    private val userDao: UserDao
) {

    /**
     * Get current user from local cache
     */
    fun getCurrentUserFlow(): Flow<User?> {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return kotlinx.coroutines.flow.flowOf(null)
        return userDao.getUserByIdFlow(userId).map { it?.toDomain() }
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return supabaseClient.auth.currentUserOrNull() != null
    }

    /**
     * Register a new user with email and password
     * Agents are set to PENDING status by default and require admin approval
     * Admins are auto-approved
     */
    suspend fun register(
        email: String,
        password: String,
        username: String,
        role: UserRole
    ): Result<User> = safeCall {
        // Register with Supabase Auth
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw Exception("Failed to get user ID after registration")

        // Set approval status: PENDING for agents, APPROVED for admins
        val approvalStatus = if (role == UserRole.AGENT) {
            ApprovalStatus.PENDING
        } else {
            ApprovalStatus.APPROVED
        }

        // Create user profile in database
        val user = User(
            id = userId,
            username = username,
            email = email,
            role = role,
            approvalStatus = approvalStatus,
            approvedByAdminId = null,
            approvalDate = if (role == UserRole.ADMIN) System.currentTimeMillis() else null,
            rejectionReason = null,
            profileDetails = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Insert into Supabase
        supabaseClient.postgrest.from("users").insert(user)

        // Cache locally
        userDao.insertUser(user.toEntity())

        user
    }

    /**
     * Login with email and password
     * Checks if user is approved before allowing login
     */
    suspend fun login(email: String, password: String): Result<User> = safeCall {
        // Login with Supabase Auth
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw Exception("Failed to get user ID after login")

        // Fetch user profile from Supabase
        val userEntity = supabaseClient.postgrest
            .from("users")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<UserEntity>()

        val user = userEntity.toDomain()

        // Check approval status
        when (user.approvalStatus) {
            ApprovalStatus.PENDING -> {
                // Logout user since they're not approved
                supabaseClient.auth.signOut()
                throw Exception("Your account is pending approval. Please wait for admin approval.")
            }
            ApprovalStatus.REJECTED -> {
                // Logout user since they're rejected
                supabaseClient.auth.signOut()
                val reason = user.rejectionReason ?: "No reason provided"
                throw Exception("Your account has been rejected. Reason: $reason")
            }
            ApprovalStatus.APPROVED -> {
                // User is approved, allow login
                // Cache locally
                userDao.insertUser(userEntity)

                // Log transaction
                logTransaction("USER_LOGIN", userId, "User logged in successfully")
            }
        }

        user
    }

    /**
     * Logout current user
     */
    suspend fun logout(): Result<Unit> = safeCall {
        val userId = getCurrentUserId()

        // Log transaction before logout
        if (userId != null) {
            logTransaction("USER_LOGOUT", userId, "User logged out")
        }

        // Logout from Supabase
        supabaseClient.auth.signOut()

        // Clear local cache (optional - keep for offline access)
        // userDao.deleteAllUsers()
    }

    /**
     * Reset password
     */
    suspend fun resetPassword(email: String): Result<Unit> = safeCall {
        supabaseClient.auth.resetPasswordForEmail(email)
    }

    /**
     * Update user profile
     */
    suspend fun updateProfile(user: User): Result<User> = safeCall {
        // Update in Supabase
        supabaseClient.postgrest
            .from("users")
            .update(user.toEntity()) {
                filter {
                    eq("id", user.id)
                }
            }

        // Update local cache
        userDao.updateUser(user.toEntity())

        user
    }

    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): Result<User> = safeCall {
        // Try local cache first
        val cachedUser = userDao.getUserById(userId)
        if (cachedUser != null) {
            return@safeCall cachedUser.toDomain()
        }

        // Fetch from Supabase
        val userEntity = supabaseClient.postgrest
            .from("users")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<UserEntity>()

        // Cache locally
        userDao.insertUser(userEntity)

        userEntity.toDomain()
    }

    /**
     * Get users by role (for admin)
     */
    suspend fun getUsersByRole(role: UserRole): Result<List<User>> = safeCall {
        val users = supabaseClient.postgrest
            .from("users")
            .select {
                filter {
                    eq("role", role.name)
                }
            }
            .decodeList<UserEntity>()

        // Cache locally
        userDao.insertUsers(users)

        users.map { it.toDomain() }
    }

    /**
     * Change password
     */
    suspend fun changePassword(newPassword: String): Result<Unit> = safeCall {
        supabaseClient.auth.modifyUser {
            password = newPassword
        }
    }

    /**
     * Refresh session token
     */
    suspend fun refreshSession(): Result<Unit> = safeCall {
        supabaseClient.auth.refreshCurrentSession()
    }

    /**
     * Log transaction for audit trail
     */
    private suspend fun logTransaction(type: String, userId: String, details: String) {
        try {
            val transaction = mapOf(
                "type" to type,
                "user_id" to userId,
                "details" to details,
                "timestamp" to System.currentTimeMillis()
            )
            supabaseClient.postgrest.from("transactions").insert(transaction)
        } catch (e: Exception) {
            // Log error but don't fail the operation
            android.util.Log.e("loanstar", "Error logging transaction in AuthRepository: ${e.message}", e)
            e.printStackTrace()
        }
    }
}
