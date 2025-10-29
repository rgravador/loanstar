import { defineStore } from 'pinia'

interface SnackbarMessage {
  text: string
  color: 'success' | 'error' | 'warning' | 'info'
  timeout?: number
}

interface UIState {
  theme: 'light' | 'dark'
  sidebarOpen: boolean
  loading: {
    [key: string]: boolean
  }
  snackbar: {
    show: boolean
    message: string
    color: string
    timeout: number
  }
}

export const useUIStore = defineStore('ui', {
  state: (): UIState => ({
    theme: 'light',
    sidebarOpen: true,
    loading: {},
    snackbar: {
      show: false,
      message: '',
      color: 'info',
      timeout: 3000
    }
  }),

  getters: {
    isDark: (state) => state.theme === 'dark',
    isLoading: (state) => (key: string) => state.loading[key] || false,
    anyLoading: (state) => Object.values(state.loading).some(v => v)
  },

  actions: {
    setTheme(theme: 'light' | 'dark') {
      this.theme = theme
      if (process.client) {
        document.documentElement.classList.toggle('dark', theme === 'dark')
      }
    },

    toggleTheme() {
      this.setTheme(this.theme === 'light' ? 'dark' : 'light')
    },

    toggleSidebar() {
      this.sidebarOpen = !this.sidebarOpen
    },

    setSidebarOpen(open: boolean) {
      this.sidebarOpen = open
    },

    setLoading(key: string, loading: boolean) {
      this.loading[key] = loading
    },

    showSnackbar(message: string | SnackbarMessage) {
      if (typeof message === 'string') {
        this.snackbar = {
          show: true,
          message,
          color: 'info',
          timeout: 3000
        }
      } else {
        this.snackbar = {
          show: true,
          message: message.text,
          color: message.color,
          timeout: message.timeout || 3000
        }
      }
    },

    hideSnackbar() {
      this.snackbar.show = false
    },

    showSuccess(message: string) {
      this.showSnackbar({ text: message, color: 'success' })
    },

    showError(message: string) {
      this.showSnackbar({ text: message, color: 'error', timeout: 5000 })
    },

    showWarning(message: string) {
      this.showSnackbar({ text: message, color: 'warning' })
    },

    showInfo(message: string) {
      this.showSnackbar({ text: message, color: 'info' })
    }
  },

  persist: {
    storage: persistedState.localStorage,
    paths: ['theme', 'sidebarOpen']
  }
})
