import { defineStore } from 'pinia'
import type { CashoutRequest, CashoutRequestForm } from '~/types'

interface CashoutsState {
  cashoutRequests: CashoutRequest[]
  loading: boolean
}

export const useCashoutsStore = defineStore('cashouts', {
  state: (): CashoutsState => ({
    cashoutRequests: [],
    loading: false
  }),

  getters: {
    pendingCashouts: (state) => state.cashoutRequests.filter(c => c.status === 'pending'),

    cashoutHistory: (state) => state.cashoutRequests.filter(c => c.status !== 'pending'),

    agentCashouts: (state) => (agentId: string) => {
      return state.cashoutRequests.filter(c => c.agent_id === agentId)
    }
  },

  actions: {
    async fetchCashoutRequests() {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.user) return

        let query = supabase
          .from('cashout_requests')
          .select('*')
          .order('request_date', { ascending: false })

        // Filter by agent if not admin
        if (!authStore.isAdmin) {
          query = query.eq('agent_id', authStore.user.id)
        }

        const { data, error } = await query

        if (error) throw error

        this.cashoutRequests = data || []
      } catch (error: any) {
        console.error('Error fetching cashout requests:', error)
      } finally {
        this.loading = false
      }
    },

    async requestCashout(cashoutData: CashoutRequestForm) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()
        const earningsStore = useEarningsStore()

        if (!authStore.user) throw new Error('User not authenticated')

        // Validate minimum amount
        if (cashoutData.amount < 10) {
          throw new Error('Minimum cashout amount is 10')
        }

        // Check if agent has sufficient collectible earnings
        await earningsStore.fetchEarnings()

        if (!earningsStore.earnings) {
          throw new Error('Earnings record not found')
        }

        if (cashoutData.amount > earningsStore.earnings.collectible_earnings) {
          throw new Error('Insufficient collectible earnings')
        }

        const { data, error } = await supabase
          .from('cashout_requests')
          .insert({
            agent_id: authStore.user.id,
            amount: cashoutData.amount,
            status: 'pending'
          })
          .select()
          .single()

        if (error) throw error

        // Add transaction log
        await supabase.from('transactions').insert({
          type: 'cashout_request',
          user_id: authStore.user.id,
          details: { amount: cashoutData.amount }
        })

        // Notify admins
        const { data: admins } = await supabase
          .from('users_profile')
          .select('id')
          .eq('role', 'admin')

        if (admins) {
          const notifications = admins.map(admin => ({
            user_id: admin.id,
            message: `New cashout request from ${authStore.user!.full_name} for ${cashoutData.amount}`,
            type: 'cashout_approved' as const
          }))

          await supabase.from('notifications').insert(notifications)
        }

        await this.fetchCashoutRequests()
        return { success: true, data }
      } catch (error: any) {
        console.error('Error requesting cashout:', error)
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    async approveCashout(cashoutId: string) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.isAdmin) throw new Error('Only admins can approve cashouts')

        // Get cashout request details
        const { data: cashout, error: fetchError } = await supabase
          .from('cashout_requests')
          .select('*')
          .eq('id', cashoutId)
          .single()

        if (fetchError) throw fetchError

        // Update cashout request
        const { data, error } = await supabase
          .from('cashout_requests')
          .update({
            status: 'approved',
            approval_date: new Date().toISOString(),
            approved_by: authStore.user!.id
          })
          .eq('id', cashoutId)
          .select()
          .single()

        if (error) throw error

        // Update agent earnings
        const { data: earnings, error: earningsError } = await supabase
          .from('earnings')
          .select('*')
          .eq('agent_id', cashout.agent_id)
          .single()

        if (earningsError) throw earningsError

        await supabase
          .from('earnings')
          .update({
            collectible_earnings: earnings.collectible_earnings - cashout.amount,
            cashed_out_amount: earnings.cashed_out_amount + cashout.amount
          })
          .eq('agent_id', cashout.agent_id)

        // Add transaction log
        await supabase.from('transactions').insert({
          type: 'cashout_approved',
          user_id: authStore.user!.id,
          details: {
            cashout_id: cashoutId,
            agent_id: cashout.agent_id,
            amount: cashout.amount
          }
        })

        // Notify agent
        await supabase.from('notifications').insert({
          user_id: cashout.agent_id,
          message: `Your cashout request of ${cashout.amount} has been approved`,
          type: 'cashout_approved'
        })

        await this.fetchCashoutRequests()
        return { success: true, data }
      } catch (error: any) {
        console.error('Error approving cashout:', error)
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    async rejectCashout(cashoutId: string, reason: string) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.isAdmin) throw new Error('Only admins can reject cashouts')

        // Get cashout request details
        const { data: cashout, error: fetchError } = await supabase
          .from('cashout_requests')
          .select('*')
          .eq('id', cashoutId)
          .single()

        if (fetchError) throw fetchError

        const { data, error } = await supabase
          .from('cashout_requests')
          .update({
            status: 'rejected',
            rejection_reason: reason,
            approved_by: authStore.user!.id
          })
          .eq('id', cashoutId)
          .select()
          .single()

        if (error) throw error

        // Add transaction log
        await supabase.from('transactions').insert({
          type: 'cashout_rejected',
          user_id: authStore.user!.id,
          details: {
            cashout_id: cashoutId,
            agent_id: cashout.agent_id,
            amount: cashout.amount,
            reason
          }
        })

        // Notify agent
        await supabase.from('notifications').insert({
          user_id: cashout.agent_id,
          message: `Your cashout request has been rejected: ${reason}`,
          type: 'cashout_rejected'
        })

        await this.fetchCashoutRequests()
        return { success: true, data }
      } catch (error: any) {
        console.error('Error rejecting cashout:', error)
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    }
  }
})
