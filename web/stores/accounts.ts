import { defineStore } from 'pinia'
import type { Account, AccountWithRelations, AccountForm } from '~/types'

interface AccountsState {
  accounts: AccountWithRelations[]
  selectedAccount: AccountWithRelations | null
  loading: boolean
  filters: {
    search: string
    status: string | null
  }
}

export const useAccountsStore = defineStore('accounts', {
  state: (): AccountsState => ({
    accounts: [],
    selectedAccount: null,
    loading: false,
    filters: {
      search: '',
      status: null
    }
  }),

  getters: {
    filteredAccounts: (state) => {
      let filtered = state.accounts

      if (state.filters.search) {
        const search = state.filters.search.toLowerCase()
        filtered = filtered.filter(account =>
          account.name.toLowerCase().includes(search) ||
          account.contact_info.toLowerCase().includes(search)
        )
      }

      if (state.filters.status) {
        filtered = filtered.filter(account => account.status === state.filters.status)
      }

      return filtered
    },

    activeAccounts: (state) => state.accounts.filter(a => a.status === 'active'),

    accountsWithOverdueLoans: (state) => {
      return state.accounts.filter(account =>
        account.loans?.some(loan => loan.status === 'active' && loan.total_penalties > 0)
      )
    },

    accountsByAgent: (state) => (agentId: string) => {
      return state.accounts.filter(a => a.assigned_agent_id === agentId)
    }
  },

  actions: {
    async fetchAccounts() {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        let query = supabase
          .from('accounts')
          .select(`
            *,
            assigned_agent:users_profile!accounts_assigned_agent_id_fkey(*),
            loans(*)
          `)
          .order('created_at', { ascending: false })

        // Filter by agent if not admin
        if (!authStore.isAdmin && authStore.user) {
          query = query.eq('assigned_agent_id', authStore.user.id)
        }

        const { data, error } = await query

        if (error) throw error

        this.accounts = data || []
      } catch (error: any) {
        console.error('Error fetching accounts:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchAccountById(id: string) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()

        const { data, error } = await supabase
          .from('accounts')
          .select(`
            *,
            assigned_agent:users_profile!accounts_assigned_agent_id_fkey(*),
            loans(*)
          `)
          .eq('id', id)
          .single()

        if (error) throw error

        this.selectedAccount = data
        return data
      } catch (error: any) {
        console.error('Error fetching account:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async createAccount(accountData: AccountForm) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.user) throw new Error('User not authenticated')

        // Upload ID proof if provided
        let idProofUrl = null
        if (accountData.id_proof_file) {
          const fileName = `${Date.now()}_${accountData.id_proof_file.name}`
          const { data: uploadData, error: uploadError } = await supabase.storage
            .from('id-proofs')
            .upload(fileName, accountData.id_proof_file)

          if (uploadError) throw uploadError

          const { data: { publicUrl } } = supabase.storage
            .from('id-proofs')
            .getPublicUrl(uploadData.path)

          idProofUrl = publicUrl
        }

        const { data, error } = await supabase
          .from('accounts')
          .insert({
            name: accountData.name,
            contact_info: accountData.contact_info,
            address: accountData.address,
            id_proof_url: idProofUrl,
            assigned_agent_id: authStore.user.id,
            created_by: authStore.user.id
          })
          .select()
          .single()

        if (error) throw error

        // Add transaction log
        await supabase.from('transactions').insert({
          type: 'create_account',
          user_id: authStore.user.id,
          account_id: data.id,
          details: { account_name: accountData.name }
        })

        await this.fetchAccounts()
        return { success: true, data }
      } catch (error: any) {
        console.error('Error creating account:', error)
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    async updateAccount(id: string, accountData: Partial<AccountForm>) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.user) throw new Error('User not authenticated')

        const updateData: any = {}

        if (accountData.name) updateData.name = accountData.name
        if (accountData.contact_info) updateData.contact_info = accountData.contact_info
        if (accountData.address) updateData.address = accountData.address

        // Handle file upload if new file provided
        if (accountData.id_proof_file) {
          const fileName = `${Date.now()}_${accountData.id_proof_file.name}`
          const { data: uploadData, error: uploadError } = await supabase.storage
            .from('id-proofs')
            .upload(fileName, accountData.id_proof_file)

          if (uploadError) throw uploadError

          const { data: { publicUrl } } = supabase.storage
            .from('id-proofs')
            .getPublicUrl(uploadData.path)

          updateData.id_proof_url = publicUrl
        }

        const { data, error } = await supabase
          .from('accounts')
          .update(updateData)
          .eq('id', id)
          .select()
          .single()

        if (error) throw error

        // Add transaction log
        await supabase.from('transactions').insert({
          type: 'update_account',
          user_id: authStore.user.id,
          account_id: id,
          details: updateData
        })

        await this.fetchAccounts()
        return { success: true, data }
      } catch (error: any) {
        console.error('Error updating account:', error)
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    setFilters(filters: Partial<AccountsState['filters']>) {
      this.filters = { ...this.filters, ...filters }
    },

    clearFilters() {
      this.filters = {
        search: '',
        status: null
      }
    }
  }
})
