<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const authStore = useAuthStore()

onMounted(() => {
  authStore.loadFromStorage()
  if (authStore.accessToken) {
    authStore.loadUser()
  }
})
</script>
