package com.example.loanstar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.loanstar.data.model.Earnings

/**
 * Room entity for Earnings table
 * Matches the schema defined in setup.sql
 */
@Entity(
    tableName = "earnings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["agentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["agentId"], unique = true)
    ]
)
data class EarningsEntity(
    @PrimaryKey
    val id: String,
    val agentId: String,
    val totalEarnings: Double = 0.0,
    val collectibleEarnings: Double = 0.0,
    val cashedOutAmount: Double = 0.0,
    val commissionPercentage: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val earningsBreakdown: String? = null // JSON array
)

/**
 * Convert EarningsEntity to domain Earnings model
 */
fun EarningsEntity.toDomain(): Earnings {
    return Earnings(
        id = id,
        agentId = agentId,
        totalEarnings = totalEarnings,
        collectibleEarnings = collectibleEarnings,
        cashedOutAmount = cashedOutAmount,
        commissionPercentage = commissionPercentage,
        lastUpdated = lastUpdated,
        earningsBreakdown = emptyList() // Will be parsed from JSON
    )
}

/**
 * Convert domain Earnings model to EarningsEntity
 */
fun Earnings.toEntity(): EarningsEntity {
    return EarningsEntity(
        id = id,
        agentId = agentId,
        totalEarnings = totalEarnings,
        collectibleEarnings = collectibleEarnings,
        cashedOutAmount = cashedOutAmount,
        commissionPercentage = commissionPercentage,
        lastUpdated = lastUpdated,
        earningsBreakdown = null // Will be converted to JSON
    )
}
