package com.example.loanstar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.loanstar.data.model.CashoutRequest
import com.example.loanstar.data.model.CashoutStatus

/**
 * Room entity for CashoutRequest table
 * Matches the schema defined in setup.sql
 */
@Entity(
    tableName = "cashout_requests",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["agentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["approvedByAdminId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["agentId"]),
        Index(value = ["status"]),
        Index(value = ["requestDate"]),
        Index(value = ["approvedByAdminId"])
    ]
)
data class CashoutRequestEntity(
    @PrimaryKey
    val id: String,
    val agentId: String,
    val amount: Double,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val requestDate: Long = System.currentTimeMillis(),
    val approvalDate: Long? = null,
    val approvedByAdminId: String? = null,
    val rejectionReason: String? = null,
    val notes: String? = null
)

/**
 * Convert CashoutRequestEntity to domain CashoutRequest model
 */
fun CashoutRequestEntity.toDomain(): CashoutRequest {
    return CashoutRequest(
        id = id,
        agentId = agentId,
        amount = amount,
        status = CashoutStatus.valueOf(status),
        requestDate = requestDate,
        approvalDate = approvalDate,
        approvedByAdminId = approvedByAdminId,
        rejectionReason = rejectionReason,
        notes = notes
    )
}

/**
 * Convert domain CashoutRequest model to CashoutRequestEntity
 */
fun CashoutRequest.toEntity(): CashoutRequestEntity {
    return CashoutRequestEntity(
        id = id,
        agentId = agentId,
        amount = amount,
        status = status.name,
        requestDate = requestDate,
        approvalDate = approvalDate,
        approvedByAdminId = approvedByAdminId,
        rejectionReason = rejectionReason,
        notes = notes
    )
}
