package com.example.loanstar.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.loanstar.R
import com.example.loanstar.databinding.FragmentRegisterBinding
import com.example.loanstar.data.model.UserRole
import dagger.hilt.android.AndroidEntryPoint

/**
 * Registration screen fragment
 */
@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }
                is AuthState.Authenticated -> {
                    binding.progressBar.visibility = View.GONE

                    // Show different message based on approval status
                    when (state.user.approvalStatus) {
                        com.example.loanstar.data.model.ApprovalStatus.PENDING -> {
                            Toast.makeText(
                                context,
                                "Registration successful! Your account is pending admin approval. You'll be able to login once approved.",
                                Toast.LENGTH_LONG
                            ).show()
                            // Navigate back to login
                            findNavController().navigateUp()
                        }
                        com.example.loanstar.data.model.ApprovalStatus.APPROVED -> {
                            Toast.makeText(context, "Registration successful as ${state.user.role}", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerFragment_to_navigation_dashboard)
                        }
                        else -> {
                            // This shouldn't happen during registration
                            findNavController().navigateUp()
                        }
                    }
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
                is AuthState.Unauthenticated -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // Validate
            if (password != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register as AGENT by default
            viewModel.register(email, password, username, UserRole.AGENT)
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
