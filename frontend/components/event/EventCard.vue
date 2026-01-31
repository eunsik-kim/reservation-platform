<template>
  <NuxtLink :to="`/events/${event.id}`" class="block">
    <div class="bg-white rounded-lg shadow hover:shadow-md transition-shadow p-6">
      <div class="flex justify-between items-start mb-4">
        <h3 class="text-lg font-semibold text-gray-900 line-clamp-2">
          {{ event.title }}
        </h3>
        <UiBadge :variant="statusVariant">
          {{ statusText }}
        </UiBadge>
      </div>

      <p v-if="event.description" class="text-gray-600 text-sm mb-4 line-clamp-2">
        {{ event.description }}
      </p>

      <div class="space-y-2 text-sm text-gray-500">
        <div class="flex items-center">
          <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <span>{{ formatDate(event.openAt) }}</span>
        </div>

        <div class="flex items-center">
          <svg class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          <span>{{ event.currentParticipants }} / {{ event.maxParticipants }}명</span>
        </div>
      </div>

      <!-- Progress bar -->
      <div class="mt-4">
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div
            class="h-2 rounded-full transition-all"
            :class="progressColor"
            :style="{ width: `${progressPercent}%` }"
          />
        </div>
      </div>
    </div>
  </NuxtLink>
</template>

<script setup lang="ts">
import type { Event } from '~/types'

interface Props {
  event: Event
}

const props = defineProps<Props>()

const statusVariant = computed(() => {
  switch (props.event.status) {
    case 'OPEN':
      return 'green'
    case 'SCHEDULED':
      return 'blue'
    case 'CLOSED':
      return 'gray'
    case 'CANCELLED':
      return 'red'
    default:
      return 'gray'
  }
})

const statusText = computed(() => {
  switch (props.event.status) {
    case 'DRAFT':
      return '준비중'
    case 'SCHEDULED':
      return '예정'
    case 'OPEN':
      return '진행중'
    case 'CLOSED':
      return '마감'
    case 'CANCELLED':
      return '취소됨'
    default:
      return props.event.status
  }
})

const progressPercent = computed(() => {
  if (props.event.maxParticipants === 0) return 0
  return Math.min(100, (props.event.currentParticipants / props.event.maxParticipants) * 100)
})

const progressColor = computed(() => {
  if (progressPercent.value >= 100) return 'bg-red-500'
  if (progressPercent.value >= 80) return 'bg-yellow-500'
  return 'bg-indigo-600'
})

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>
