package com.example.loanstar.ui.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loanstar.R
import com.example.loanstar.data.model.ApprovalStatus
import com.example.loanstar.data.model.User
import com.example.loanstar.databinding.DialogAgentApprovalBinding
import com.example.loanstar.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

/**
 * Fragment displaying the Admin Dashboard
 * Shows statistics cards and list of agents
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var agentAdapter: AgentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    /**
     * Setup RecyclerView for agents list
     */
    private fun setupRecyclerView() {
        agentAdapter = AgentAdapter(
            onAgentClick = { agent ->
                // Show approval dialog for pending agents
                if (agent.approvalStatus == ApprovalStatus.PENDING) {
                    showAgentApprovalDialog(agent)
                } else {
                    // TODO: Navigate to agent details for approved/rejected agents
                    Toast.makeText(
                        context,
                        "Clicked on ${agent.username}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        binding.rvAgents.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = agentAdapter
        }
    }

    /**
     * Show agent approval dialog with agent info and approve/reject actions
     */
    private fun showAgentApprovalDialog(agent: User) {
        val dialogBinding = DialogAgentApprovalBinding.inflate(layoutInflater)

        // Create dialog with transparent background for rounded corners
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .setBackground(resources.getDrawable(android.R.color.transparent, null))
            .create()

        // Set transparent window background for proper rounded corners
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set agent information
        with(dialogBinding) {
            // Set avatar
            tvAvatar.text = agent.username.firstOrNull()?.uppercase() ?: "A"

            // Set name
            tvAgentName.text = agent.username

            // Set email
            tvAgentEmail.text = agent.email

            // Set status badge
            when (agent.approvalStatus) {
                ApprovalStatus.PENDING -> {
                    tvStatus.text = "PENDING APPROVAL"
                    tvStatus.setBackgroundColor(requireContext().getColor(R.color.status_pending_bg))
                    tvStatus.setTextColor(requireContext().getColor(R.color.status_pending_text))
                }
                ApprovalStatus.APPROVED -> {
                    tvStatus.text = "APPROVED"
                    tvStatus.setBackgroundColor(requireContext().getColor(R.color.status_approved_bg))
                    tvStatus.setTextColor(requireContext().getColor(R.color.status_approved_text))
                }
                ApprovalStatus.REJECTED -> {
                    tvStatus.text = "REJECTED"
                    tvStatus.setBackgroundColor(requireContext().getColor(R.color.status_rejected_bg))
                    tvStatus.setTextColor(requireContext().getColor(R.color.status_rejected_text))
                }
            }

            // Set up approve button
            btnApprove.setOnClickListener {
                // Add haptic feedback
                btnApprove.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)

                viewModel.approveAgent(agent.id)
                dialog.dismiss()

                Toast.makeText(
                    context,
                    "✓ ${agent.username} approved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Set up reject button
            btnReject.setOnClickListener {
                // Add haptic feedback
                btnReject.performHapticFeedback(android.view.HapticFeedbackConstants.REJECT)

                dialog.dismiss()
                showRejectReasonDialog(agent)
            }
        }

        dialog.show()

        // Animate dialog entrance
        dialogBinding.contentCard.alpha = 0f
        dialogBinding.contentCard.scaleX = 0.9f
        dialogBinding.contentCard.scaleY = 0.9f
        dialogBinding.contentCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    /**
     * Show dialog to input rejection reason
     */
    private fun showRejectReasonDialog(agent: User) {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Enter reason for rejection"
            setPadding(48, 32, 48, 32)
            textSize = 16f
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reject Agent")
            .setMessage("Please provide a reason for rejecting ${agent.username}:")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isNotEmpty()) {
                    viewModel.rejectAgent(agent.id, reason)
                    Toast.makeText(
                        context,
                        "✗ ${agent.username} rejected",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Please provide a rejection reason",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Setup observers for ViewModel LiveData
     */
    private fun setupObservers() {
        // Observe dashboard state
        viewModel.dashboardState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DashboardState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvAgents.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.GONE
                }
                is DashboardState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvAgents.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                }
                is DashboardState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvAgents.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = state.message
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observe statistics
        viewModel.statistics.observe(viewLifecycleOwner) { stats ->
            updateStatistics(stats)
        }

        // Observe agents list
        viewModel.agents.observe(viewLifecycleOwner) { agents ->
            if (agents.isEmpty()) {
                binding.rvAgents.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.tvEmptyState.text = "No agents found"
            } else {
                binding.rvAgents.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
                agentAdapter.submitList(agents)
            }
        }
    }

    /**
     * Update statistics cards with data
     */
    private fun updateStatistics(stats: DashboardStatistics) {
        // Format currency
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        currencyFormat.maximumFractionDigits = 0

        // Update cards
        binding.tvAgentsCount.text = stats.agentsCount.toString()
        binding.tvReleasesAmount.text = currencyFormat.format(stats.totalLoanReleases)
        binding.tvCollectablesAmount.text = currencyFormat.format(stats.totalCollectables)
        binding.tvAccountsCount.text = stats.activeAccountsCount.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
