package com.example.loanstar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.loanstar.data.model.Notification
import com.example.loanstar.data.model.NotificationPriority
import com.example.loanstar.data.model.NotificationType

/**
 * Room entity for Notification table
 * Matches the schema defined in setup.sql
 */
@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["isRead"]),
        Index(value = ["timestamp"]),
        Index(value = ["type"])
    ]
)
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val message: String,
    val type: String, // PAST_DUE, UPCOMING_DUE, LOAN_APPROVED, LOAN_REJECTED, CASHOUT_APPROVED, CASHOUT_REJECTED, PAYMENT_RECEIVED, ACCOUNT_CREATED, GENERAL
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedEntityId: String? = null,
    val relatedEntityType: String? = null,
    val priority: String = "NORMAL" // LOW, NORMAL, HIGH, URGENT
)

/**
 * Convert NotificationEntity to domain Notification model
 */
fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = id,
        userId = userId,
        message = message,
        type = NotificationType.valueOf(type),
        timestamp = timestamp,
        isRead = isRead,
        relatedEntityId = relatedEntityId,
        relatedEntityType = relatedEntityType,
        priority = NotificationPriority.valueOf(priority)
    )
}

/**
 * Convert domain Notification model to NotificationEntity
 */
fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        userId = userId,
        message = message,
        type = type.name,
        timestamp = timestamp,
        isRead = isRead,
        relatedEntityId = relatedEntityId,
        relatedEntityType = relatedEntityType,
        priority = priority.name
    )
}
