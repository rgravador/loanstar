package com.example.loanstar.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Account model representing a borrower profile
 */
@Parcelize
@Serializable
data class Account(
    val id: String,
    val name: String,
    val contactInfo: ContactInfo,
    val address: String,
    val assignedAgentId: String,
    val idProofUrl: String? = null,
    val creationDate: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: AccountStatus = AccountStatus.ACTIVE
) : Parcelable

@Parcelize
@Serializable
data class ContactInfo(
    val phone: String,
    val email: String? = null,
    val alternatePhone: String? = null
) : Parcelable

@Parcelize
enum class AccountStatus : Parcelable {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
