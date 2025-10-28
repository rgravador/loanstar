package com.example.loanstar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.loanstar.data.model.Payment
import com.example.loanstar.data.model.PaymentStatus
import com.example.loanstar.data.model.PaymentType
import kotlinx.serialization.Serializable

/**
 * Room entity for Payment table
 * Matches the schema defined in setup.sql
 */
@Serializable
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["receivedByAgentId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["loanId"]),
        Index(value = ["paymentDate"]),
        Index(value = ["receivedByAgentId"])
    ]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String,
    val loanId: String,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val type: String = "REGULAR", // REGULAR, PENALTY, PARTIAL, FULL
    val status: String = "RECEIVED", // RECEIVED, PENDING, FAILED
    val receivedByAgentId: String,
    val notes: String? = null,
    val appliedToPrincipal: Double = 0.0,
    val appliedToInterest: Double = 0.0,
    val appliedToPenalty: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Convert PaymentEntity to domain Payment model
 */
fun PaymentEntity.toDomain(): Payment {
    return Payment(
        id = id,
        loanId = loanId,
        amount = amount,
        paymentDate = paymentDate,
        type = PaymentType.valueOf(type),
        status = PaymentStatus.valueOf(status),
        receivedByAgentId = receivedByAgentId,
        notes = notes,
        appliedToPrincipal = appliedToPrincipal,
        appliedToInterest = appliedToInterest,
        appliedToPenalty = appliedToPenalty,
        createdAt = createdAt
    )
}

/**
 * Convert domain Payment model to PaymentEntity
 */
fun Payment.toEntity(): PaymentEntity {
    return PaymentEntity(
        id = id,
        loanId = loanId,
        amount = amount,
        paymentDate = paymentDate,
        type = type.name,
        status = status.name,
        receivedByAgentId = receivedByAgentId,
        notes = notes,
        appliedToPrincipal = appliedToPrincipal,
        appliedToInterest = appliedToInterest,
        appliedToPenalty = appliedToPenalty,
        createdAt = createdAt
    )
}
