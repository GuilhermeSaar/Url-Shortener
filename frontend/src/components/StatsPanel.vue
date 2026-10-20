<script setup>
import { computed, nextTick, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowUpRight, BarChart3, CalendarDays, ExternalLink, Link2, LoaderCircle, MousePointer2, RefreshCw, Search, Unlink } from 'lucide-vue-next'
import CopyButton from './CopyButton.vue'
import { errorMessage, getUrlStats } from '../services/api'
import { extractCode, formatDate, isHttpUrl } from '../utils/links'
import { useHistoryStore } from '../stores/history'

const route = useRoute()
const router = useRouter()
const history = useHistoryStore()
const panel = ref(null)
const heading = ref(null)
const query = ref('')
const inputError = ref('')
const error = ref('')
const notFound = ref(false)
const loading = ref(false)
const stats = ref(null)
const activeCode = ref('')
const submittedLink = ref(null)
const shortUrl = computed(() => {
  if (!stats.value) return ''
  const selected = submittedLink.value
  if (selected && selected.code === activeCode.value) return selected.url
  return history.links.find((link) => link.code === activeCode.value)?.shortUrl || ''
})
const numberFormatter = new Intl.NumberFormat('pt-BR')
let controller

async function load(code) {
  controller?.abort()
  stats.value = null
  error.value = ''
  inputError.value = ''
  notFound.value = false
  activeCode.value = ''
  loading.value = false
  if (!code) return
  try {
    if (typeof code !== 'string' || extractCode(code) !== code) throw new Error('Informe um código válido ou uma URL curta completa.')
  } catch (cause) {
    inputError.value = cause.message
    return
  }
  const requestController = new AbortController()
  controller = requestController
  activeCode.value = code
  loading.value = true
  try {
    const data = await getUrlStats(code, requestController.signal)
    if (requestController.signal.aborted) return
    stats.value = data
  } catch (cause) {
    if (requestController.signal.aborted) return
    notFound.value = cause.response?.status === 404
    if (!notFound.value) error.value = errorMessage(cause)
  } finally {
    if (!requestController.signal.aborted) loading.value = false
  }
}

watch(() => route.query.code, (code) => {
  const selected = submittedLink.value
  query.value = typeof code === 'string' ? code : ''
  if (selected && selected.code === code) query.value = selected.url
  load(code)
}, { immediate: true })
onUnmounted(() => controller?.abort())

function search() {
  inputError.value = ''
  try {
    const value = query.value.trim()
    const code = extractCode(value)
    submittedLink.value = isHttpUrl(value) ? { code, url: value } : null
    if (route.query.code === code) load(code)
    else router.replace({ name: 'home', query: { ...route.query, code } })
  } catch (cause) {
    inputError.value = cause.message
  }
}

async function inspect(value, focus = false) {
  query.value = value
  search()
  if (focus) {
    await nextTick()
    heading.value?.focus({ preventScroll: true })
    panel.value?.scrollIntoView({ block: 'nearest', behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth' })
  }
}

defineExpose({ inspect })
</script>

<template>
  <section id="link-stats" ref="panel" class="workspace-stats" aria-labelledby="stats-title">
    <div class="workspace-section-heading">
      <div><p class="workspace-eyebrow"><BarChart3 :size="13" /> ACOMPANHAR</p><h2 id="stats-title" ref="heading" tabindex="-1">Cada clique conta<span class="brand-dot">.</span></h2></div>
      <button v-if="activeCode" type="button" class="stats-refresh" :disabled="loading" aria-label="Atualizar estatísticas" @click="load(activeCode)"><RefreshCw :size="15" :class="{ 'animate-spin': loading }" /></button>
      <span v-else class="workspace-step">02</span>
    </div>
    <form class="stats-search" novalidate @submit.prevent="search">
      <label for="stats-query" class="sr-only">Código ou URL curta para consultar estatísticas</label>
      <div class="stats-search-field"><Search :size="16" /><input id="stats-query" v-model="query" name="code" placeholder="Código ou link curto" autocomplete="off" autocapitalize="none" spellcheck="false" :aria-invalid="Boolean(inputError)" :aria-describedby="inputError ? 'search-error' : 'stats-help'" /><button type="submit" class="stats-search-button" aria-label="Consultar estatísticas"><ArrowUpRight :size="18" /></button></div>
      <p v-if="inputError" id="search-error" role="alert" class="stats-error">{{ inputError }}</p>
      <p id="stats-help" class="stats-search-help">Busque um link ou selecione um no histórico.</p>
    </form>

    <div class="stats-content" aria-live="polite" :aria-busy="loading">
      <div v-if="loading" class="stats-state" role="status"><span class="stats-state-icon"><LoaderCircle :size="24" class="animate-spin" /></span><p>Contando cada conexão…</p><span>Buscando as estatísticas do link.</span></div>
      <div v-else-if="notFound" class="stats-state"><span class="stats-state-icon"><Unlink :size="24" /></span><h3>Esse link não foi encontrado.</h3><p>Confira o código. O link pode não existir ou ter expirado.</p></div>
      <div v-else-if="error" class="stats-state"><p role="alert" class="stats-error">{{ error }}</p><button type="button" class="new-link-button" @click="load(activeCode)"><RefreshCw :size="14" /> Tentar novamente</button></div>
      <template v-else>
        <div class="stats-selection"><span class="stats-selection-dot" :class="{ 'is-active': stats }" /><span>{{ stats ? 'Acompanhando' : 'Nenhum link selecionado' }}</span><code v-if="stats">{{ activeCode }}</code></div>
        <div class="stats-metrics">
          <article class="stats-metric"><p>Total de cliques <MousePointer2 :size="15" /></p><strong>{{ stats ? numberFormatter.format(stats.clickCount) : '—' }}</strong><span>{{ stats ? 'Acessos pelo link curto' : 'Os cliques aparecem aqui' }}</span></article>
          <article class="stats-metric"><p>Criado em <CalendarDays :size="15" /></p><strong class="stats-date">{{ stats ? formatDate(stats.date) : '—' }}</strong><span>{{ stats ? 'Data de criação do link' : 'Uma história para começar' }}</span></article>
        </div>
        <div class="stats-destination"><p><Link2 :size="14" /> URL de destino</p><a v-if="stats" :href="stats.urlOriginal" target="_blank" rel="noopener noreferrer" :title="stats.urlOriginal"><span>{{ stats.urlOriginal }}</span><ExternalLink :size="13" /><span class="sr-only"> (abre o destino em nova aba)</span></a><span v-else>Encurte um link para acompanhar seu alcance.</span></div>
        <div v-if="shortUrl" class="stats-link-actions"><a :href="shortUrl" target="_blank" rel="noopener noreferrer">Abrir link curto <ArrowUpRight :size="14" /><span class="sr-only"> (abre em nova aba e contabiliza um clique)</span></a><CopyButton :text="shortUrl" /></div>
      </template>
    </div>
  </section>
</template>
