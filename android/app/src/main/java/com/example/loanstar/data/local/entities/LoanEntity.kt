package com.example.loanstar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.loanstar.data.model.Loan
import com.example.loanstar.data.model.LoanStatus
import com.example.loanstar.data.model.PaymentFrequency
import kotlinx.serialization.Serializable

/**
 * Room entity for Loan table
 * Matches the schema defined in setup.sql
 */
@Serializable
@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByAgentId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["approvedByAdminId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["status"]),
        Index(value = ["createdByAgentId"]),
        Index(value = ["approvedByAdminId"])
    ]
)
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val accountId: String,
    val principalAmount: Double,
    val interestRate: Double, // Between 3.0 and 5.0
    val tenureMonths: Int, // Between 2 and 12
    val paymentFrequency: String, // WEEKLY, BI_MONTHLY, MONTHLY
    val status: String = "PENDING_APPROVAL", // PENDING_APPROVAL, APPROVED, ACTIVE, CLOSED, REJECTED
    val approvalDate: Long? = null,
    val amortizationSchedule: String? = null, // JSON array
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdByAgentId: String,
    val approvedByAdminId: String? = null,
    val rejectionReason: String? = null,
    val outstandingBalance: Double,
    val totalPaid: Double = 0.0
)

/**
 * Convert LoanEntity to domain Loan model
 */
fun LoanEntity.toDomain(): Loan {
    return Loan(
        id = id,
        accountId = accountId,
        principalAmount = principalAmount,
        interestRate = interestRate,
        tenureMonths = tenureMonths,
        paymentFrequency = PaymentFrequency.valueOf(paymentFrequency),
        status = LoanStatus.valueOf(status),
        approvalDate = approvalDate,
        amortizationSchedule = emptyList(), // Will be parsed from JSON
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdByAgentId = createdByAgentId,
        approvedByAdminId = approvedByAdminId,
        rejectionReason = rejectionReason,
        outstandingBalance = outstandingBalance,
        totalPaid = totalPaid
    )
}

/**
 * Convert domain Loan model to LoanEntity
 */
fun Loan.toEntity(): LoanEntity {
    return LoanEntity(
        id = id,
        accountId = accountId,
        principalAmount = principalAmount,
        interestRate = interestRate,
        tenureMonths = tenureMonths,
        paymentFrequency = paymentFrequency.name,
        status = status.name,
        approvalDate = approvalDate,
        amortizationSchedule = null, // Will be converted to JSON
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdByAgentId = createdByAgentId,
        approvedByAdminId = approvedByAdminId,
        rejectionReason = rejectionReason,
        outstandingBalance = outstandingBalance,
        totalPaid = totalPaid
    )
}
