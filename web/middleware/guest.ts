export default defineNuxtRouteMiddleware((to, from) => {
  const authStore = useAuthStore()

  // Redirect to dashboard if already authenticated
  if (authStore.isAuthenticated) {
    if (authStore.isAdmin) {
      return navigateTo('/admin/dashboard')
    } else {
      return navigateTo('/dashboard')
    }
  }
})
