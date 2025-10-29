import { defineStore } from 'pinia'
import type { Payment, PaymentWithRelations, PaymentForm } from '~/types'

interface PaymentsState {
  payments: PaymentWithRelations[]
  loading: boolean
}

export const usePaymentsStore = defineStore('payments', {
  state: (): PaymentsState => ({
    payments: [],
    loading: false
  }),

  getters: {
    paymentsByLoan: (state) => (loanId: string) => {
      return state.payments.filter(p => p.loan_id === loanId)
    },

    recentPayments: (state) => {
      return state.payments.slice(0, 10)
    },

    totalPaymentsToday: (state) => {
      const today = new Date().toISOString().split('T')[0]
      return state.payments
        .filter(p => p.payment_date === today)
        .reduce((sum, p) => sum + p.amount, 0)
    }
  },

  actions: {
    async fetchPayments() {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        let query = supabase
          .from('payments')
          .select(`
            *,
            loan:loans(
              *,
              account:accounts(*)
            ),
            receiver:users_profile!payments_received_by_fkey(*)
          `)
          .order('created_at', { ascending: false })

        // Filter by agent's loans if not admin
        if (!authStore.isAdmin && authStore.user) {
          const { data: agentAccounts } = await supabase
            .from('accounts')
            .select('id')
            .eq('assigned_agent_id', authStore.user.id)

          const accountIds = agentAccounts?.map(a => a.id) || []

          const { data: agentLoans } = await supabase
            .from('loans')
            .select('id')
            .in('account_id', accountIds)

          const loanIds = agentLoans?.map(l => l.id) || []
          query = query.in('loan_id', loanIds)
        }

        const { data, error } = await query

        if (error) throw error

        this.payments = data || []
      } catch (error: any) {
        console.error('Error fetching payments:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async recordPayment(paymentData: PaymentForm) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()
        const { calculateCommission } = useCommissions()

        if (!authStore.user) throw new Error('User not authenticated')

        // Get loan details
        const { data: loan, error: loanError } = await supabase
          .from('loans')
          .select('*, account:accounts(*)')
          .eq('id', paymentData.loan_id)
          .single()

        if (loanError) throw loanError

        // Calculate payment application
        const paymentAmount = paymentData.amount
        let remainingAmount = paymentAmount
        let appliedToPenalty = 0
        let appliedToInterest = 0
        let appliedToPrincipal = 0

        // Apply to penalties first
        if (loan.total_penalties > 0) {
          appliedToPenalty = Math.min(remainingAmount, loan.total_penalties)
          remainingAmount -= appliedToPenalty
        }

        // Get next unpaid amount from amortization schedule
        if (remainingAmount > 0) {
          // Find next payment due
          const schedule = loan.amortization_schedule as any[]
          const totalPaid = loan.total_paid

          // Calculate how much interest and principal are due
          let accumulatedTotal = 0
          for (const item of schedule) {
            accumulatedTotal += item.total_due
            if (accumulatedTotal > totalPaid) {
              // This is the next payment
              const dueForThisPayment = accumulatedTotal - totalPaid
              const portionToApply = Math.min(remainingAmount, dueForThisPayment)

              // Calculate proportion
              const interestProportion = item.interest_due / item.total_due
              appliedToInterest = portionToApply * interestProportion
              appliedToPrincipal = portionToApply * (1 - interestProportion)

              remainingAmount -= portionToApply
              break
            }
          }

          // If still remaining, apply all to principal
          if (remainingAmount > 0) {
            appliedToPrincipal += remainingAmount
          }
        }

        // Round values
        appliedToPenalty = Math.round(appliedToPenalty * 100) / 100
        appliedToInterest = Math.round(appliedToInterest * 100) / 100
        appliedToPrincipal = Math.round(appliedToPrincipal * 100) / 100

        // Create payment record
        const { data: payment, error: paymentError } = await supabase
          .from('payments')
          .insert({
            loan_id: paymentData.loan_id,
            amount: paymentAmount,
            payment_date: paymentData.payment_date,
            type: 'regular',
            status: 'received',
            applied_to_principal: appliedToPrincipal,
            applied_to_interest: appliedToInterest,
            applied_to_penalty: appliedToPenalty,
            received_by: authStore.user.id,
            notes: paymentData.notes
          })
          .select()
          .single()

        if (paymentError) throw paymentError

        // Update loan balances
        const newBalance = loan.current_balance - appliedToPrincipal
        const newTotalPaid = loan.total_paid + paymentAmount
        const newPenalties = Math.max(0, loan.total_penalties - appliedToPenalty)

        await supabase
          .from('loans')
          .update({
            current_balance: Math.max(0, newBalance),
            total_paid: newTotalPaid,
            total_penalties: newPenalties,
            status: newBalance <= 0 ? 'closed' : loan.status
          })
          .eq('id', paymentData.loan_id)

        // Calculate and update agent earnings
        if (appliedToInterest > 0) {
          const { data: earnings } = await supabase
            .from('earnings')
            .select('*')
            .eq('agent_id', loan.account.assigned_agent_id)
            .single()

          if (earnings) {
            const commission = calculateCommission(appliedToInterest, earnings.commission_percentage)

            await supabase
              .from('earnings')
              .update({
                total_earnings: earnings.total_earnings + commission,
                collectible_earnings: earnings.collectible_earnings + commission
              })
              .eq('agent_id', loan.account.assigned_agent_id)
          }
        }

        // Add transaction log
        await supabase.from('transactions').insert({
          type: 'receive_payment',
          user_id: authStore.user.id,
          loan_id: paymentData.loan_id,
          payment_id: payment.id,
          details: {
            amount: paymentAmount,
            applied_to_principal: appliedToPrincipal,
            applied_to_interest: appliedToInterest,
            applied_to_penalty: appliedToPenalty
          }
        })

        // Notify agent about payment
        await supabase.from('notifications').insert({
          user_id: loan.account.assigned_agent_id,
          loan_id: paymentData.loan_id,
          message: `Payment of ${paymentAmount} received`,
          type: 'payment_received'
        })

        await this.fetchPayments()
        return { success: true, data: payment }
      } catch (error: any) {
        console.error('Error recording payment:', error)
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    async fetchPaymentHistory(loanId: string) {
      const supabase = useSupabaseClient()

      const { data, error } = await supabase
        .from('payments')
        .select('*')
        .eq('loan_id', loanId)
        .order('payment_date', { ascending: false })

      if (error) throw error

      return data || []
    }
  }
})
