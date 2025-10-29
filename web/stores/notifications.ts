import { defineStore } from 'pinia'
import type { Notification } from '~/types'

interface NotificationsState {
  notifications: Notification[]
  unreadCount: number
  loading: boolean
}

export const useNotificationsStore = defineStore('notifications', {
  state: (): NotificationsState => ({
    notifications: [],
    unreadCount: 0,
    loading: false
  }),

  getters: {
    unreadNotifications: (state) => state.notifications.filter(n => !n.is_read),

    notificationsByType: (state) => (type: string) => {
      return state.notifications.filter(n => n.type === type)
    },

    recentNotifications: (state) => state.notifications.slice(0, 5)
  },

  actions: {
    async fetchNotifications() {
      this.loading = true
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.user) return

        const { data, error } = await supabase
          .from('notifications')
          .select('*')
          .eq('user_id', authStore.user.id)
          .order('timestamp', { ascending: false })

        if (error) throw error

        this.notifications = data || []
        this.unreadCount = this.notifications.filter(n => !n.is_read).length
      } catch (error: any) {
        console.error('Error fetching notifications:', error)
      } finally {
        this.loading = false
      }
    },

    async markAsRead(notificationId: string) {
      try {
        const supabase = useSupabaseClient()

        const { error } = await supabase
          .from('notifications')
          .update({ is_read: true })
          .eq('id', notificationId)

        if (error) throw error

        // Update local state
        const notification = this.notifications.find(n => n.id === notificationId)
        if (notification && !notification.is_read) {
          notification.is_read = true
          this.unreadCount--
        }
      } catch (error: any) {
        console.error('Error marking notification as read:', error)
      }
    },

    async markAllAsRead() {
      try {
        const supabase = useSupabaseClient()
        const authStore = useAuthStore()

        if (!authStore.user) return

        const { error } = await supabase
          .from('notifications')
          .update({ is_read: true })
          .eq('user_id', authStore.user.id)
          .eq('is_read', false)

        if (error) throw error

        // Update local state
        this.notifications.forEach(n => {
          n.is_read = true
        })
        this.unreadCount = 0
      } catch (error: any) {
        console.error('Error marking all notifications as read:', error)
      }
    },

    subscribeToRealtime() {
      const supabase = useSupabaseClient()
      const authStore = useAuthStore()

      if (!authStore.user) return

      const channel = supabase
        .channel('notifications')
        .on(
          'postgres_changes',
          {
            event: 'INSERT',
            schema: 'public',
            table: 'notifications',
            filter: `user_id=eq.${authStore.user.id}`
          },
          (payload) => {
            this.notifications.unshift(payload.new as Notification)
            this.unreadCount++

            // Show snackbar notification
            const uiStore = useUIStore()
            uiStore.showInfo(payload.new.message)
          }
        )
        .subscribe()

      return () => {
        supabase.removeChannel(channel)
      }
    }
  }
})
