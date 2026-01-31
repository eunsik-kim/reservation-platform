<template>
  <div :class="['bg-white rounded-lg shadow', padding ? 'p-6' : '']">
    <div v-if="$slots.header" class="border-b border-gray-200 pb-4 mb-4">
      <slot name="header" />
    </div>
    <slot />
    <div v-if="$slots.footer" class="border-t border-gray-200 pt-4 mt-4">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  padding?: boolean
}

withDefaults(defineProps<Props>(), {
  padding: true
})
</script>
