<script setup lang="ts">
import type { Event, PageResponse } from '~/types'

const config = useRuntimeConfig()

const { data: eventsData, refresh } = await useFetch<PageResponse<Event>>(
  `${config.public.apiBase}/events`
)

const events = computed(() => eventsData.value?.content ?? [])

const getStatusColor = (status: string) => {
  switch (status) {
    case 'OPEN': return 'bg-green-100 text-green-800'
    case 'SCHEDULED': return 'bg-blue-100 text-blue-800'
    case 'CLOSED': return 'bg-gray-100 text-gray-800'
    case 'CANCELLED': return 'bg-red-100 text-red-800'
    default: return 'bg-gray-100 text-gray-800'
  }
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<template>
  <div class="min-h-screen bg-gray-100">
    <header class="bg-white shadow">
      <div class="max-w-7xl mx-auto py-6 px-4 flex justify-between items-center">
        <h1 class="text-3xl font-bold text-gray-900">Reservation Platform</h1>
        <div class="flex gap-4">
          <NuxtLink
            to="/events/create"
            class="bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors"
          >
            Create Event
          </NuxtLink>
          <NuxtLink
            to="/my-events"
            class="text-gray-600 hover:text-gray-900 px-4 py-2"
          >
            My Events
          </NuxtLink>
          <NuxtLink
            to="/my-reservations"
            class="text-gray-600 hover:text-gray-900 px-4 py-2"
          >
            My Reservations
          </NuxtLink>
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto py-6 px-4">
      <div class="mb-6">
        <h2 class="text-xl font-semibold text-gray-800 mb-2">Upcoming Events</h2>
        <p class="text-gray-600">Join exciting events and reserve your spot!</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="event in events"
          :key="event.id"
          class="bg-white rounded-lg shadow overflow-hidden hover:shadow-lg transition-shadow"
        >
          <div class="h-32 bg-gradient-to-r from-primary-500 to-primary-700 flex items-center justify-center">
            <span class="text-white text-4xl font-bold">{{ event.title.charAt(0) }}</span>
          </div>
          <div class="p-6">
            <div class="flex justify-between items-start mb-2">
              <h2 class="text-xl font-semibold text-gray-900">{{ event.title }}</h2>
              <span
                :class="['px-2 py-1 rounded-full text-xs font-medium', getStatusColor(event.status)]"
              >
                {{ event.status }}
              </span>
            </div>
            <p class="text-gray-600 text-sm mb-4 line-clamp-2">{{ event.description }}</p>

            <div class="space-y-2 text-sm text-gray-500 mb-4">
              <div class="flex items-center gap-2">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>Opens: {{ formatDate(event.openAt) }}</span>
              </div>
              <div class="flex items-center gap-2">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <span>{{ event.currentParticipants }} / {{ event.maxParticipants }} participants</span>
              </div>
              <div class="flex items-center gap-2">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                <span>by {{ event.creatorName }}</span>
              </div>
            </div>

            <NuxtLink
              :to="`/events/${event.id}`"
              class="block w-full text-center bg-primary-600 text-white py-2 rounded-lg hover:bg-primary-700 transition-colors"
            >
              View Details
            </NuxtLink>
          </div>
        </div>
      </div>

      <div v-if="events.length === 0" class="text-center py-12">
        <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        <p class="text-gray-500 mb-4">No events available yet</p>
        <NuxtLink
          to="/events/create"
          class="text-primary-600 hover:underline"
        >
          Create the first event
        </NuxtLink>
      </div>
    </main>
  </div>
</template>
