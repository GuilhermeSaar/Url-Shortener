<script setup>
import { onUnmounted, ref, watch } from 'vue'
import { Check, Copy } from 'lucide-vue-next'

const props = defineProps({ text: { type: String, required: true }, compact: Boolean })
const copied = ref(false)
const failed = ref(false)
let timer
onUnmounted(() => clearTimeout(timer))
watch(() => props.text, () => {
  clearTimeout(timer)
  copied.value = false
  failed.value = false
})

async function copy() {
  clearTimeout(timer)
  copied.value = false
  failed.value = false
  try {
    await navigator.clipboard.writeText(props.text)
    copied.value = true
  } catch {
    failed.value = true
  }
  timer = setTimeout(() => { copied.value = false; failed.value = false }, 3500)
}
defineExpose({ copy })
</script>

<template>
  <span class="inline-flex flex-col items-end gap-1">
    <button type="button" :class="compact ? 'icon-button' : 'secondary-button'" :aria-label="copied ? 'Link copiado' : 'Copiar link'" @click="copy">
      <Check v-if="copied" :size="16" class="copy-success" />
      <Copy v-else :size="16" />
      <span v-if="!compact">{{ copied ? 'Copiado!' : 'Copiar link' }}</span>
    </button>
    <span v-if="copied || failed" role="status" class="text-xs" :class="[failed ? 'copy-error' : 'copy-success', { 'sr-only': copied && !compact, 'copy-feedback': !compact }]">
      {{ failed ? 'Selecione o link e copie manualmente.' : 'Link copiado.' }}
    </span>
  </span>
</template>
