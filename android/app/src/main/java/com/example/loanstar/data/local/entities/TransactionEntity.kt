package com.example.loanstar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.loanstar.data.model.Transaction
import com.example.loanstar.data.model.TransactionType

/**
 * Room entity for Transaction table (Audit Log)
 * Matches the schema defined in setup.sql
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["type"]),
        Index(value = ["timestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val type: String, // CREATE_ACCOUNT, UPDATE_ACCOUNT, CREATE_LOAN, APPROVE_LOAN, REJECT_LOAN, RECEIVE_PAYMENT, CASHOUT_REQUEST, CASHOUT_APPROVED, CASHOUT_REJECTED, UPDATE_COMMISSION, USER_LOGIN, USER_LOGOUT
    val userId: String,
    val details: String, // JSON string with transaction details
    val timestamp: Long = System.currentTimeMillis(),
    val relatedEntityId: String? = null,
    val relatedEntityType: String? = null
)

/**
 * Convert TransactionEntity to domain Transaction model
 */
fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        type = TransactionType.valueOf(type),
        userId = userId,
        details = details,
        timestamp = timestamp,
        relatedEntityId = relatedEntityId,
        relatedEntityType = relatedEntityType
    )
}

/**
 * Convert domain Transaction model to TransactionEntity
 */
fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        type = type.name,
        userId = userId,
        details = details,
        timestamp = timestamp,
        relatedEntityId = relatedEntityId,
        relatedEntityType = relatedEntityType
    )
}
