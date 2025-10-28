package com.example.loanstar.ui.loans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loanstar.R
import com.example.loanstar.data.model.Account
import com.example.loanstar.data.model.AccountStatus
import com.example.loanstar.databinding.ItemAccountBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView adapter for displaying accounts list
 */
class AccountsAdapter(
    private val onAccountClick: (Account) -> Unit
) : ListAdapter<Account, AccountsAdapter.AccountViewHolder>(AccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AccountViewHolder(binding, onAccountClick)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AccountViewHolder(
        private val binding: ItemAccountBinding,
        private val onAccountClick: (Account) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(account: Account) {
            binding.apply {
                // Set account name
                tvAccountName.text = account.name

                // Set avatar initial
                tvAvatarInitial.text = account.name.firstOrNull()?.uppercase() ?: "A"

                // Set contact info
                tvPhone.text = account.contactInfo.phone

                // Set address
                tvAddress.text = account.address

                // Set status badge
                tvStatusBadge.text = account.status.name
                when (account.status) {
                    AccountStatus.ACTIVE -> {
                        tvStatusBadge.setBackgroundColor(
                            ContextCompat.getColor(root.context, R.color.status_approved_bg)
                        )
                        tvStatusBadge.setTextColor(
                            ContextCompat.getColor(root.context, R.color.status_approved_text)
                        )
                    }
                    AccountStatus.INACTIVE -> {
                        tvStatusBadge.setBackgroundColor(
                            ContextCompat.getColor(root.context, R.color.status_pending_bg)
                        )
                        tvStatusBadge.setTextColor(
                            ContextCompat.getColor(root.context, R.color.status_pending_text)
                        )
                    }
                    AccountStatus.SUSPENDED -> {
                        tvStatusBadge.setBackgroundColor(
                            ContextCompat.getColor(root.context, R.color.status_rejected_bg)
                        )
                        tvStatusBadge.setTextColor(
                            ContextCompat.getColor(root.context, R.color.status_rejected_text)
                        )
                    }
                }

                // Set creation date
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvCreatedDate.text = "Created: ${dateFormat.format(Date(account.creationDate))}"

                // Set click listener
                root.setOnClickListener {
                    onAccountClick(account)
                }
            }
        }
    }

    private class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
}
