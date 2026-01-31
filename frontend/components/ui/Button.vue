<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="[
      'inline-flex items-center justify-center px-4 py-2 border rounded-md font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2',
      variantClasses,
      sizeClasses,
      { 'opacity-50 cursor-not-allowed': disabled || loading }
    ]"
  >
    <svg
      v-if="loading"
      class="animate-spin -ml-1 mr-2 h-4 w-4"
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
    <slot />
  </button>
</template>

<script setup lang="ts">
interface Props {
  type?: 'button' | 'submit' | 'reset'
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'button',
  variant: 'primary',
  size: 'md',
  disabled: false,
  loading: false
})

const variantClasses = computed(() => {
  switch (props.variant) {
    case 'primary':
      return 'border-transparent bg-indigo-600 text-white hover:bg-indigo-700 focus:ring-indigo-500'
    case 'secondary':
      return 'border-gray-300 bg-white text-gray-700 hover:bg-gray-50 focus:ring-indigo-500'
    case 'danger':
      return 'border-transparent bg-red-600 text-white hover:bg-red-700 focus:ring-red-500'
    case 'ghost':
      return 'border-transparent bg-transparent text-gray-700 hover:bg-gray-100 focus:ring-gray-500'
    default:
      return ''
  }
})

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm':
      return 'text-sm px-3 py-1.5'
    case 'md':
      return 'text-sm px-4 py-2'
    case 'lg':
      return 'text-base px-6 py-3'
    default:
      return ''
  }
})
</script>
