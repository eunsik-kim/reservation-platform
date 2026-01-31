export default defineNuxtRouteMiddleware(() => {
  const authStore = useAuthStore()

  // 이미 로그인한 경우 홈으로 리다이렉트
  if (authStore.isLoggedIn) {
    return navigateTo('/')
  }
})
