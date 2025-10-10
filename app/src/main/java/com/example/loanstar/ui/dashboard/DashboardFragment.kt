package com.example.loanstar.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loanstar.R
import com.example.loanstar.databinding.FragmentDashboardBinding
import com.example.loanstar.ui.home.AgentAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for Admin Dashboard
 * Shows agents with status filtering
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var agentAdapter: AgentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterChips()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        agentAdapter = AgentAdapter(
            onAgentClick = { agent ->
                // Show agent details or approval dialog when card is clicked
                // Show approval dialog for pending agents
                if (agent.approvalStatus == com.example.loanstar.data.model.ApprovalStatus.PENDING) {
                    showApprovalDialog(agent.id, agent.username)
                } else {
                    // TODO: Navigate to agent details for approved/rejected agents
                    Toast.makeText(
                        requireContext(),
                        "Clicked on ${agent.username}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        binding.rvAgents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = agentAdapter
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val filter = when (checkedIds[0]) {
                R.id.chip_all -> StatusFilter.ALL
                R.id.chip_pending -> StatusFilter.PENDING
                R.id.chip_approved -> StatusFilter.APPROVED
                R.id.chip_rejected -> StatusFilter.REJECTED
                else -> StatusFilter.PENDING
            }

            viewModel.setFilter(filter)
        }
    }

    private fun observeViewModel() {
        // Observe agents
        viewModel.agents.observe(viewLifecycleOwner) { agents ->
            agentAdapter.submitList(agents)

            // Update count badge
            binding.tvAgentCount.text = agents.size.toString()

            // Show/hide empty state
            if (agents.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.rvAgents.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.rvAgents.visibility = View.VISIBLE
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressLoading.visibility = View.VISIBLE
                binding.rvAgents.visibility = View.GONE
                binding.emptyState.visibility = View.GONE
                binding.tvErrorMessage.visibility = View.GONE
            } else {
                binding.progressLoading.visibility = View.GONE
            }
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.tvErrorMessage.text = errorMessage
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.rvAgents.visibility = View.GONE
                binding.emptyState.visibility = View.GONE

                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            } else {
                binding.tvErrorMessage.visibility = View.GONE
            }
        }
    }

    private fun showApprovalDialog(agentId: String, agentUsername: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Agent Actions")
            .setMessage("What would you like to do with $agentUsername?")
            .setPositiveButton("Approve") { _, _ ->
                viewModel.approveAgent(agentId)
                Toast.makeText(
                    requireContext(),
                    "$agentUsername approved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Reject") { _, _ ->
                showRejectionDialog(agentId, agentUsername)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showRejectionDialog(agentId: String, agentUsername: String) {
        val input = android.widget.EditText(requireContext())
        input.hint = "Enter rejection reason"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reject Agent")
            .setMessage("Please provide a reason for rejecting $agentUsername:")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isNotEmpty()) {
                    viewModel.rejectAgent(agentId, reason)
                    Toast.makeText(
                        requireContext(),
                        "$agentUsername rejected",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please provide a rejection reason",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
