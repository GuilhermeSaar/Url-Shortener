<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { GalleryVerticalEnd, LockKeyhole } from 'lucide-vue-next'
import HistoryDrawer from '../components/HistoryDrawer.vue'
import StatsPanel from '../components/StatsPanel.vue'
import { useHistoryStore } from '../stores/history.js'

const router = useRouter()
const history = useHistoryStore()
const drawer = ref(null)
const historyOpen = ref(false)
const statsPanel = ref(null)

function showStats(link) {
  statsPanel.value?.inspect(link.shortUrl, true)
}
</script>

<template>
  <div class="workspace workspace-single workspace-metrics">
    <div class="workspace-intro"><p class="workspace-eyebrow">CADA LINK TEM UMA HISTÓRIA.</p><h1>Conheça o alcance dos seus links<span class="brand-dot">.</span></h1><p>Consulte os cliques, a data de criação e o destino de um link.</p></div>
    <StatsPanel ref="statsPanel" />
    <p class="workspace-footnote"><LockKeyhole :size="12" /> Seu histórico fica disponível somente nesta aba.</p>
  </div>

  <button type="button" class="history-trigger" aria-haspopup="dialog" :aria-expanded="historyOpen" aria-controls="history-drawer" @click="drawer.open()">
    <GalleryVerticalEnd :size="18" /><span>Histórico</span><span v-if="history.links.length" class="history-count">{{ history.links.length }}</span>
  </button>
  <HistoryDrawer ref="drawer" @toggle="historyOpen = $event" @create="router.push({ name: 'home' })" @inspect="showStats" />
</template>
