<template>
  <div>
    <div class="mb-6">
      <NuxtLink to="/reservations" class="text-indigo-600 hover:text-indigo-800 text-sm">
        &larr; 내 예약으로
      </NuxtLink>
    </div>

    <UiLoading v-if="loading" full-page text="결제 정보를 불러오는 중..." />

    <div v-else-if="reservation" class="max-w-lg mx-auto">
      <UiCard>
        <template #header>
          <h1 class="text-xl font-bold text-gray-900">결제하기</h1>
        </template>

        <!-- Reservation Info -->
        <div class="mb-6 pb-6 border-b">
          <h2 class="text-lg font-semibold text-gray-900 mb-2">예약 정보</h2>
          <div class="bg-gray-50 rounded-lg p-4">
            <p class="font-medium text-gray-900">{{ reservation.eventTitle }}</p>
            <p v-if="reservation.slotName" class="text-sm text-gray-600 mt-1">
              옵션: {{ reservation.slotName }}
            </p>
            <p class="text-sm text-gray-600 mt-1">
              예약 번호: #{{ reservation.id }}
            </p>
          </div>
        </div>

        <!-- Payment Amount -->
        <div class="mb-6 pb-6 border-b">
          <h2 class="text-lg font-semibold text-gray-900 mb-2">결제 금액</h2>
          <p class="text-3xl font-bold text-indigo-600">
            {{ amount.toLocaleString() }}원
          </p>
        </div>

        <!-- Payment Method -->
        <div class="mb-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">결제 수단</h2>
          <div class="space-y-3">
            <label
              v-for="method in paymentMethods"
              :key="method.value"
              class="flex items-center p-4 border rounded-lg cursor-pointer hover:bg-gray-50"
              :class="{ 'border-indigo-500 bg-indigo-50': selectedMethod === method.value }"
            >
              <input
                type="radio"
                :value="method.value"
                v-model="selectedMethod"
                class="h-4 w-4 text-indigo-600 focus:ring-indigo-500"
              />
              <div class="ml-3">
                <span class="block text-sm font-medium text-gray-900">{{ method.label }}</span>
                <span class="block text-sm text-gray-500">{{ method.description }}</span>
              </div>
            </label>
          </div>
        </div>

        <!-- Submit -->
        <div class="pt-4 border-t">
          <UiButton
            class="w-full"
            size="lg"
            :loading="processing"
            @click="processPayment"
          >
            {{ amount.toLocaleString() }}원 결제하기
          </UiButton>
          <p class="text-xs text-gray-500 text-center mt-3">
            결제 버튼을 클릭하면 약관에 동의하는 것으로 간주됩니다.
          </p>
        </div>
      </UiCard>
    </div>

    <!-- Success Modal -->
    <UiModal v-model="showSuccessModal" title="결제 완료" :show-close="false" :close-on-backdrop="false">
      <div class="text-center">
        <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100 mb-4">
          <svg class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <p class="text-gray-600">결제가 성공적으로 완료되었습니다.</p>
      </div>
      <template #footer>
        <div class="flex justify-center">
          <UiButton @click="goToReservations">예약 확인하기</UiButton>
        </div>
      </template>
    </UiModal>

    <!-- Error Modal -->
    <UiModal v-model="showErrorModal" title="결제 실패">
      <div class="text-center">
        <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
          <svg class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
        <p class="text-gray-600">{{ errorMessage }}</p>
      </div>
      <template #footer>
        <div class="flex justify-end space-x-3">
          <UiButton variant="secondary" @click="showErrorModal = false">닫기</UiButton>
          <UiButton @click="processPayment">다시 시도</UiButton>
        </div>
      </template>
    </UiModal>
  </div>
</template>

<script setup lang="ts">
import { api } from '~/services/api'
import type { Reservation, PaymentMethod } from '~/types'

definePageMeta({
  middleware: 'auth'
})

const route = useRoute()
const router = useRouter()
const reservationId = Number(route.query.reservationId)

const reservation = ref<Reservation | null>(null)
const amount = ref(0)
const loading = ref(true)
const processing = ref(false)
const selectedMethod = ref<PaymentMethod>('CARD')
const showSuccessModal = ref(false)
const showErrorModal = ref(false)
const errorMessage = ref('')

const paymentMethods = [
  { value: 'CARD' as PaymentMethod, label: '신용/체크카드', description: '카드 결제' },
  { value: 'BANK_TRANSFER' as PaymentMethod, label: '계좌이체', description: '실시간 계좌이체' },
  { value: 'VIRTUAL_ACCOUNT' as PaymentMethod, label: '가상계좌', description: '무통장 입금' }
]

const loadPaymentInfo = async () => {
  if (!reservationId) {
    router.push('/reservations')
    return
  }

  loading.value = true
  try {
    reservation.value = await api.getReservation(reservationId)

    // Get slot price if exists
    if (reservation.value.slotId) {
      const event = await api.getEvent(reservation.value.eventId)
      const slot = event.slots?.find(s => s.id === reservation.value?.slotId)
      amount.value = slot?.price || 0
    }
  } catch (error: any) {
    console.error('Failed to load payment info:', error)
    router.push('/reservations')
  } finally {
    loading.value = false
  }
}

const processPayment = async () => {
  if (!reservationId) return

  processing.value = true
  showErrorModal.value = false

  try {
    await api.createPayment({
      reservationId,
      method: selectedMethod.value
    })
    showSuccessModal.value = true
  } catch (error: any) {
    errorMessage.value = error.message || '결제 처리 중 오류가 발생했습니다.'
    showErrorModal.value = true
  } finally {
    processing.value = false
  }
}

const goToReservations = () => {
  router.push('/reservations')
}

onMounted(() => {
  loadPaymentInfo()
})
</script>
