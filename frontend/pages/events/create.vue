<script setup lang="ts">
import type { CreateEventRequest, EventSettings } from '~/types'

const config = useRuntimeConfig()
const router = useRouter()
const { requireAuth, isAuthenticated } = useAuth()
const authStore = useAuthStore()

onMounted(() => {
  requireAuth()
})

const form = reactive({
  title: '',
  description: '',
  openAt: '',
  closeAt: '',
  maxParticipants: 100,
})

const settings = reactive<EventSettings>({
  useQueue: true,
  queueBatchSize: 50,
  requirePayment: false,
  maxReservationsPerUser: 1,
  reservationTimeLimit: 600,
})

const loading = ref(false)
const error = ref('')

const handleSubmit = async () => {
  if (!isAuthenticated.value) {
    router.push('/login')
    return
  }

  error.value = ''
  loading.value = true

  try {
    const request: CreateEventRequest = {
      ...form,
      settings,
    }

    const response = await $fetch<{ id: number }>(`${config.public.apiBase}/events`, {
      method: 'POST',
      body: request,
      headers: {
        Authorization: `Bearer ${authStore.accessToken}`,
      },
    })

    router.push(`/events/${response.id}`)
  } catch (e: any) {
    error.value = e.data?.message || 'Failed to create event'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-100 py-8">
    <div class="max-w-2xl mx-auto px-4">
      <div class="bg-white rounded-lg shadow p-8">
        <h1 class="text-2xl font-bold text-gray-900 mb-6">Create New Event</h1>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Basic Info -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Event Title</label>
            <input
              v-model="form.title"
              type="text"
              required
              placeholder="Enter event title"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea
              v-model="form.description"
              rows="4"
              required
              placeholder="Describe your event"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            ></textarea>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Open Time</label>
              <input
                v-model="form.openAt"
                type="datetime-local"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Close Time</label>
              <input
                v-model="form.closeAt"
                type="datetime-local"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Max Participants</label>
            <input
              v-model.number="form.maxParticipants"
              type="number"
              min="1"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
          </div>

          <!-- Settings -->
          <div class="border-t pt-6">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">Event Settings</h2>

            <div class="space-y-4">
              <label class="flex items-center gap-3">
                <input
                  v-model="settings.useQueue"
                  type="checkbox"
                  class="w-5 h-5 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                />
                <span class="text-gray-700">Use waiting queue</span>
              </label>

              <div v-if="settings.useQueue" class="ml-8">
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  Queue Batch Size (users admitted at once)
                </label>
                <input
                  v-model.number="settings.queueBatchSize"
                  type="number"
                  min="1"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                />
              </div>

              <label class="flex items-center gap-3">
                <input
                  v-model="settings.requirePayment"
                  type="checkbox"
                  class="w-5 h-5 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                />
                <span class="text-gray-700">Require payment</span>
              </label>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  Max Reservations Per User
                </label>
                <input
                  v-model.number="settings.maxReservationsPerUser"
                  type="number"
                  min="1"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  Reservation Time Limit (seconds)
                </label>
                <input
                  v-model.number="settings.reservationTimeLimit"
                  type="number"
                  min="60"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                />
                <p class="text-xs text-gray-500 mt-1">
                  Time allowed to complete reservation after entering
                </p>
              </div>
            </div>
          </div>

          <div v-if="error" class="text-red-600 text-sm">{{ error }}</div>

          <div class="flex gap-4">
            <button
              type="button"
              @click="router.back()"
              class="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              :disabled="loading"
              class="flex-1 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50"
            >
              {{ loading ? 'Creating...' : 'Create Event' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
