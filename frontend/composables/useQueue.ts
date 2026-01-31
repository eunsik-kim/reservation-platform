import type { QueueEntry } from '~/types'

export const useQueue = () => {
  const config = useRuntimeConfig()
  const authStore = useAuthStore()

  const queueEntry = ref<QueueEntry | null>(null)
  const isConnected = ref(false)
  const error = ref<string | null>(null)

  let socket: WebSocket | null = null

  const connect = (eventId: number) => {
    if (socket) {
      socket.close()
    }

    const wsUrl = `${config.public.wsBase}/queue?eventId=${eventId}&token=${authStore.accessToken}`
    socket = new WebSocket(wsUrl)

    socket.onopen = () => {
      isConnected.value = true
      error.value = null
    }

    socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data) as QueueEntry
        queueEntry.value = data
      } catch (e) {
        console.error('Failed to parse queue message:', e)
      }
    }

    socket.onerror = (e) => {
      error.value = 'WebSocket connection error'
      console.error('WebSocket error:', e)
    }

    socket.onclose = () => {
      isConnected.value = false
    }
  }

  const disconnect = () => {
    if (socket) {
      socket.close()
      socket = null
    }
    queueEntry.value = null
    isConnected.value = false
  }

  const enterQueue = async (eventId: number): Promise<boolean> => {
    try {
      await $fetch(`${config.public.apiBase}/queue/enter`, {
        method: 'POST',
        body: { eventId },
        headers: {
          Authorization: `Bearer ${authStore.accessToken}`,
        },
      })
      connect(eventId)
      return true
    } catch (e) {
      console.error('Failed to enter queue:', e)
      return false
    }
  }

  const leaveQueue = async (eventId: number): Promise<boolean> => {
    try {
      await $fetch(`${config.public.apiBase}/queue/leave`, {
        method: 'DELETE',
        body: { eventId },
        headers: {
          Authorization: `Bearer ${authStore.accessToken}`,
        },
      })
      disconnect()
      return true
    } catch (e) {
      console.error('Failed to leave queue:', e)
      return false
    }
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    queueEntry: readonly(queueEntry),
    isConnected: readonly(isConnected),
    error: readonly(error),
    enterQueue,
    leaveQueue,
    disconnect,
  }
}
