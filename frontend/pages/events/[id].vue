<script setup lang="ts">
import type { Event, Slot } from '~/types'

const config = useRuntimeConfig()
const route = useRoute()
const router = useRouter()
const { isAuthenticated } = useAuth()
const authStore = useAuthStore()

const eventId = computed(() => route.params.id)

const { data: event, pending: loadingEvent } = await useFetch<Event>(
  `${config.public.apiBase}/events/${eventId.value}`
)

const { data: slotsData } = await useFetch<{ content: Slot[] }>(
  `${config.public.apiBase}/events/${eventId.value}/slots`
)

const slots = computed(() => slotsData.value?.content ?? [])

const selectedSlotId = ref<number | null>(null)
const joining = ref(false)
const error = ref('')

const canJoin = computed(() => {
  if (!event.value) return false
  return event.value.status === 'OPEN' &&
         event.value.currentParticipants < event.value.maxParticipants
})

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleJoinQueue = async () => {
  if (!isAuthenticated.value) {
    router.push('/login')
    return
  }

  joining.value = true
  error.value = ''

  try {
    if (event.value?.settings.useQueue) {
      // Enter queue first
      await $fetch(`${config.public.apiBase}/queue/enter`, {
        method: 'POST',
        body: { eventId: eventId.value },
        headers: {
          Authorization: `Bearer ${authStore.accessToken}`,
        },
      })
      router.push(`/queue?eventId=${eventId.value}`)
    } else {
      // Direct reservation
      const response = await $fetch<{ id: number }>(`${config.public.apiBase}/reservations`, {
        method: 'POST',
        body: {
          eventId: Number(eventId.value),
          slotId: selectedSlotId.value,
        },
        headers: {
          Authorization: `Bearer ${authStore.accessToken}`,
        },
      })
      router.push(`/reservation/${response.id}`)
    }
  } catch (e: any) {
    error.value = e.data?.message || 'Failed to join'
  } finally {
    joining.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-100 py-8">
    <div class="max-w-3xl mx-auto px-4">
      <NuxtLink to="/" class="text-primary-600 hover:underline mb-4 inline-block">
        &larr; Back to Events
      </NuxtLink>

      <div v-if="loadingEvent" class="bg-white rounded-lg shadow p-8 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">Loading event...</p>
      </div>

      <div v-else-if="event" class="bg-white rounded-lg shadow overflow-hidden">
        <div class="h-48 bg-gradient-to-r from-primary-500 to-primary-700 flex items-center justify-center">
          <span class="text-white text-6xl font-bold">{{ event.title.charAt(0) }}</span>
        </div>

        <div class="p-8">
          <div class="flex justify-between items-start mb-4">
            <h1 class="text-3xl font-bold text-gray-900">{{ event.title }}</h1>
            <span
              :class="[
                'px-3 py-1 rounded-full text-sm font-medium',
                event.status === 'OPEN' ? 'bg-green-100 text-green-800' :
                event.status === 'SCHEDULED' ? 'bg-blue-100 text-blue-800' :
                'bg-gray-100 text-gray-800'
              ]"
            >
              {{ event.status }}
            </span>
          </div>

          <p class="text-gray-600 mb-6 whitespace-pre-wrap">{{ event.description }}</p>

          <div class="grid grid-cols-2 gap-6 mb-6">
            <div class="bg-gray-50 p-4 rounded-lg">
              <h3 class="text-sm font-medium text-gray-500 mb-1">Opens At</h3>
              <p class="text-gray-900">{{ formatDate(event.openAt) }}</p>
            </div>
            <div class="bg-gray-50 p-4 rounded-lg">
              <h3 class="text-sm font-medium text-gray-500 mb-1">Closes At</h3>
              <p class="text-gray-900">{{ formatDate(event.closeAt) }}</p>
            </div>
            <div class="bg-gray-50 p-4 rounded-lg">
              <h3 class="text-sm font-medium text-gray-500 mb-1">Participants</h3>
              <p class="text-gray-900">{{ event.currentParticipants }} / {{ event.maxParticipants }}</p>
            </div>
            <div class="bg-gray-50 p-4 rounded-lg">
              <h3 class="text-sm font-medium text-gray-500 mb-1">Created By</h3>
              <p class="text-gray-900">{{ event.creatorName }}</p>
            </div>
          </div>

          <!-- Settings Info -->
          <div class="border-t pt-6 mb-6">
            <h2 class="text-lg font-semibold text-gray-900 mb-3">Event Info</h2>
            <div class="flex flex-wrap gap-2">
              <span v-if="event.settings.useQueue" class="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm">
                Queue System
              </span>
              <span v-if="event.settings.requirePayment" class="px-3 py-1 bg-yellow-100 text-yellow-800 rounded-full text-sm">
                Payment Required
              </span>
              <span class="px-3 py-1 bg-gray-100 text-gray-800 rounded-full text-sm">
                Max {{ event.settings.maxReservationsPerUser }} per user
              </span>
            </div>
          </div>

          <!-- Slots -->
          <div v-if="slots.length > 0" class="border-t pt-6 mb-6">
            <h2 class="text-lg font-semibold text-gray-900 mb-3">Available Options</h2>
            <div class="space-y-2">
              <label
                v-for="slot in slots"
                :key="slot.id"
                :class="[
                  'flex items-center justify-between p-4 border rounded-lg cursor-pointer transition-colors',
                  selectedSlotId === slot.id ? 'border-primary-500 bg-primary-50' : 'border-gray-200 hover:border-gray-300',
                  slot.availableQuantity === 0 ? 'opacity-50 cursor-not-allowed' : ''
                ]"
              >
                <div class="flex items-center gap-3">
                  <input
                    v-model="selectedSlotId"
                    type="radio"
                    :value="slot.id"
                    :disabled="slot.availableQuantity === 0"
                    class="w-4 h-4 text-primary-600"
                  />
                  <div>
                    <p class="font-medium text-gray-900">{{ slot.name }}</p>
                    <p class="text-sm text-gray-500">{{ slot.availableQuantity }} / {{ slot.quantity }} available</p>
                  </div>
                </div>
                <span v-if="slot.price > 0" class="font-semibold text-gray-900">
                  {{ slot.price.toLocaleString() }}원
                </span>
                <span v-else class="text-gray-500">Free</span>
              </label>
            </div>
          </div>

          <div v-if="error" class="text-red-600 text-sm mb-4">{{ error }}</div>

          <button
            v-if="canJoin"
            @click="handleJoinQueue"
            :disabled="joining"
            class="w-full bg-primary-600 text-white py-3 rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 text-lg font-semibold"
          >
            {{ joining ? 'Processing...' : event.settings.useQueue ? 'Join Queue' : 'Reserve Now' }}
          </button>

          <div v-else-if="event.status === 'SCHEDULED'" class="text-center py-4">
            <p class="text-gray-600">Reservations open at</p>
            <p class="text-xl font-semibold text-primary-600">{{ formatDate(event.openAt) }}</p>
          </div>

          <div v-else-if="event.status === 'CLOSED'" class="text-center py-4">
            <p class="text-gray-600">This event has ended</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
