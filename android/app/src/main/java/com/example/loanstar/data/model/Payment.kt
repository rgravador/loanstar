package com.example.loanstar.data.model

import kotlinx.serialization.Serializable

/**
 * Payment type
 */
enum class PaymentType {
    REGULAR,
    PENALTY,
    PARTIAL,
    FULL
}

/**
 * Payment status
 */
enum class PaymentStatus {
    RECEIVED,
    PENDING,
    FAILED
}

/**
 * Payment model representing a loan payment
 */
@Serializable
data class Payment(
    val id: String,
    val loanId: String,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val type: PaymentType = PaymentType.REGULAR,
    val status: PaymentStatus = PaymentStatus.RECEIVED,
    val receivedByAgentId: String,
    val notes: String? = null,
    val appliedToPrincipal: Double = 0.0,
    val appliedToInterest: Double = 0.0,
    val appliedToPenalty: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
