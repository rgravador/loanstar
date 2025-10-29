export default defineNuxtRouteMiddleware(async (to, from) => {
  const authStore = useAuthStore()

  // Try to refresh session if not authenticated
  if (!authStore.isAuthenticated) {
    await authStore.refreshSession()
  }

  // Redirect to login if not authenticated
  if (!authStore.isAuthenticated) {
    return navigateTo('/auth/login')
  }
})
