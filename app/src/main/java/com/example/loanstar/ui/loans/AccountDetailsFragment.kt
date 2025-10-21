package com.example.loanstar.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.loanstar.R
import com.example.loanstar.data.model.AccountStatus
import com.example.loanstar.databinding.FragmentAccountDetailsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment to display detailed information about a specific account
 */
class AccountDetailsFragment : Fragment() {

    private var _binding: FragmentAccountDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AccountDetailsViewModel
    private val args: AccountDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AccountDetailsViewModel::class.java]
        _binding = FragmentAccountDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()

        // Load account data
        viewModel.loadAccount(args.accountId)
    }

    private fun setupObservers() {
        // Observe account data
        viewModel.account.observe(viewLifecycleOwner) { account ->
            account?.let {
                setupUI(it)
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupUI(account: com.example.loanstar.data.model.Account) {
        binding.apply {
            // Set avatar initial
            tvAvatarInitial.text = account.name.firstOrNull()?.uppercase() ?: "A"

            // Set account name
            tvAccountName.text = account.name

            // Set status badge
            tvStatusBadge.text = account.status.name
            when (account.status) {
                AccountStatus.ACTIVE -> {
                    tvStatusBadge.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.status_approved_bg)
                    )
                    tvStatusBadge.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.status_approved_text)
                    )
                }
                AccountStatus.INACTIVE -> {
                    tvStatusBadge.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.status_pending_bg)
                    )
                    tvStatusBadge.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.status_pending_text)
                    )
                }
                AccountStatus.SUSPENDED -> {
                    tvStatusBadge.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.status_rejected_bg)
                    )
                    tvStatusBadge.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.status_rejected_text)
                    )
                }
            }

            // Set contact information
            tvPhone.text = account.contactInfo.phone

            // Set email if available
            if (!account.contactInfo.email.isNullOrBlank()) {
                layoutEmail.visibility = View.VISIBLE
                tvEmail.text = account.contactInfo.email
            } else {
                layoutEmail.visibility = View.GONE
            }

            // Set alternate phone if available
            if (!account.contactInfo.alternatePhone.isNullOrBlank()) {
                layoutAlternatePhone.visibility = View.VISIBLE
                tvAlternatePhone.text = account.contactInfo.alternatePhone
            } else {
                layoutAlternatePhone.visibility = View.GONE
            }

            // Set address
            tvAddress.text = account.address

            // Set dates
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvCreationDate.text = dateFormat.format(Date(account.creationDate))
            tvUpdatedDate.text = dateFormat.format(Date(account.updatedAt))

            // Set account ID
            tvAccountId.text = account.id.take(12) + "..."
        }
    }

    private fun setupClickListeners() {
        binding.btnEditAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Edit account coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnViewLoans.setOnClickListener {
            Toast.makeText(requireContext(), "View loans coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
