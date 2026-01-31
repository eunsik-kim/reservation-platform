<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900">내 이벤트</h1>
      <NuxtLink to="/events/create">
        <UiButton>
          <svg class="w-5 h-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          새 이벤트 만들기
        </UiButton>
      </NuxtLink>
    </div>

    <UiLoading v-if="loading" full-page text="이벤트 목록을 불러오는 중..." />

    <div v-else-if="events.length === 0" class="text-center py-12">
      <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
      <h3 class="mt-2 text-sm font-medium text-gray-900">이벤트가 없습니다</h3>
      <p class="mt-1 text-sm text-gray-500">새로운 이벤트를 만들어보세요.</p>
      <div class="mt-6">
        <NuxtLink to="/events/create">
          <UiButton>새 이벤트 만들기</UiButton>
        </NuxtLink>
      </div>
    </div>

    <div v-else class="space-y-4">
      <UiCard v-for="event in events" :key="event.id">
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center space-x-3 mb-2">
              <NuxtLink :to="`/events/${event.id}`" class="text-lg font-semibold text-gray-900 hover:text-indigo-600">
                {{ event.title }}
              </NuxtLink>
              <UiBadge :variant="statusVariant(event.status)">
                {{ statusText(event.status) }}
              </UiBadge>
            </div>

            <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm text-gray-500 mb-4">
              <div>
                <span class="block text-gray-400">오픈 시간</span>
                {{ formatDate(event.openAt) }}
              </div>
              <div>
                <span class="block text-gray-400">마감 시간</span>
                {{ formatDate(event.closeAt) }}
              </div>
              <div>
                <span class="block text-gray-400">참가자</span>
                {{ event.currentParticipants }} / {{ event.maxParticipants }}명
              </div>
              <div>
                <span class="block text-gray-400">생성일</span>
                {{ formatDate(event.createdAt) }}
              </div>
            </div>

            <!-- Progress bar -->
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div
                class="h-2 rounded-full transition-all"
                :class="progressColor(event)"
                :style="{ width: `${progressPercent(event)}%` }"
              />
            </div>
          </div>

          <div class="ml-4 flex flex-col space-y-2">
            <NuxtLink :to="`/events/${event.id}/edit`">
              <UiButton variant="secondary" size="sm">수정</UiButton>
            </NuxtLink>
            <UiButton
              v-if="event.status === 'DRAFT'"
              size="sm"
              @click="publishEvent(event.id)"
            >
              게시
            </UiButton>
            <NuxtLink :to="`/events/${event.id}/participants`">
              <UiButton variant="ghost" size="sm">참가자 보기</UiButton>
            </NuxtLink>
            <UiButton
              v-if="event.status === 'DRAFT'"
              variant="danger"
              size="sm"
              @click="deleteEvent(event.id)"
            >
              삭제
            </UiButton>
          </div>
        </div>
      </UiCard>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center mt-6 space-x-2">
        <button
          v-for="page in totalPages"
          :key="page"
          @click="loadEvents(page - 1)"
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

    <!-- Delete Confirmation Modal -->
    <UiModal v-model="showDeleteModal" title="이벤트 삭제">
      <p class="text-gray-600">정말로 이 이벤트를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.</p>
      <template #footer>
        <div class="flex justify-end space-x-3">
          <UiButton variant="secondary" @click="showDeleteModal = false">
            취소
          </UiButton>
          <UiButton variant="danger" :loading="deleting" @click="confirmDelete">
            삭제
          </UiButton>
        </div>
      </template>
    </UiModal>
  </div>
</template>

<script setup lang="ts">
import { api } from '~/services/api'
import type { Event } from '~/types'

definePageMeta({
  middleware: 'auth'
})

const events = ref<Event[]>([])
const loading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)
const showDeleteModal = ref(false)
const deleting = ref(false)
const selectedEventId = ref<number | null>(null)

const loadEvents = async (page = 0) => {
  loading.value = true
  try {
    const response = await api.getMyEvents(page, 10)
    events.value = response.events
    currentPage.value = response.currentPage
    totalPages.value = response.totalPages
  } catch (error: any) {
    console.error('Failed to load events:', error)
  } finally {
    loading.value = false
  }
}

const publishEvent = async (id: number) => {
  try {
    await api.publishEvent(id)
    await loadEvents(currentPage.value)
  } catch (error: any) {
    alert(error.message)
  }
}

const deleteEvent = (id: number) => {
  selectedEventId.value = id
  showDeleteModal.value = true
}

const confirmDelete = async () => {
  if (!selectedEventId.value) return

  deleting.value = true
  try {
    await api.deleteEvent(selectedEventId.value)
    showDeleteModal.value = false
    await loadEvents(currentPage.value)
  } catch (error: any) {
    alert(error.message)
  } finally {
    deleting.value = false
  }
}

const statusVariant = (status: string) => {
  switch (status) {
    case 'OPEN':
      return 'green'
    case 'SCHEDULED':
      return 'blue'
    case 'DRAFT':
      return 'yellow'
    case 'CLOSED':
      return 'gray'
    case 'CANCELLED':
      return 'red'
    default:
      return 'gray'
  }
}

const statusText = (status: string) => {
  switch (status) {
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
      return status
  }
}

const progressPercent = (event: Event) => {
  if (event.maxParticipants === 0) return 0
  return Math.min(100, (event.currentParticipants / event.maxParticipants) * 100)
}

const progressColor = (event: Event) => {
  const percent = progressPercent(event)
  if (percent >= 100) return 'bg-red-500'
  if (percent >= 80) return 'bg-yellow-500'
  return 'bg-indigo-600'
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
  loadEvents()
})
</script>
