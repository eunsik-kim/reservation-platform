import { defineStore } from 'pinia'
import { api } from '~/services/api'
import type { User, LoginRequest, SignupRequest } from '~/types'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    accessToken: null as string | null,
    refreshToken: null as string | null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.accessToken,
    isLoggedIn: (state) => !!state.accessToken,
  },

  actions: {
    async login(credentials: LoginRequest): Promise<boolean> {
      try {
        const response = await api.login(credentials)
        this.setTokens(response.accessToken, response.refreshToken)

        // Load user info
        const user = await api.getCurrentUser()
        this.user = user

        return true
      } catch (error) {
        console.error('Login failed:', error)
        return false
      }
    },

    async signup(data: SignupRequest): Promise<boolean> {
      try {
        await api.signup(data)
        // Auto login after signup
        return await this.login({ email: data.email, password: data.password })
      } catch (error) {
        console.error('Signup failed:', error)
        return false
      }
    },

    async logout() {
      try {
        await api.logout()
      } catch (error) {
        console.error('Logout failed:', error)
      } finally {
        this.clearAuth()
      }
    },

    async refreshTokens(): Promise<boolean> {
      if (!this.refreshToken) return false

      try {
        const response = await api.refreshToken(this.refreshToken)
        this.setTokens(response.accessToken, response.refreshToken)
        return true
      } catch (error) {
        console.error('Token refresh failed:', error)
        this.clearAuth()
        return false
      }
    },

    async loadUser() {
      if (!this.accessToken) return

      try {
        const user = await api.getCurrentUser()
        this.user = user
      } catch (error) {
        console.error('Failed to load user:', error)
        // Token might be invalid, try refresh
        const refreshed = await this.refreshTokens()
        if (refreshed) {
          try {
            const user = await api.getCurrentUser()
            this.user = user
          } catch {
            this.clearAuth()
          }
        }
      }
    },

    setTokens(accessToken: string, refreshToken: string) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken

      if (import.meta.client) {
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', refreshToken)
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
