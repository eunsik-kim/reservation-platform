import { defineStore } from 'pinia'
import type { User, LoginRequest, SignupRequest, AuthResponse } from '~/types'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    accessToken: null as string | null,
    refreshToken: null as string | null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.accessToken,
  },

  actions: {
    async login(credentials: LoginRequest): Promise<boolean> {
      const config = useRuntimeConfig()
      try {
        const response = await $fetch<AuthResponse>(`${config.public.apiBase}/auth/login`, {
          method: 'POST',
          body: credentials,
        })
        this.setAuth(response)
        return true
      } catch (error) {
        console.error('Login failed:', error)
        return false
      }
    },

    async signup(data: SignupRequest): Promise<boolean> {
      const config = useRuntimeConfig()
      try {
        const response = await $fetch<AuthResponse>(`${config.public.apiBase}/auth/signup`, {
          method: 'POST',
          body: data,
        })
        this.setAuth(response)
        return true
      } catch (error) {
        console.error('Signup failed:', error)
        return false
      }
    },

    async logout() {
      const config = useRuntimeConfig()
      try {
        await $fetch(`${config.public.apiBase}/auth/logout`, {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${this.accessToken}`,
          },
        })
      } catch (error) {
        console.error('Logout failed:', error)
      } finally {
        this.clearAuth()
      }
    },

    setAuth(response: AuthResponse) {
      this.accessToken = response.accessToken
      this.refreshToken = response.refreshToken
      this.user = response.user

      if (import.meta.client) {
        localStorage.setItem('accessToken', response.accessToken)
        localStorage.setItem('refreshToken', response.refreshToken)
      }
    },

    clearAuth() {
      this.accessToken = null
      this.refreshToken = null
      this.user = null

      if (import.meta.client) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
      }
    },

    loadFromStorage() {
      if (import.meta.client) {
        this.accessToken = localStorage.getItem('accessToken')
        this.refreshToken = localStorage.getItem('refreshToken')
      }
    },
  },
})
