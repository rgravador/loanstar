import { defineStore } from 'pinia'
import type { UserProfile } from '~/types'

interface AuthState {
  user: UserProfile | null
  loading: boolean
  session: any | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    loading: false,
    session: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.user,
    isAdmin: (state) => state.user?.role === 'admin',
    isAgent: (state) => state.user?.role === 'agent',
    userProfile: (state) => state.user
  },

  actions: {
    async login(email: string, password: string) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const { data, error } = await supabase.auth.signInWithPassword({
          email,
          password
        })

        if (error) throw error

        this.session = data.session
        await this.fetchUserProfile(data.user.id)

        return { success: true }
      } catch (error: any) {
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    async signup(userData: { email: string; password: string; username: string; full_name: string; role: 'admin' | 'agent' }) {
      this.loading = true
      try {
        const supabase = useSupabaseClient()

        // Create auth user
        const { data: authData, error: authError } = await supabase.auth.signUp({
          email: userData.email,
          password: userData.password
        })

        if (authError) throw authError

        // Create user profile
        const { error: profileError } = await supabase
          .from('users_profile')
          .insert({
            id: authData.user!.id,
            email: userData.email,
            username: userData.username,
            full_name: userData.full_name,
            role: userData.role
          })

        if (profileError) throw profileError

        return { success: true }
      } catch (error: any) {
        return { success: false, error: error.message }
      } finally {
        this.loading = false
      }
    },

    async logout() {
      const supabase = useSupabaseClient()
      await supabase.auth.signOut()
      this.user = null
      this.session = null
      navigateTo('/auth/login')
    },

    async fetchUserProfile(userId: string) {
      const supabase = useSupabaseClient()
      const { data, error } = await supabase
        .from('users_profile')
        .select('*')
        .eq('id', userId)
        .single()

      if (error) throw error

      this.user = data
    },

    async refreshSession() {
      const supabase = useSupabaseClient()
      const { data } = await supabase.auth.getSession()

      if (data.session) {
        this.session = data.session
        await this.fetchUserProfile(data.session.user.id)
      }
    }
  },

  persist: {
    storage: persistedState.localStorage
  }
})
