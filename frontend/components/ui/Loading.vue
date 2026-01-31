<template>
  <div class="flex items-center justify-center" :class="fullPage ? 'min-h-[60vh]' : ''">
    <div class="text-center">
      <svg
        class="animate-spin mx-auto text-indigo-600"
        :class="sizeClasses"
        fill="none"
        viewBox="0 0 24 24"
      >
        <circle
          class="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="4"
        />
        <path
          class="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        />
      </svg>
      <p v-if="text" class="mt-2 text-sm text-gray-500">{{ text }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  size?: 'sm' | 'md' | 'lg'
  text?: string
  fullPage?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  fullPage: false
})

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm':
      return 'h-6 w-6'
    case 'md':
      return 'h-10 w-10'
    case 'lg':
      return 'h-16 w-16'
    default:
      return 'h-10 w-10'
  }
})
</script>
