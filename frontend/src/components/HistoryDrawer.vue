<script setup>
import { onBeforeUnmount, ref } from 'vue'
import { ArrowUpRight, BarChart3, ExternalLink, GalleryVerticalEnd, Link2, LockKeyhole, Plus, X } from 'lucide-vue-next'
import CopyButton from './CopyButton.vue'
import { useHistoryStore } from '../stores/history'
import { formatDate } from '../utils/links'

const history = useHistoryStore()
const dialog = ref(null)
const emit = defineEmits(['toggle', 'create', 'inspect'])

function open() {
  dialog.value.showModal()
  emit('toggle', true)
}

function close() {
  dialog.value.close()
}

function create() {
  close()
  emit('create')
}

function inspect(link) {
  close()
  emit('inspect', link)
}

function closeOnBackdrop(event) {
  if (event.target !== dialog.value) return
  const bounds = dialog.value.getBoundingClientRect()
  if (event.clientX < bounds.left || event.clientX > bounds.right
    || event.clientY < bounds.top || event.clientY > bounds.bottom) close()
}

onBeforeUnmount(() => dialog.value?.close())
defineExpose({ open })
</script>

<template>
  <dialog id="history-drawer" ref="dialog" class="history-drawer" aria-labelledby="history-title" aria-describedby="history-description" @click="closeOnBackdrop" @close="emit('toggle', false)">
    <div class="drawer-heading">
      <div><span class="drawer-eyebrow"><GalleryVerticalEnd :size="14" /> SEUS LINKS</span><h2 id="history-title">Um lugar para voltar<span class="brand-dot">.</span></h2></div>
      <button type="button" class="capsule-close" aria-label="Fechar histórico" autofocus @click="close"><X :size="19" /></button>
    </div>
    <p id="history-description" class="drawer-description">{{ history.links.length ? `Seus últimos ${history.links.length} links, sempre à mão.` : 'Seus links recentes, sempre à mão.' }}</p>
    <p v-if="history.storageWarning" role="status" class="drawer-warning">{{ history.storageWarning }}</p>
    <div class="drawer-body">
      <div v-if="!history.links.length" class="drawer-empty">
        <span class="drawer-empty-icon"><Link2 :size="25" /></span>
        <h3>Seu próximo link começa aqui.</h3>
        <p>Encurte seu primeiro link.<br />A gente guarda o caminho de volta.</p>
        <button type="button" class="new-link-button" @click="create"><Plus :size="15" /> Encurtar meu primeiro link <ArrowUpRight :size="14" /></button>
      </div>
      <ol v-else class="history-list">
        <li v-for="link in history.links" :key="link.shortUrl" class="history-item">
          <div class="history-item-top"><span class="history-item-icon"><Link2 :size="16" /></span><time :datetime="link.createdAt">{{ formatDate(link.createdAt) }}</time></div>
          <a :href="link.shortUrl" target="_blank" rel="noopener noreferrer" class="history-link" :title="link.shortUrl"><span>{{ link.shortUrl }}</span><ExternalLink :size="13" /><span class="sr-only"> (abre em nova aba)</span></a>
          <p class="history-original" :title="link.originalUrl">{{ link.originalUrl }}</p>
          <div class="history-item-actions"><CopyButton :text="link.shortUrl" compact /><button type="button" class="drawer-stats" :aria-label="`Ver estatísticas do código ${link.code}`" @click="inspect(link)"><BarChart3 :size="15" /> Estatísticas <ArrowUpRight :size="13" /></button></div>
        </li>
      </ol>
    </div>
    <div class="drawer-note"><LockKeyhole :size="13" /><p>Salvo só neste navegador.<br /><span>Até 20 links recentes, sem precisar de conta.</span></p></div>
  </dialog>
</template>
