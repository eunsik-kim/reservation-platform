<template>
  <div>
    <div class="mb-6">
      <NuxtLink to="/events/my" class="text-indigo-600 hover:text-indigo-800 text-sm">
        &larr; 내 이벤트 목록으로
      </NuxtLink>
    </div>

    <UiLoading v-if="loading" full-page text="이벤트 정보를 불러오는 중..." />

    <UiCard v-else>
      <template #header>
        <h1 class="text-xl font-bold text-gray-900">이벤트 수정</h1>
      </template>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        <UiInput
          v-model="form.title"
          label="이벤트 제목"
          placeholder="이벤트 제목을 입력하세요"
          required
          :error="errors.title"
        />

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">설명</label>
          <textarea
            v-model="form.description"
            rows="4"
            class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            placeholder="이벤트에 대한 설명을 입력하세요"
          />
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <UiInput
            v-model="form.openAt"
            type="datetime-local"
            label="예약 오픈 시간"
            required
            :error="errors.openAt"
          />
          <UiInput
            v-model="form.closeAt"
            type="datetime-local"
            label="예약 마감 시간"
            required
            :error="errors.closeAt"
          />
        </div>

        <UiInput
          v-model.number="form.maxParticipants"
          type="number"
          label="최대 참가자 수"
          min="1"
          required
          :error="errors.maxParticipants"
        />

        <!-- Settings -->
        <div class="border-t pt-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">이벤트 설정</h3>

          <div class="space-y-4">
            <label class="flex items-center">
              <input
                type="checkbox"
                v-model="form.settings.useQueue"
                class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
              />
              <span class="ml-2 text-sm text-gray-700">대기열 사용</span>
            </label>

            <div v-if="form.settings.useQueue" class="ml-6">
              <UiInput
                v-model.number="form.settings.queueBatchSize"
                type="number"
                label="한 번에 입장시킬 인원"
                min="1"
              />
            </div>

            <label class="flex items-center">
              <input
                type="checkbox"
                v-model="form.settings.requirePayment"
                class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
              />
              <span class="ml-2 text-sm text-gray-700">결제 필요</span>
            </label>

            <UiInput
              v-model.number="form.settings.maxReservationsPerUser"
              type="number"
              label="1인당 최대 예약 수"
              min="1"
            />
          </div>
        </div>

        <div class="flex justify-end space-x-3 pt-6 border-t">
          <NuxtLink to="/events/my">
            <UiButton variant="secondary" type="button">취소</UiButton>
          </NuxtLink>
          <UiButton type="submit" :loading="submitting">저장</UiButton>
        </div>
      </form>
    </UiCard>
  </div>
</template>

<script setup lang="ts">
import { api } from '~/services/api'
import type { EventSettings } from '~/types'

definePageMeta({
  middleware: 'auth'
})

const route = useRoute()
const router = useRouter()
const eventId = Number(route.params.id)

const loading = ref(true)
const submitting = ref(false)
const errors = ref<Record<string, string>>({})

const form = ref({
  title: '',
  description: '',
  openAt: '',
  closeAt: '',
  maxParticipants: 100,
  settings: {
    useQueue: true,
    queueBatchSize: 100,
    requirePayment: false,
    maxReservationsPerUser: 1,
    reservationTimeLimit: 600
  } as EventSettings
})

const loadEvent = async () => {
  loading.value = true
  try {
    const event = await api.getEvent(eventId)
    form.value = {
      title: event.title,
      description: event.description || '',
      openAt: formatDateTimeLocal(event.openAt),
      closeAt: formatDateTimeLocal(event.closeAt),
      maxParticipants: event.maxParticipants,
      settings: event.settings
    }
  } catch (error: any) {
    console.error('Failed to load event:', error)
    router.push('/events/my')
  } finally {
    loading.value = false
  }
}

const formatDateTimeLocal = (dateString: string) => {
  const date = new Date(dateString)
  return date.toISOString().slice(0, 16)
}

const validate = () => {
  errors.value = {}

  if (!form.value.title.trim()) {
    errors.value.title = '제목을 입력해주세요'
  }

  if (!form.value.openAt) {
    errors.value.openAt = '오픈 시간을 선택해주세요'
  }

  if (!form.value.closeAt) {
    errors.value.closeAt = '마감 시간을 선택해주세요'
  }

  if (form.value.openAt && form.value.closeAt && new Date(form.value.openAt) >= new Date(form.value.closeAt)) {
    errors.value.closeAt = '마감 시간은 오픈 시간 이후여야 합니다'
  }

  if (form.value.maxParticipants < 1) {
    errors.value.maxParticipants = '최대 참가자 수는 1 이상이어야 합니다'
  }

  return Object.keys(errors.value).length === 0
}

const handleSubmit = async () => {
  if (!validate()) return

  submitting.value = true
  try {
    await api.updateEvent(eventId, {
      title: form.value.title,
      description: form.value.description || undefined,
      openAt: new Date(form.value.openAt).toISOString(),
      closeAt: new Date(form.value.closeAt).toISOString(),
      maxParticipants: form.value.maxParticipants,
      settings: form.value.settings
    })
    router.push('/events/my')
  } catch (error: any) {
    alert(error.message)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadEvent()
})
</script>
