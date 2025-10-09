package com.example.loanstar.data.model

import kotlinx.serialization.Serializable

/**
 * User roles in the system
 */
enum class UserRole {
    ADMIN,
    AGENT
}

/**
 * User approval status
 * PENDING - Agent registration awaiting admin approval
 * APPROVED - User approved and can login
 * REJECTED - User registration rejected
 */
enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

/**
 * User model representing a user in the system
 */
@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val role: UserRole,
    val approvalStatus: ApprovalStatus = ApprovalStatus.PENDING,
    val approvedByAdminId: String? = null,
    val approvalDate: Long? = null,
    val rejectionReason: String? = null,
    val profileDetails: ProfileDetails? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class ProfileDetails(
    val fullName: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val address: String? = null
)
