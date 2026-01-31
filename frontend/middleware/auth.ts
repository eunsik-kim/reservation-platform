export default defineNuxtRouteMiddleware((to) => {
  const authStore = useAuthStore()

  // 인증이 필요한 페이지 목록
  const protectedRoutes = [
    '/events/create',
    '/events/my',
    '/reservations',
    '/payment'
  ]

  const isProtected = protectedRoutes.some(route => to.path.startsWith(route))

  if (isProtected && !authStore.isLoggedIn) {
    return navigateTo(`/login?redirect=${to.path}`)
  }
})
