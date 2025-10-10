package com.example.loanstar.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loanstar.R
import com.example.loanstar.data.model.ApprovalStatus
import com.example.loanstar.data.model.User
import com.example.loanstar.databinding.ItemAgentBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter for displaying agents list in the admin dashboard
 */
class AgentAdapter(
    private val onAgentClick: (User) -> Unit
) : ListAdapter<AgentItemData, AgentAdapter.AgentViewHolder>(AgentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgentViewHolder {
        val binding = ItemAgentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AgentViewHolder(binding, onAgentClick)
    }

    override fun onBindViewHolder(holder: AgentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AgentViewHolder(
        private val binding: ItemAgentBinding,
        private val onAgentClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(agentData: AgentItemData) {
            val user = agentData.user

            // Set agent name
            binding.tvAgentName.text = user.username

            // Set agent email
            binding.tvAgentEmail.text = user.email

            // Set avatar (first letter of username)
            binding.tvAvatar.text = user.username.firstOrNull()?.uppercase() ?: "A"

            // Set status badge
            when (user.approvalStatus) {
                ApprovalStatus.APPROVED -> {
                    binding.tvStatus.text = "APPROVED"
                    binding.tvStatus.setBackgroundColor(
                        itemView.context.getColor(R.color.status_approved_bg)
                    )
                    binding.tvStatus.setTextColor(
                        itemView.context.getColor(R.color.status_approved_text)
                    )
                }
                ApprovalStatus.PENDING -> {
                    binding.tvStatus.text = "PENDING"
                    binding.tvStatus.setBackgroundColor(
                        itemView.context.getColor(R.color.status_pending_bg)
                    )
                    binding.tvStatus.setTextColor(
                        itemView.context.getColor(R.color.status_pending_text)
                    )
                }
                ApprovalStatus.REJECTED -> {
                    binding.tvStatus.text = "REJECTED"
                    binding.tvStatus.setBackgroundColor(
                        itemView.context.getColor(R.color.status_rejected_bg)
                    )
                    binding.tvStatus.setTextColor(
                        itemView.context.getColor(R.color.status_rejected_text)
                    )
                }
            }

            // Show/hide stats based on approval status
            if (user.approvalStatus == ApprovalStatus.PENDING) {
                // Hide stats for pending agents
                binding.statsContainer.visibility = View.GONE
            } else {
                // Show stats for approved/rejected agents
                binding.statsContainer.visibility = View.VISIBLE

                // Set accounts count
                binding.tvAccountsCount.text = agentData.accountsCount.toString()

                // Set earnings
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                currencyFormat.maximumFractionDigits = 0
                binding.tvEarnings.text = currencyFormat.format(agentData.totalEarnings)
            }

            // Set click listener for the card
            binding.root.setOnClickListener {
                onAgentClick(user)
            }
        }
    }

    class AgentDiffCallback : DiffUtil.ItemCallback<AgentItemData>() {
        override fun areItemsTheSame(oldItem: AgentItemData, newItem: AgentItemData): Boolean {
            return oldItem.user.id == newItem.user.id
        }

        override fun areContentsTheSame(oldItem: AgentItemData, newItem: AgentItemData): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Data class representing an agent item with additional statistics
 */
data class AgentItemData(
    val user: User,
    val accountsCount: Int = 0,
    val totalEarnings: Double = 0.0
)
