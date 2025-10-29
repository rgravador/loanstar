import { defineStore } from 'pinia'
import type { Loan, LoanWithRelations, LoanForm, AmortizationScheduleItem } from '~/types'

interface LoansState {
  loans: LoanWithRelations[]
  selectedLoan: LoanWithRelations | null
  amortizationSchedule: AmortizationScheduleItem[]
  loading: boolean
}

export const useLoansStore = defineStore('loans', {
  state: (): LoansState => ({
    loans: [],
    selectedLoan: null,
    amortizationSchedule: [],
    loading: false
  }),

  getters: {
    pendingApprovalLoans: (state) => state.loans.filter(l => l.status === 'pending_approval'),

    activeLoans: (state) => state.loans.filter(l => l.status === 'active'),

    overdueLoans: (state) => state.loans.filter(l =>
      l.status === 'active' && l.total_penalties > 0
    ),

    loansByAccount: (state) => (accountId: string) => {
      return state.loans.filter(l => l.account_id === accountId)
    },

    totalOutstanding: (state) => {
      return state.activeLoans.reduce((sum, loan) => sum + loan.current_balance, 0)
    }
  },

  actions: {
    async fetchLoans() {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        let query = supabase
          .from('loans')
          .select(`
            *,
            account:accounts(*),
            creator:users_profile!loans_created_by_fkey(*),
            approver:users_profile!loans_approved_by_fkey(*),
            payments(*),
            penalties(*)
          `)
          .order('created_at', { ascending: false })

        // Filter by agent's accounts if not admin
        if (!authStore.isAdmin && authStore.user) {
          const { data: agentAccounts } = await supabase
            .from('accounts')
            .select('id')
            .eq('assigned_agent_id', authStore.user.id)

          const accountIds = agentAccounts?.map(a => a.id) || []
          query = query.in('account_id', accountIds)
        }

        const { data, error } = await query

        if (error) throw error

        this.loans = data || []
      } catch (error: any) {
        console.error('Error fetching loans:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchLoanById(id: string) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()

        const { data, error } = await supabase
          .from('loans')
          .select(`
            *,
            account:accounts(*),
            creator:users_profile!loans_created_by_fkey(*),
            approver:users_profile!loans_approved_by_fkey(*),
            payments(*),
            penalties(*)
          `)
          .eq('id', id)
          .single()

        if (error) throw error

        this.selectedLoan = data
        this.amortizationSchedule = data.amortization_schedule
        return data
      } catch (error: any) {
        console.error('Error fetching loan:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async createLoan(loanData: LoanForm) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()
        const { generateSchedule } = useAmortization()

        if (!authStore.user) throw new Error('User not authenticated')

        // Generate amortization schedule
        const startDate = loanData.start_date ? new Date(loanData.start_date) : new Date()
        const schedule = generateSchedule(
          loanData.principal_amount,
          loanData.interest_rate,
          loanData.tenure_months,
          loanData.payment_frequency,
          startDate
        )

        // Calculate end date from last payment in schedule
        const endDate = schedule[schedule.length - 1].due_date

        const { data, error } = await supabase
          .from('loans')
          .insert({
            account_id: loanData.account_id,
            principal_amount: loanData.principal_amount,
            interest_rate: loanData.interest_rate,
            tenure_months: loanData.tenure_months,
            payment_frequency: loanData.payment_frequency,
            amortization_schedule: schedule,
            current_balance: loanData.principal_amount,
            start_date: startDate.toISOString().split('T')[0],
            end_date: endDate,
            created_by: authStore.user.id,
            status: 'pending_approval'
          })
          .select()
          .single()

        if (error) throw error

        // Add transaction log
        await supabase.from('transactions').insert({
          type: 'create_loan',
          user_id: authStore.user.id,
          loan_id: data.id,
          account_id: loanData.account_id,
          details: {
            principal: loanData.principal_amount,
            interest_rate: loanData.interest_rate,
            tenure: loanData.tenure_months
          }
        })

        // Notify admin about new loan approval request
        const { data: admins } = await supabase
          .from('users_profile')
          .select('id')
          .eq('role', 'admin')

        if (admins) {
          const notifications = admins.map(admin => ({
            user_id: admin.id,
            loan_id: data.id,
            account_id: loanData.account_id,
            message: 'New loan pending approval',
            type: 'loan_approval' as const
          }))

          await supabase.from('notifications').insert(notifications)
        }

        await this.fetchLoans()
        return { success: true, data }
      } catch (error: any) {
        console.error('Error creating loan:', error)
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    async approveLoan(loanId: string) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.isAdmin) throw new Error('Only admins can approve loans')

        const { data, error } = await supabase
          .from('loans')
          .update({
            status: 'active',
            approval_date: new Date().toISOString(),
            approved_by: authStore.user!.id
          })
          .eq('id', loanId)
          .select()
          .single()

        if (error) throw error

        // Get loan details for notification
        const loan = await this.fetchLoanById(loanId)

        // Notify agent
        await supabase.from('notifications').insert({
          user_id: loan!.created_by,
          loan_id: loanId,
          message: 'Your loan has been approved',
          type: 'loan_approval'
        })

        // Add transaction log
        await supabase.from('transactions').insert({
          type: 'approve_loan',
          user_id: authStore.user!.id,
          loan_id: loanId,
          details: { approved_at: new Date().toISOString() }
        })

        await this.fetchLoans()
        return { success: true, data }
      } catch (error: any) {
        console.error('Error approving loan:', error)
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    async rejectLoan(loanId: string, reason: string) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.isAdmin) throw new Error('Only admins can reject loans')

        const { data, error } = await supabase
          .from('loans')
          .update({
            status: 'rejected',
            rejection_reason: reason,
            approved_by: authStore.user!.id
          })
          .eq('id', loanId)
          .select()
          .single()

        if (error) throw error

        // Get loan details for notification
        const loan = await this.fetchLoanById(loanId)

        // Notify agent
        await supabase.from('notifications').insert({
          user_id: loan!.created_by,
          loan_id: loanId,
          message: `Your loan has been rejected: ${reason}`,
          type: 'loan_rejection'
        })

        // Add transaction log
        await supabase.from('transactions').insert({
          type: 'reject_loan',
          user_id: authStore.user!.id,
          loan_id: loanId,
          details: { reason }
        })

        await this.fetchLoans()
        return { success: true, data }
      } catch (error: any) {
        console.error('Error rejecting loan:', error)
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    generateSchedulePreview(loanData: Partial<LoanForm>): AmortizationScheduleItem[] {
      const { generateSchedule } = useAmortization()

      if (!loanData.principal_amount || !loanData.interest_rate ||
          !loanData.tenure_months || !loanData.payment_frequency) {
        return []
      }

      return generateSchedule(
        loanData.principal_amount,
        loanData.interest_rate,
        loanData.tenure_months,
        loanData.payment_frequency,
        loanData.start_date ? new Date(loanData.start_date) : new Date()
      )
    },

    async exportSchedule(loanId: string, format: 'pdf' | 'csv') {
      // TODO: Implement export functionality
      console.log('Export schedule:', loanId, format)
    }
  }
})
