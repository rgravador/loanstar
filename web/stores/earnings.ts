import { defineStore } from 'pinia'
import type { Earnings } from '~/types'

interface EarningsState {
  earnings: Earnings | null
  loading: boolean
}

export const useEarningsStore = defineStore('earnings', {
  state: (): EarningsState => ({
    earnings: null,
    loading: false
  }),

  getters: {
    collectibleBalance: (state) => state.earnings?.collectible_earnings || 0,
    totalEarnings: (state) => state.earnings?.total_earnings || 0,
    cashedOutTotal: (state) => state.earnings?.cashed_out_amount || 0,
    commissionPercentage: (state) => state.earnings?.commission_percentage || 0
  },

  actions: {
    async fetchEarnings() {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.user) return

        const { data, error } = await supabase
          .from('earnings')
          .select('*')
          .eq('agent_id', authStore.user.id)
          .single()

        if (error) {
          // If no earnings record exists, it might be a new agent
          if (error.code === 'PGRST116') {
            // Create earnings record
            const { data: newEarnings, error: createError } = await supabase
              .from('earnings')
              .insert({
                agent_id: authStore.user.id,
                commission_percentage: 5.0
              })
              .select()
              .single()

            if (createError) throw createError
            this.earnings = newEarnings
          } else {
            throw error
          }
        } else {
          this.earnings = data
        }
      } catch (error: any) {
        console.error('Error fetching earnings:', error)
      } finally {
        this.loading = false
      }
    },

    async updateCommissionPercentage(agentId: string, percentage: number) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.isAdmin) throw new Error('Only admins can update commission percentage')

        const { data, error } = await supabase
          .from('earnings')
          .update({ commission_percentage: percentage })
          .eq('agent_id', agentId)
          .select()
          .single()

        if (error) throw error

        // Add transaction log
        await supabase.from('transactions').insert({
          type: 'commission_update',
          user_id: authStore.user!.id,
          details: {
            agent_id: agentId,
            new_percentage: percentage
          }
        })

        return { success: true, data }
      } catch (error: any) {
        console.error('Error updating commission percentage:', error)
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    }
  }
})
