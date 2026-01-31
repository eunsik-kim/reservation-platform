<template>
  <div>
    <div class="mb-6">
      <NuxtLink to="/events/my" class="text-indigo-600 hover:text-indigo-800 text-sm">
        &larr; 내 이벤트 목록으로
      </NuxtLink>
    </div>

    <UiLoading v-if="loading" full-page text="참가자 목록을 불러오는 중..." />

    <div v-else>
      <UiCard>
        <template #header>
          <div class="flex justify-between items-center">
            <div>
              <h1 class="text-xl font-bold text-gray-900">참가자 목록</h1>
              <p class="text-sm text-gray-500 mt-1">{{ event?.title }}</p>
            </div>
            <div class="text-right">
              <p class="text-2xl font-bold text-indigo-600">{{ totalElements }}</p>
              <p class="text-sm text-gray-500">총 참가자</p>
            </div>
          </div>
        </template>

        <div v-if="participants.length === 0" class="text-center py-8">
          <p class="text-gray-500">아직 참가자가 없습니다.</p>
        </div>

        <div v-else>
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  참가자
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  옵션
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  상태
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  예약일
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="participant in participants" :key="participant.reservationId">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div>
                    <div class="text-sm font-medium text-gray-900">{{ participant.userName }}</div>
                    <div class="text-sm text-gray-500">{{ participant.userEmail }}</div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ participant.slotName || '-' }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <UiBadge :variant="statusVariant(participant.status)">
                    {{ statusText(participant.status) }}
                  </UiBadge>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {{ formatDate(participant.reservedAt) }}
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="flex justify-center mt-6 space-x-2">
            <button
              v-for="page in totalPages"
              :key="page"
              @click="loadParticipants(page - 1)"
              :class="[
                'px-3 py-1 rounded-md text-sm',
                currentPage === page - 1
                  ? 'bg-indigo-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              {{ page }}
            </button>
          </div>
        </div>
      </UiCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { api } from '~/services/api'
import type { Event, Participant } from '~/types'

definePageMeta({
  middleware: 'auth'
})

const route = useRoute()
const router = useRouter()
const eventId = Number(route.params.id)

const event = ref<Event | null>(null)
const participants = ref<Participant[]>([])
const loading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const loadParticipants = async (page = 0) => {
  loading.value = true
  try {
    if (!event.value) {
      event.value = await api.getEvent(eventId)
    }

    const response = await api.getParticipants(eventId, page, 20)
    participants.value = response.participants
    currentPage.value = response.currentPage
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
  } catch (error: any) {
    console.error('Failed to load participants:', error)
    router.push('/events/my')
  } finally {
    loading.value = false
  }
}

const statusVariant = (status: string) => {
  switch (status) {
    case 'CONFIRMED':
      return 'green'
    case 'PENDING':
      return 'yellow'
    case 'CANCELLED':
      return 'red'
    default:
      return 'gray'
  }
}

const statusText = (status: string) => {
  switch (status) {
    case 'PENDING':
      return '대기중'
    case 'CONFIRMED':
      return '확정'
    case 'CANCELLED':
      return '취소됨'
    case 'EXPIRED':
      return '만료됨'
    default:
      return status
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadParticipants()
})
</script>
