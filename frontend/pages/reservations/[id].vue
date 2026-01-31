<template>
  <div>
    <UiLoading v-if="loading" full-page text="예약 정보를 불러오는 중..." />

    <div v-else-if="reservation">
      <div class="mb-6">
        <NuxtLink to="/reservations" class="text-indigo-600 hover:text-indigo-800 text-sm">
          &larr; 내 예약 목록으로
        </NuxtLink>
      </div>

      <UiCard>
        <template #header>
          <div class="flex justify-between items-center">
            <h1 class="text-xl font-bold text-gray-900">예약 상세</h1>
            <UiBadge :variant="statusVariant">
              {{ statusText }}
            </UiBadge>
          </div>
        </template>

        <div class="space-y-6">
          <!-- Event Info -->
          <div>
            <h2 class="text-lg font-semibold text-gray-900 mb-2">이벤트 정보</h2>
            <div class="bg-gray-50 rounded-lg p-4">
              <NuxtLink :to="`/events/${reservation.eventId}`" class="text-lg font-medium text-indigo-600 hover:text-indigo-800">
                {{ reservation.eventTitle }}
              </NuxtLink>
              <p v-if="reservation.slotName" class="text-sm text-gray-600 mt-1">
                옵션: {{ reservation.slotName }}
              </p>
            </div>
          </div>

          <!-- Reservation Info -->
          <div>
            <h2 class="text-lg font-semibold text-gray-900 mb-2">예약 정보</h2>
            <dl class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <dt class="text-sm text-gray-500">예약 번호</dt>
                <dd class="text-sm font-medium text-gray-900">#{{ reservation.id }}</dd>
              </div>
              <div>
                <dt class="text-sm text-gray-500">예약일</dt>
                <dd class="text-sm font-medium text-gray-900">{{ formatDate(reservation.createdAt) }}</dd>
              </div>
              <div v-if="reservation.confirmedAt">
                <dt class="text-sm text-gray-500">확정일</dt>
                <dd class="text-sm font-medium text-gray-900">{{ formatDate(reservation.confirmedAt) }}</dd>
              </div>
            </dl>
          </div>

          <!-- Payment Section -->
          <div v-if="needsPayment && reservation.status === 'CONFIRMED'">
            <h2 class="text-lg font-semibold text-gray-900 mb-2">결제</h2>
            <div v-if="payment" class="bg-gray-50 rounded-lg p-4">
              <div class="flex justify-between items-center">
                <div>
                  <p class="text-sm text-gray-600">결제 금액</p>
                  <p class="text-lg font-semibold">{{ payment.amount.toLocaleString() }}원</p>
                </div>
                <UiBadge :variant="paymentStatusVariant">
                  {{ paymentStatusText }}
                </UiBadge>
              </div>
            </div>
            <div v-else class="bg-yellow-50 rounded-lg p-4">
              <p class="text-sm text-yellow-800 mb-3">결제가 필요합니다.</p>
              <NuxtLink
                :to="`/payment?reservationId=${reservation.id}`"
                class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700"
              >
                결제하기
              </NuxtLink>
            </div>
          </div>

          <!-- Actions -->
          <div v-if="reservation.status === 'PENDING' || reservation.status === 'CONFIRMED'" class="pt-4 border-t">
            <UiButton variant="danger" @click="showCancelModal = true">
              예약 취소
            </UiButton>
          </div>
        </div>
      </UiCard>
    </div>

    <!-- Cancel Modal -->
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
import type { Reservation, Payment } from '~/types'

definePageMeta({
  middleware: 'auth'
})

const route = useRoute()
const router = useRouter()
const reservationId = Number(route.params.id)

const reservation = ref<Reservation | null>(null)
const payment = ref<Payment | null>(null)
const loading = ref(true)
const showCancelModal = ref(false)
const cancelling = ref(false)
const needsPayment = ref(false)

const statusVariant = computed(() => {
  switch (reservation.value?.status) {
    case 'CONFIRMED':
      return 'green'
    case 'PENDING':
      return 'yellow'
    case 'CANCELLED':
      return 'red'
    default:
      return 'gray'
  }
})

const statusText = computed(() => {
  switch (reservation.value?.status) {
    case 'PENDING':
      return '대기중'
    case 'CONFIRMED':
      return '확정'
    case 'CANCELLED':
      return '취소됨'
    case 'EXPIRED':
      return '만료됨'
    default:
      return reservation.value?.status
  }
})

const paymentStatusVariant = computed(() => {
  switch (payment.value?.status) {
    case 'COMPLETED':
      return 'green'
    case 'PENDING':
      return 'yellow'
    case 'FAILED':
      return 'red'
    default:
      return 'gray'
  }
})

const paymentStatusText = computed(() => {
  switch (payment.value?.status) {
    case 'COMPLETED':
      return '결제 완료'
    case 'PENDING':
      return '결제 대기'
    case 'FAILED':
      return '결제 실패'
    case 'REFUNDED':
      return '환불됨'
    default:
      return payment.value?.status
  }
})

const loadReservation = async () => {
  loading.value = true
  try {
    reservation.value = await api.getReservation(reservationId)

    // Check if event requires payment
    const event = await api.getEvent(reservation.value.eventId)
    needsPayment.value = event.settings.requirePayment

    // Load payment if exists
    if (needsPayment.value) {
      try {
        payment.value = await api.getPaymentByReservation(reservationId)
      } catch {
        // Payment doesn't exist yet
      }
    }
  } catch (error: any) {
    console.error('Failed to load reservation:', error)
    router.push('/reservations')
  } finally {
    loading.value = false
  }
}

const confirmCancel = async () => {
  cancelling.value = true
  try {
    await api.cancelReservation(reservationId)
    showCancelModal.value = false
    router.push('/reservations')
  } catch (error: any) {
    alert(error.message)
  } finally {
    cancelling.value = false
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
  loadReservation()
})
</script>
