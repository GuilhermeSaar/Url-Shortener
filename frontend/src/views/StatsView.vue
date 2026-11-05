<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { GalleryVerticalEnd } from 'lucide-vue-next'
import HistoryDrawer from '../components/HistoryDrawer.vue'
import StatsPanel from '../components/StatsPanel.vue'
import { useUrlHistory } from '../composables/useUrlHistory'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const drawer = ref(null)
const statsPanel = ref(null)
const historyOpen = ref(false)
const { links, loading, error, load: loadHistory } = useUrlHistory({
  onSessionExpired: () => router.replace({ name: 'login' }),
})

function showStats(link) {
  statsPanel.value?.inspect(link.code, true)
}

</script>

<template>
  <div class="workspace workspace-single workspace-metrics">
    <div class="workspace-intro"><p class="workspace-eyebrow">CADA LINK TEM UMA HISTÓRIA.</p><h1>Conheça o alcance dos seus links<span class="brand-dot">.</span></h1><p>Consulte os cliques, a data de criação e o destino de um link.</p></div>
    <StatsPanel ref="statsPanel" :links="links" />
  </div>

  <button v-if="auth.isAuthenticated" type="button" class="history-trigger" aria-haspopup="dialog" :aria-expanded="historyOpen" aria-controls="history-drawer" @click="drawer.open()">
    <GalleryVerticalEnd :size="18" /><span>Histórico</span><span v-if="links.length" class="history-count">{{ links.length }}</span>
  </button>
  <HistoryDrawer v-if="auth.isAuthenticated" ref="drawer" :links="links" :loading="loading" :error="error" @toggle="historyOpen = $event" @create="router.push({ name: 'home' })" @inspect="showStats" @retry="loadHistory" />
</template>
