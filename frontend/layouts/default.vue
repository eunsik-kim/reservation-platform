<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <header class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo -->
          <NuxtLink to="/" class="flex items-center space-x-2">
            <span class="text-xl font-bold text-indigo-600">Reservation</span>
          </NuxtLink>

          <!-- Navigation -->
          <nav class="hidden md:flex items-center space-x-4">
            <NuxtLink
              to="/"
              class="text-gray-600 hover:text-indigo-600 px-3 py-2 rounded-md text-sm font-medium"
            >
              이벤트 목록
            </NuxtLink>
            <template v-if="isLoggedIn">
              <NuxtLink
                to="/events/my"
                class="text-gray-600 hover:text-indigo-600 px-3 py-2 rounded-md text-sm font-medium"
              >
                내 이벤트
              </NuxtLink>
              <NuxtLink
                to="/reservations"
                class="text-gray-600 hover:text-indigo-600 px-3 py-2 rounded-md text-sm font-medium"
              >
                내 예약
              </NuxtLink>
            </template>
          </nav>

          <!-- Auth Buttons -->
          <div class="flex items-center space-x-4">
            <template v-if="isLoggedIn">
              <span class="text-sm text-gray-600">{{ user?.name }}님</span>
              <button
                @click="handleLogout"
                class="text-gray-600 hover:text-indigo-600 text-sm font-medium"
              >
                로그아웃
              </button>
            </template>
            <template v-else>
              <NuxtLink
                to="/login"
                class="text-gray-600 hover:text-indigo-600 text-sm font-medium"
              >
                로그인
              </NuxtLink>
              <NuxtLink
                to="/signup"
                class="bg-indigo-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-indigo-700"
              >
                회원가입
              </NuxtLink>
            </template>
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <slot />
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t mt-auto">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <p class="text-center text-gray-500 text-sm">
          &copy; 2024 Reservation Platform. All rights reserved.
        </p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const isLoggedIn = computed(() => authStore.isLoggedIn)
const user = computed(() => authStore.user)

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>
