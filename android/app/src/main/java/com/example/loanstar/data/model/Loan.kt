package com.example.loanstar.data.model

import kotlinx.serialization.Serializable

/**
 * Loan status
 */
enum class LoanStatus {
    PENDING_APPROVAL,
    APPROVED,
    ACTIVE,
    CLOSED,
    REJECTED
}

/**
 * Payment frequency options
 */
enum class PaymentFrequency {
    WEEKLY,       // Every 7 days
    BI_MONTHLY,   // Every 15 days
    MONTHLY       // Every 30 days
}

/**
 * Loan model representing a loan
 */
@Serializable
data class Loan(
    val id: String,
    val accountId: String,
    val principalAmount: Double,
    val interestRate: Double, // 3-5% per month
    val tenureMonths: Int,    // 2-12 months
    val paymentFrequency: PaymentFrequency,
    val status: LoanStatus = LoanStatus.PENDING_APPROVAL,
    val approvalDate: Long? = null,
    val amortizationSchedule: List<AmortizationEntry> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdByAgentId: String,
    val approvedByAdminId: String? = null,
    val rejectionReason: String? = null,
    val outstandingBalance: Double = principalAmount,
    val totalPaid: Double = 0.0
)

/**
 * Amortization schedule entry
 */
@Serializable
data class AmortizationEntry(
    val paymentNumber: Int,
    val dueDate: Long,
    val principalDue: Double,
    val interestDue: Double,
    val totalDue: Double,
    val remainingBalance: Double,
    val penalty: Double = 0.0,
    val isPaid: Boolean = false,
    val paidAmount: Double = 0.0,
    val paidDate: Long? = null
)
