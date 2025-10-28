package com.example.loanstar.data.model

import org.json.JSONObject

/**
 * User role enum
 */
enum class UserRole {
    ADMIN,
    AGENT
}

/**
 * Approval status enum for user accounts
 */
enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

/**
 * User data model representing the users table from Supabase
 */
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

/**
 * Profile details stored in JSONB format in the database
 */
data class ProfileDetails(
    val fullName: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val address: String? = null,
    val dateOfBirth: String? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            fullName?.let { put("fullName", it) }
            phone?.let { put("phone", it) }
            address?.let { put("address", it) }
            dateOfBirth?.let { put("dateOfBirth", it) }
        }
    }

    companion object {
        fun fromJson(json: JSONObject?): ProfileDetails? {
            if (json == null) return null
            return ProfileDetails(
                fullName = json.optString("fullName", null),
                phone = json.optString("phone", null),
                avatarUrl = json.optString("avatarUrl", null),
                address = json.optString("address", null),
                dateOfBirth = json.optString("dateOfBirth", null)
            )
        }
    }
}
