package com.example.loanstar.data.model

import kotlinx.serialization.Serializable

/**
 * Notification type
 */
enum class NotificationType {
    PAST_DUE,
    UPCOMING_DUE,
    LOAN_APPROVED,
    LOAN_REJECTED,
    CASHOUT_APPROVED,
    CASHOUT_REJECTED,
    PAYMENT_RECEIVED,
    ACCOUNT_CREATED,
    GENERAL
}

/**
 * Notification model
 */
@Serializable
data class Notification(
    val id: String,
    val userId: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedEntityId: String? = null, // Could be loanId, accountId, etc.
    val relatedEntityType: String? = null, // "loan", "account", "cashout", etc.
    val priority: NotificationPriority = NotificationPriority.NORMAL
)

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}
