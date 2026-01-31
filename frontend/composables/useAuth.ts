export const useAuth = () => {
  const authStore = useAuthStore()
  const router = useRouter()

  const login = async (email: string, password: string): Promise<boolean> => {
    const success = await authStore.login({ email, password })
    if (success) {
      await router.push('/')
    }
    return success
  }

  const signup = async (email: string, password: string, name: string): Promise<boolean> => {
    const success = await authStore.signup({ email, password, name })
    if (success) {
      await router.push('/')
    }
    return success
  }

  const logout = async () => {
    await authStore.logout()
    await router.push('/login')
  }

  const requireAuth = () => {
    if (!authStore.isAuthenticated) {
      router.push('/login')
      return false
    }
    return true
  }

  return {
    user: computed(() => authStore.user),
    isAuthenticated: computed(() => authStore.isAuthenticated),
    login,
    signup,
    logout,
    requireAuth,
  }
}
