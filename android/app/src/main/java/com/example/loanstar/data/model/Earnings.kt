package com.example.loanstar.data.model

import kotlinx.serialization.Serializable

/**
 * Earnings model representing agent earnings
 */
@Serializable
data class Earnings(
    val id: String,
    val agentId: String,
    val totalEarnings: Double = 0.0,
    val collectibleEarnings: Double = 0.0,
    val cashedOutAmount: Double = 0.0,
    val commissionPercentage: Double = 0.0, // Set by admin
    val lastUpdated: Long = System.currentTimeMillis(),
    val earningsBreakdown: List<EarningEntry> = emptyList()
)

/**
 * Individual earning entry for tracking
 */
@Serializable
data class EarningEntry(
    val id: String,
    val loanId: String,
    val paymentId: String,
    val interestAmount: Double,
    val commissionAmount: Double,
    val earnedDate: Long = System.currentTimeMillis(),
    val isCashedOut: Boolean = false
)
