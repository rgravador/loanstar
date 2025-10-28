package com.example.loanstar.data.model

import kotlinx.serialization.Serializable

/**
 * Cashout request status
 */
enum class CashoutStatus {
    PENDING,
    APPROVED,
    REJECTED
}

/**
 * Cashout request model
 */
@Serializable
data class CashoutRequest(
    val id: String,
    val agentId: String,
    val amount: Double,
    val status: CashoutStatus = CashoutStatus.PENDING,
    val requestDate: Long = System.currentTimeMillis(),
    val approvalDate: Long? = null,
    val approvedByAdminId: String? = null,
    val rejectionReason: String? = null,
    val notes: String? = null
)
