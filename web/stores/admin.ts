import { defineStore } from 'pinia'
import type { AdminDashboardStats, AgentPerformance } from '~/types'

interface AdminState {
  dashboardStats: AdminDashboardStats | null
  agentPerformance: AgentPerformance[]
  loading: boolean
}

export const useAdminStore = defineStore('admin', {
  state: (): AdminState => ({
    dashboardStats: null,
    agentPerformance: [],
    loading: false
  }),

  getters: {
    totalSystemBalance: (state) => state.dashboardStats?.total_outstanding || 0,
    totalActiveLoans: (state) => state.dashboardStats?.total_loans || 0,
    topPerformingAgents: (state) => {
      return [...state.agentPerformance]
        .sort((a, b) => b.total_collections - a.total_collections)
        .slice(0, 5)
    }
  },

  actions: {
    async fetchDashboardStats() {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.isAdmin) throw new Error('Admin access required')

        // Fetch total loans and outstanding balance
        const { data: loans, error: loansError } = await supabase
          .from('loans')
          .select('current_balance, status')

        if (loansError) throw loansError

        const activeLoans = loans?.filter(l => l.status === 'active') || []
        const totalOutstanding = activeLoans.reduce((sum, l) => sum + l.current_balance, 0)

        // Fetch total agents
        const { count: agentCount, error: agentError } = await supabase
          .from('users_profile')
          .select('*', { count: 'exact', head: true })
          .eq('role', 'agent')

        if (agentError) throw agentError

        // Fetch pending approvals
        const { count: pendingLoans, error: pendingError } = await supabase
          .from('loans')
          .select('*', { count: 'exact', head: true })
          .eq('status', 'pending_approval')

        if (pendingError) throw pendingError

        // Fetch pending cashouts
        const { count: pendingCashouts, error: cashoutError } = await supabase
          .from('cashout_requests')
          .select('*', { count: 'exact', head: true })
          .eq('status', 'pending')

        if (cashoutError) throw cashoutError

        this.dashboardStats = {
          total_loans: loans?.length || 0,
          total_outstanding: totalOutstanding,
          total_agents: agentCount || 0,
          pending_approvals: pendingLoans || 0,
          pending_cashouts: pendingCashouts || 0
        }
      } catch (error: any) {
        console.error('Error fetching dashboard stats:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchAgentPerformance() {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.isAdmin) throw new Error('Admin access required')

        // Fetch all agents
        const { data: agents, error: agentsError } = await supabase
          .from('users_profile')
          .select('id, full_name')
          .eq('role', 'agent')

        if (agentsError) throw agentsError

        const performance: AgentPerformance[] = []

        for (const agent of agents || []) {
          // Get accounts count
          const { count: accountCount } = await supabase
            .from('accounts')
            .select('*', { count: 'exact', head: true })
            .eq('assigned_agent_id', agent.id)

          // Get loans count
          const { data: agentAccounts } = await supabase
            .from('accounts')
            .select('id')
            .eq('assigned_agent_id', agent.id)

          const accountIds = agentAccounts?.map(a => a.id) || []

          const { count: loanCount } = await supabase
            .from('loans')
            .select('*', { count: 'exact', head: true })
            .in('account_id', accountIds)

          // Get total collections (payments)
          const { data: loans } = await supabase
            .from('loans')
            .select('id, total_paid')
            .in('account_id', accountIds)

          const totalCollections = loans?.reduce((sum, l) => sum + l.total_paid, 0) || 0

          // Get earnings
          const { data: earnings } = await supabase
            .from('earnings')
            .select('total_earnings')
            .eq('agent_id', agent.id)
            .single()

          performance.push({
            agent_id: agent.id,
            agent_name: agent.full_name,
            total_accounts: accountCount || 0,
            total_loans: loanCount || 0,
            total_collections: totalCollections,
            total_earnings: earnings?.total_earnings || 0
          })
        }

        this.agentPerformance = performance
      } catch (error: any) {
        console.error('Error fetching agent performance:', error)
      } finally {
        this.loading = false
      }
    },

    async deactivateUser(userId: string) {
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.isAdmin) throw new Error('Admin access required')

        const { error } = await supabase
          .from('users_profile')
          .update({ is_active: false })
          .eq('id', userId)

        if (error) throw error

        return { success: true }
      } catch (error: any) {
        console.error('Error deactivating user:', error)
        return { success: false, error: error.message }
      }
    },

    async assignRole(userId: string, role: 'admin' | 'agent') {
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.isAdmin) throw new Error('Admin access required')

        const { error } = await supabase
          .from('users_profile')
          .update({ role })
          .eq('id', userId)

        if (error) throw error

        return { success: true }
      } catch (error: any) {
        console.error('Error assigning role:', error)
        return { success: false, error: error.message }
      }
    }
  }
})
