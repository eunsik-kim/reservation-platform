<script setup lang="ts">
const route = useRoute()
const router = useRouter()
const { requireAuth } = useAuth()
const { queueEntry, isConnected, enterQueue, leaveQueue } = useQueue()

const eventId = computed(() => Number(route.query.eventId))

onMounted(() => {
  if (!requireAuth()) return
  if (eventId.value) {
    enterQueue(eventId.value)
  }
})

const handleLeave = async () => {
  if (eventId.value) {
    await leaveQueue(eventId.value)
    router.push('/')
  }
}

const handleProceed = () => {
  router.push(`/events/${eventId.value}?ready=true`)
}
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center">
    <div class="max-w-md w-full bg-white rounded-lg shadow-lg p-8 text-center">
      <div v-if="!isConnected" class="py-8">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">Connecting to queue...</p>
      </div>

      <template v-else-if="queueEntry">
        <div v-if="queueEntry.status === 'WAITING'" class="py-8">
          <div class="text-6xl font-bold text-primary-600 mb-4">
            {{ queueEntry.position }}
          </div>
          <p class="text-gray-600 mb-2">Your position in queue</p>
          <p class="text-sm text-gray-500">
            Estimated wait: {{ Math.ceil(queueEntry.estimatedWaitTime / 60) }} minutes
          </p>

          <div class="mt-8">
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div
                class="bg-primary-600 h-2 rounded-full transition-all duration-500"
                :style="{ width: `${Math.max(10, 100 - queueEntry.position)}%` }"
              ></div>
            </div>
          </div>

          <button
            @click="handleLeave"
            class="mt-8 text-gray-500 hover:text-gray-700 underline"
          >
            Leave Queue
          </button>
        </div>

        <div v-else-if="queueEntry.status === 'READY'" class="py-8">
          <div class="text-green-500 mb-4">
            <svg class="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-900 mb-2">It's Your Turn!</h2>
          <p class="text-gray-600 mb-6">You can now select your seats</p>

          <button
            @click="handleProceed"
            class="w-full bg-green-600 text-white py-3 rounded-lg hover:bg-green-700 transition-colors"
          >
            Select Seats
          </button>
        </div>

        <div v-else-if="queueEntry.status === 'EXPIRED'" class="py-8">
          <div class="text-red-500 mb-4">
            <svg class="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-900 mb-2">Session Expired</h2>
          <p class="text-gray-600 mb-6">Your turn has expired. Please rejoin the queue.</p>

          <NuxtLink
            to="/"
            class="block w-full bg-primary-600 text-white py-3 rounded-lg hover:bg-primary-700 transition-colors"
          >
            Back to Events
          </NuxtLink>
        </div>
      </template>
    </div>
  </div>
</template>
