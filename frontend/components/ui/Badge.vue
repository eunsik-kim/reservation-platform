<template>
  <span
    :class="[
      'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
      variantClasses
    ]"
  >
    <slot />
  </span>
</template>

<script setup lang="ts">
interface Props {
  variant?: 'gray' | 'green' | 'yellow' | 'red' | 'blue' | 'indigo'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'gray'
})

const variantClasses = computed(() => {
  switch (props.variant) {
    case 'green':
      return 'bg-green-100 text-green-800'
    case 'yellow':
      return 'bg-yellow-100 text-yellow-800'
    case 'red':
      return 'bg-red-100 text-red-800'
    case 'blue':
      return 'bg-blue-100 text-blue-800'
    case 'indigo':
      return 'bg-indigo-100 text-indigo-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
})
</script>
