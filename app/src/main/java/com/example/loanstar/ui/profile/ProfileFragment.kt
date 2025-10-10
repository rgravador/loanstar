package com.example.loanstar.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.loanstar.R
import com.example.loanstar.data.model.ApprovalStatus
import com.example.loanstar.databinding.DialogEditProfileBinding
import com.example.loanstar.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Observe user data
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                // Update header
                binding.tvUsername.text = it.username
                binding.tvEmail.text = it.email
                binding.tvRoleBadge.text = it.role.name
                binding.tvAvatarInitial.text = viewModel.getUserInitials()

                // Update profile details
                val details = it.profileDetails
                binding.tvFullName.text = details?.fullName ?: "Not set"
                binding.tvPhone.text = details?.phone ?: "Not set"
                binding.tvAddress.text = details?.address ?: "Not set"
                binding.tvDateOfBirth.text = details?.dateOfBirth ?: "Not set"

                // Update account status
                binding.tvApprovalStatus.text = it.approvalStatus.name
                when (it.approvalStatus) {
                    ApprovalStatus.APPROVED -> {
                        binding.tvApprovalStatus.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.status_approved_bg)
                        )
                        binding.tvApprovalStatus.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.status_approved_text)
                        )
                    }
                    ApprovalStatus.PENDING -> {
                        binding.tvApprovalStatus.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.status_pending_bg)
                        )
                        binding.tvApprovalStatus.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.status_pending_text)
                        )
                    }
                    ApprovalStatus.REJECTED -> {
                        binding.tvApprovalStatus.setBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.status_rejected_bg)
                        )
                        binding.tvApprovalStatus.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.status_rejected_text)
                        )
                    }
                }

                binding.tvMemberSince.text = viewModel.getFormattedMemberSince()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.cardProfileHeader.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.cardProfileDetails.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.cardAccountStatus.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // Pre-fill with current data
        val currentUser = viewModel.user.value
        val profileDetails = currentUser?.profileDetails
        dialogBinding.etFullName.setText(profileDetails?.fullName ?: "")
        dialogBinding.etPhone.setText(profileDetails?.phone ?: "")
        dialogBinding.etAddress.setText(profileDetails?.address ?: "")
        dialogBinding.etDateOfBirth.setText(profileDetails?.dateOfBirth ?: "")

        // Setup button listeners
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val fullName = dialogBinding.etFullName.text?.toString()
            val phone = dialogBinding.etPhone.text?.toString()
            val address = dialogBinding.etAddress.text?.toString()
            val dateOfBirth = dialogBinding.etDateOfBirth.text?.toString()

            viewModel.updateProfile(fullName, phone, address, dateOfBirth)
            dialog.dismiss()
            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }

        // Make dialog background transparent for glassmorphism effect
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
