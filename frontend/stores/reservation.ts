import { defineStore } from 'pinia'
import type { Reservation, CreateReservationRequest, PageResponse } from '~/types'

export const useReservationStore = defineStore('reservation', {
  state: () => ({
    reservations: [] as Reservation[],
    currentReservation: null as Reservation | null,
    loading: false,
  }),

  actions: {
    async fetchMyReservations(): Promise<void> {
      const config = useRuntimeConfig()
      const authStore = useAuthStore()

      this.loading = true
      try {
        const response = await $fetch<PageResponse<Reservation>>(
          `${config.public.apiBase}/reservations/my`,
          {
            headers: {
              Authorization: `Bearer ${authStore.accessToken}`,
            },
          }
        )
        this.reservations = response.content
      } catch (error) {
        console.error('Failed to fetch reservations:', error)
      } finally {
        this.loading = false
      }
    },

    async createReservation(request: CreateReservationRequest): Promise<Reservation | null> {
      const config = useRuntimeConfig()
      const authStore = useAuthStore()

      this.loading = true
      try {
        const reservation = await $fetch<Reservation>(
          `${config.public.apiBase}/reservations`,
          {
            method: 'POST',
            body: request,
            headers: {
              Authorization: `Bearer ${authStore.accessToken}`,
            },
          }
        )
        this.currentReservation = reservation
        return reservation
      } catch (error) {
        console.error('Failed to create reservation:', error)
        return null
      } finally {
        this.loading = false
      }
    },

    async cancelReservation(id: number): Promise<boolean> {
      const config = useRuntimeConfig()
      const authStore = useAuthStore()

      try {
        await $fetch(`${config.public.apiBase}/reservations/${id}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${authStore.accessToken}`,
          },
        })
        this.reservations = this.reservations.filter((r) => r.id !== id)
        return true
      } catch (error) {
        console.error('Failed to cancel reservation:', error)
        return false
      }
    },
  },
})
