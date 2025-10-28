package com.example.loanstar.ui.loans

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loanstar.databinding.FragmentLoansBinding

class LoansFragment : Fragment() {

    private var _binding: FragmentLoansBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoansViewModel
    private lateinit var accountsAdapter: AccountsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[LoansViewModel::class.java]
        _binding = FragmentLoansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchField()
        setupClickListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        accountsAdapter = AccountsAdapter { account ->
            // Navigate to account details with account ID
            val action = LoansFragmentDirections
                .actionNavigationLoansToAccountDetailsFragment(account.id)
            findNavController().navigate(action)
        }

        binding.rvAccounts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = accountsAdapter
        }
    }

    private fun setupSearchField() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                viewModel.searchAccounts(query)

                // Show/hide clear button
                binding.ivClearSearch.visibility = if (query.isEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClickListeners() {
        // Clear search button
        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        // Create new account FAB
        binding.fabCreateAccount.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Create new account coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupObservers() {
        // Observe filtered accounts
        viewModel.filteredAccounts.observe(viewLifecycleOwner) { accounts ->
            accountsAdapter.submitList(accounts)

            // Show/hide empty state
            if (accounts.isEmpty()) {
                binding.rvAccounts.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.rvAccounts.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvAccounts.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
