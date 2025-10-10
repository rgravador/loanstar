package com.example.loanstar.data.remote.dto

import com.example.loanstar.data.model.ApprovalStatus
import com.example.loanstar.data.model.ProfileDetails
import com.example.loanstar.data.model.User
import com.example.loanstar.data.model.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * DTO for User table that matches the Supabase database schema with snake_case column names
 */
@Serializable
data class UserDto(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val username: String,

    @SerialName("email")
    val email: String,

    @SerialName("role")
    val role: String,

    @SerialName("approval_status")
    val approvalStatus: String = "PENDING",

    @SerialName("approved_by_admin_id")
    val approvedByAdminId: String? = null,

    @SerialName("approval_date")
    val approvalDate: String? = null, // ISO timestamp string

    @SerialName("rejection_reason")
    val rejectionReason: String? = null,

    @SerialName("profile_details")
    val profileDetails: JsonObject? = null,

    @SerialName("created_at")
    val createdAt: String? = null, // ISO timestamp string

    @SerialName("updated_at")
    val updatedAt: String? = null // ISO timestamp string
)

/**
 * Convert UserDto to domain User model
 */
fun UserDto.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        role = UserRole.valueOf(role.uppercase()), // Convert to uppercase to match enum
        approvalStatus = ApprovalStatus.valueOf(approvalStatus), // Already uppercase
        approvedByAdminId = approvedByAdminId,
        approvalDate = approvalDate?.let { parseTimestamp(it) },
        rejectionReason = rejectionReason,
        profileDetails = profileDetails?.let { parseProfileDetails(it) },
        createdAt = createdAt?.let { parseTimestamp(it) } ?: System.currentTimeMillis(),
        updatedAt = updatedAt?.let { parseTimestamp(it) } ?: System.currentTimeMillis()
    )
}

/**
 * Convert domain User model to UserDto
 */
fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        username = username,
        email = email,
        role = role.name.lowercase(), // Convert to lowercase to match database constraint
        approvalStatus = approvalStatus.name, // Keep uppercase for approval_status
        approvedByAdminId = approvedByAdminId,
        approvalDate = approvalDate?.let { formatTimestamp(it) },
        rejectionReason = rejectionReason,
        profileDetails = profileDetails?.let { profileDetailsToJson(it) },
        createdAt = formatTimestamp(createdAt),
        updatedAt = formatTimestamp(updatedAt)
    )
}

/**
 * Parse ISO timestamp string to milliseconds
 * Compatible with API level 24+
 */
private fun parseTimestamp(timestamp: String): Long {
    return try {
        // Parse ISO 8601 timestamp (e.g., "2024-10-10T10:11:24.306Z")
        // Using SimpleDateFormat for API 24 compatibility
        val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
        format.parse(timestamp)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        // Fallback for timestamps without milliseconds
        try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(timestamp)?.time ?: System.currentTimeMillis()
        } catch (e2: Exception) {
            System.currentTimeMillis()
        }
    }
}

/**
 * Format milliseconds to ISO timestamp string
 * Compatible with API level 24+
 */
private fun formatTimestamp(millis: Long): String {
    val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return format.format(java.util.Date(millis))
}

/**
 * Parse JSONB profile_details to ProfileDetails
 */
private fun parseProfileDetails(json: JsonObject): ProfileDetails? {
    return try {
        ProfileDetails(
            fullName = json["fullName"]?.toString()?.removeSurrounding("\"") ?: "",
            phone = json["phone"]?.toString()?.removeSurrounding("\""),
            avatarUrl = json["avatarUrl"]?.toString()?.removeSurrounding("\""),
            address = json["address"]?.toString()?.removeSurrounding("\"")
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert ProfileDetails to JSONB format
 */
private fun profileDetailsToJson(details: ProfileDetails): JsonObject {
    return kotlinx.serialization.json.buildJsonObject {
        put("fullName", kotlinx.serialization.json.JsonPrimitive(details.fullName))
        details.phone?.let { put("phone", kotlinx.serialization.json.JsonPrimitive(it)) }
        details.avatarUrl?.let { put("avatarUrl", kotlinx.serialization.json.JsonPrimitive(it)) }
        details.address?.let { put("address", kotlinx.serialization.json.JsonPrimitive(it)) }
    }
}
