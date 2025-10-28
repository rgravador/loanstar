package com.example.loanstar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.loanstar.data.model.Account
import com.example.loanstar.data.model.AccountStatus
import com.example.loanstar.data.model.ContactInfo
import kotlinx.serialization.Serializable

/**
 * Room entity for Account table
 * Matches the schema defined in setup.sql
 */
@Serializable
@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignedAgentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["assignedAgentId"]),
        Index(value = ["status"]),
        Index(value = ["name"])
    ]
)
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val contactInfo: String, // JSON string (phone, email, alternatePhone)
    val address: String,
    val assignedAgentId: String,
    val idProofUrl: String? = null,
    val creationDate: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE" // ACTIVE, INACTIVE, SUSPENDED
)

/**
 * Convert AccountEntity to domain Account model
 */
fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        contactInfo = ContactInfo("", null, null), // Will be parsed from JSON
        address = address,
        assignedAgentId = assignedAgentId,
        idProofUrl = idProofUrl,
        creationDate = creationDate,
        updatedAt = updatedAt,
        status = AccountStatus.valueOf(status)
    )
}

/**
 * Convert domain Account model to AccountEntity
 */
fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        contactInfo = "", // Will be converted to JSON
        address = address,
        assignedAgentId = assignedAgentId,
        idProofUrl = idProofUrl,
        creationDate = creationDate,
        updatedAt = updatedAt,
        status = status.name
    )
}
