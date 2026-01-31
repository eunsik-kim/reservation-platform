<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900">내 예약</h1>
    </div>

    <UiLoading v-if="loading" full-page text="예약 목록을 불러오는 중..." />

    <div v-else-if="reservations.length === 0" class="text-center py-12">
      <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
      </svg>
      <h3 class="mt-2 text-sm font-medium text-gray-900">예약 내역이 없습니다</h3>
      <p class="mt-1 text-sm text-gray-500">이벤트를 둘러보고 예약해보세요.</p>
      <div class="mt-6">
        <NuxtLink to="/" class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700">
          이벤트 둘러보기
        </NuxtLink>
      </div>
    </div>

    <div v-else class="space-y-4">
      <UiCard v-for="reservation in reservations" :key="reservation.id">
        <div class="flex justify-between items-start">
          <div>
            <NuxtLink :to="`/events/${reservation.eventId}`" class="text-lg font-semibold text-gray-900 hover:text-indigo-600">
              {{ reservation.eventTitle }}
            </NuxtLink>
            <p v-if="reservation.slotName" class="text-sm text-gray-500 mt-1">
              옵션: {{ reservation.slotName }}
            </p>
            <p class="text-sm text-gray-500 mt-1">
              예약일: {{ formatDate(reservation.createdAt) }}
            </p>
            <p v-if="reservation.confirmedAt" class="text-sm text-gray-500">
              확정일: {{ formatDate(reservation.confirmedAt) }}
            </p>
          </div>
          <div class="flex flex-col items-end space-y-2">
            <UiBadge :variant="statusVariant(reservation.status)">
              {{ statusText(reservation.status) }}
            </UiBadge>
            <div class="flex space-x-2">
              <NuxtLink
                v-if="reservation.status === 'CONFIRMED'"
                :to="`/reservations/${reservation.id}`"
                class="text-sm text-indigo-600 hover:text-indigo-800"
              >
                상세보기
              </NuxtLink>
              <button
                v-if="reservation.status === 'PENDING' || reservation.status === 'CONFIRMED'"
                @click="cancelReservation(reservation.id)"
                class="text-sm text-red-600 hover:text-red-800"
              >
                예약 취소
              </button>
            </div>
          </div>
        </div>
      </UiCard>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center mt-6 space-x-2">
        <button
          v-for="page in totalPages"
          :key="page"
          @click="loadReservations(page - 1)"
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

    <!-- Cancel Confirmation Modal -->
    <UiModal v-model="showCancelModal" title="예약 취소">
      <p class="text-gray-600">정말로 이 예약을 취소하시겠습니까?</p>
      <template #footer>
        <div class="flex justify-end space-x-3">
          <UiButton variant="secondary" @click="showCancelModal = false">
            아니오
          </UiButton>
          <UiButton variant="danger" :loading="cancelling" @click="confirmCancel">
            예, 취소합니다
          </UiButton>
        </div>
      </template>
    </UiModal>
  </div>
</template>

<script setup lang="ts">
import { api } from '~/services/api'
import type { Reservation } from '~/types'

definePageMeta({
  middleware: 'auth'
})

const reservations = ref<Reservation[]>([])
const loading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)
const showCancelModal = ref(false)
const cancelling = ref(false)
const selectedReservationId = ref<number | null>(null)

const loadReservations = async (page = 0) => {
  loading.value = true
  try {
    const response = await api.getMyReservations(page, 10)
    reservations.value = response.reservations
    currentPage.value = response.currentPage
    totalPages.value = response.totalPages
  } catch (error: any) {
    console.error('Failed to load reservations:', error)
  } finally {
    loading.value = false
  }
}

const cancelReservation = (id: number) => {
  selectedReservationId.value = id
  showCancelModal.value = true
}

const confirmCancel = async () => {
  if (!selectedReservationId.value) return

  cancelling.value = true
  try {
    await api.cancelReservation(selectedReservationId.value)
    showCancelModal.value = false
    await loadReservations(currentPage.value)
  } catch (error: any) {
    alert(error.message)
  } finally {
    cancelling.value = false
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
    case 'EXPIRED':
      return 'gray'
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
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadReservations()
})
</script>
