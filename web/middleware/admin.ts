export default defineNuxtRouteMiddleware((to, from) => {
  const authStore = useAuthStore()

  // Check if user is authenticated first
  if (!authStore.isAuthenticated) {
    return navigateTo('/auth/login')
  }

  // Check if user is admin
  if (!authStore.isAdmin) {
    const uiStore = useUIStore()
    uiStore.showError('Access denied. Admin privileges required.')
    return navigateTo('/dashboard')
  }
})
