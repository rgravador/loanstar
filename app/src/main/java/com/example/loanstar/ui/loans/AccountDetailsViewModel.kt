package com.example.loanstar.ui.loans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.loanstar.data.model.Account
import com.example.loanstar.data.model.AccountStatus
import com.example.loanstar.data.model.ContactInfo
import java.util.UUID

/**
 * ViewModel for Account Details
 */
class AccountDetailsViewModel : ViewModel() {

    private val _account = MutableLiveData<Account?>()
    val account: LiveData<Account?> = _account

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Load account by ID
     * TODO: Replace with actual Supabase API call when backend is implemented
     */
    fun loadAccount(accountId: String) {
        _isLoading.value = true

        // Mock data - replace with actual API call
        // In a real app, this would fetch from database/API
        val mockAccounts = getMockAccounts()
        val account = mockAccounts.find { it.id == accountId }

        if (account != null) {
            _account.value = account
        } else {
            _errorMessage.value = "Account not found"
        }

        _isLoading.value = false
    }

    /**
     * Get mock accounts (same as in LoansViewModel)
     * TODO: Replace with actual repository call
     */
    private fun getMockAccounts(): List<Account> {
        return listOf(
            Account(
                id = "account_1",
                name = "John Smith",
                contactInfo = ContactInfo(
                    phone = "+1 (555) 123-4567",
                    email = "john.smith@email.com",
                    alternatePhone = "+1 (555) 987-6543"
                ),
                address = "123 Main Street, Springfield, IL 62701",
                assignedAgentId = "agent123",
                creationDate = System.currentTimeMillis() - 86400000 * 30,
                status = AccountStatus.ACTIVE
            ),
            Account(
                id = "account_2",
                name = "Sarah Johnson",
                contactInfo = ContactInfo(
                    phone = "+1 (555) 234-5678",
                    email = "sarah.j@email.com"
                ),
                address = "456 Oak Avenue, Chicago, IL 60601",
                assignedAgentId = "agent123",
                creationDate = System.currentTimeMillis() - 86400000 * 15,
                status = AccountStatus.ACTIVE
            ),
            Account(
                id = "account_3",
                name = "Michael Davis",
                contactInfo = ContactInfo(
                    phone = "+1 (555) 345-6789"
                ),
                address = "789 Pine Road, Austin, TX 78701",
                assignedAgentId = "agent123",
                creationDate = System.currentTimeMillis() - 86400000 * 45,
                status = AccountStatus.ACTIVE
            ),
            Account(
                id = "account_4",
                name = "Emily Brown",
                contactInfo = ContactInfo(
                    phone = "+1 (555) 456-7890",
                    email = "emily.brown@email.com",
                    alternatePhone = "+1 (555) 111-2222"
                ),
                address = "321 Elm Street, Boston, MA 02101",
                assignedAgentId = "agent123",
                creationDate = System.currentTimeMillis() - 86400000 * 60,
                status = AccountStatus.INACTIVE
            ),
            Account(
                id = "account_5",
                name = "David Wilson",
                contactInfo = ContactInfo(
                    phone = "+1 (555) 567-8901",
                    email = "d.wilson@email.com"
                ),
                address = "654 Maple Drive, Seattle, WA 98101",
                assignedAgentId = "agent123",
                creationDate = System.currentTimeMillis() - 86400000 * 90,
                status = AccountStatus.ACTIVE
            ),
            Account(
                id = "account_6",
                name = "Jessica Martinez",
                contactInfo = ContactInfo(
                    phone = "+1 (555) 678-9012"
                ),
                address = "987 Cedar Lane, Miami, FL 33101",
                assignedAgentId = "agent123",
                creationDate = System.currentTimeMillis() - 86400000 * 20,
                status = AccountStatus.ACTIVE
            ),
            Account(
                id = "account_7",
                name = "Robert Taylor",
                contactInfo = ContactInfo(
                    phone = "+1 (555) 789-0123",
                    email = "robert.t@email.com"
                ),
                address = "246 Birch Avenue, Denver, CO 80201",
                assignedAgentId = "agent123",
                creationDate = System.currentTimeMillis() - 86400000 * 10,
                status = AccountStatus.SUSPENDED
            ),
            Account(
                id = "account_8",
                name = "Lisa Anderson",
                contactInfo = ContactInfo(
                    phone = "+1 (555) 890-1234",
                    email = "lisa.anderson@email.com",
                    alternatePhone = "+1 (555) 333-4444"
                ),
                address = "135 Spruce Street, Portland, OR 97201",
                assignedAgentId = "agent123",
                creationDate = System.currentTimeMillis() - 86400000 * 5,
                status = AccountStatus.ACTIVE
            )
        )
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
