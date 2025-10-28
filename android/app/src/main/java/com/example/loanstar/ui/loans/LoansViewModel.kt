package com.example.loanstar.ui.loans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.loanstar.data.model.Account
import com.example.loanstar.data.model.AccountStatus
import com.example.loanstar.data.model.ContactInfo
import java.util.UUID

class LoansViewModel : ViewModel() {

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _filteredAccounts = MutableLiveData<List<Account>>()
    val filteredAccounts: LiveData<List<Account>> = _filteredAccounts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var allAccounts: List<Account> = emptyList()

    init {
        loadAccounts()
    }

    /**
     * Load accounts from database
     * TODO: Replace with actual Supabase API call when backend is implemented
     */
    fun loadAccounts() {
        _isLoading.value = true

        // Mock data - replace with actual API call
        val mockAccounts = listOf(
            Account(
                id = UUID.randomUUID().toString(),
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
                id = UUID.randomUUID().toString(),
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
                id = UUID.randomUUID().toString(),
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
                id = UUID.randomUUID().toString(),
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
                id = UUID.randomUUID().toString(),
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
                id = UUID.randomUUID().toString(),
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
                id = UUID.randomUUID().toString(),
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
                id = UUID.randomUUID().toString(),
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

        allAccounts = mockAccounts
        _accounts.value = mockAccounts
        _filteredAccounts.value = mockAccounts
        _isLoading.value = false
    }

    /**
     * Search accounts by name, phone, or address
     */
    fun searchAccounts(query: String) {
        if (query.isBlank()) {
            _filteredAccounts.value = allAccounts
            return
        }

        val lowercaseQuery = query.lowercase()
        val filtered = allAccounts.filter { account ->
            account.name.lowercase().contains(lowercaseQuery) ||
            account.contactInfo.phone.contains(lowercaseQuery) ||
            account.address.lowercase().contains(lowercaseQuery) ||
            account.contactInfo.email?.lowercase()?.contains(lowercaseQuery) == true
        }

        _filteredAccounts.value = filtered
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
