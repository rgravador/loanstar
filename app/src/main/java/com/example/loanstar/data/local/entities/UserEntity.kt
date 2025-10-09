package com.example.loanstar.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.loanstar.data.model.ApprovalStatus
import com.example.loanstar.data.model.ProfileDetails
import com.example.loanstar.data.model.User
import com.example.loanstar.data.model.UserRole
import kotlinx.serialization.Serializable

/**
 * Room entity for User table
 * Matches the schema defined in setup.sql
 */
@Serializable
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true),
        Index(value = ["role"]),
        Index(value = ["approvalStatus"])
    ]
)
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val role: String, // ADMIN or AGENT
    val approvalStatus: String = "PENDING", // PENDING, APPROVED, REJECTED
    val approvedByAdminId: String? = null,
    val approvalDate: Long? = null,
    val rejectionReason: String? = null,
    val profileDetails: String? = null, // JSON string
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Convert UserEntity to domain User model
 */
fun UserEntity.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        role = UserRole.valueOf(role),
        approvalStatus = ApprovalStatus.valueOf(approvalStatus),
        approvedByAdminId = approvedByAdminId,
        approvalDate = approvalDate,
        rejectionReason = rejectionReason,
        profileDetails = profileDetails?.let {
            // Parse JSON to ProfileDetails
            // Will be handled by type converter
            null
        },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Convert domain User model to UserEntity
 */
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        email = email,
        role = role.name,
        approvalStatus = approvalStatus.name,
        approvedByAdminId = approvedByAdminId,
        approvalDate = approvalDate,
        rejectionReason = rejectionReason,
        profileDetails = profileDetails?.let {
            // Convert ProfileDetails to JSON
            // Will be handled by type converter
            null
        },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
