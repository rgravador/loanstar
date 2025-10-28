package com.example.loanstar.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Transaction type for audit logging
 */
enum class TransactionType {
    CREATE_ACCOUNT,
    UPDATE_ACCOUNT,
    CREATE_LOAN,
    APPROVE_LOAN,
    REJECT_LOAN,
    RECEIVE_PAYMENT,
    CASHOUT_REQUEST,
    CASHOUT_APPROVED,
    CASHOUT_REJECTED,
    UPDATE_COMMISSION,
    USER_LOGIN,
    USER_LOGOUT
}

/**
 * Transaction model for audit logging
 */
@Serializable
data class Transaction(
    val id: String,
    val type: TransactionType,
    val userId: String,
    val details: String, // JSON string with transaction details
    val timestamp: Long = System.currentTimeMillis(),
    val relatedEntityId: String? = null,
    val relatedEntityType: String? = null
)
