<script setup>
import { computed, nextTick, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Check, Link2, LoaderCircle, Plus, RefreshCw } from 'lucide-vue-next'
import CopyButton from '../components/CopyButton.vue'
import LinkList from '../components/LinkList.vue'
import { useUrlHistory } from '../composables/useUrlHistory'
import { useAuthStore } from '../stores/auth'
import { errorMessage, shortenUrl } from '../services/api'
import { extractCode, isHttpUrl } from '../utils/links'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { links, loading, error, hasLoaded, load } = useUrlHistory({
  onSessionExpired: () => router.replace({ name: 'login', query: route.query }),
})
const number = new Intl.NumberFormat('pt-BR')
const totalClicks = computed(() => links.value.reduce((sum, link) => sum + link.clickCount, 0))
const selectedCode = computed(() => {
  const value = route.query.code
  return typeof value === 'string' && /^[a-zA-Z0-9]+$/.test(value) ? value : ''
})
const selectionMessage = computed(() => {
  if (!('code' in route.query)) return ''
  if (!selectedCode.value) return 'O código informado é inválido. Selecione um link da lista.'
  if (hasLoaded.value && !links.value.some((link) => link.code === selectedCode.value)) {
    return 'Esse link não está disponível na sua conta. Ele pode ter expirado.'
  }
  return ''
})
const url = ref('')
const creating = ref(false)
const createError = ref('')
const created = ref(null)
const input = ref(null)
const result = ref(null)
let creationController

async function create() {
  if (creating.value) return
  createError.value = ''
  const originalUrl = url.value.trim()
  if (!isHttpUrl(originalUrl)) {
    createError.value = 'Cole um endereço válido começando com http:// ou https://.'
    input.value?.focus()
    return
  }
  const request = new AbortController()
  creationController = request
  creating.value = true
  try {
    const data = await shortenUrl(originalUrl, request.signal)
    if (request.signal.aborted || !auth.isAuthenticated) return
    created.value = { shortUrl: data.response, code: extractCode(data.response) }
    url.value = ''
    await nextTick()
    result.value?.focus()
    load()
  } catch (cause) {
    if (request.signal.aborted) return
    createError.value = errorMessage(cause, 'url')
  } finally {
    if (!request.signal.aborted) creating.value = false
  }
}

function select(code) {
  const query = { ...route.query }
  if (selectedCode.value === code) delete query.code
  else query.code = code
  router.replace({ name: 'links', query })
}

watch(() => auth.isAuthenticated, (authenticated) => {
  if (!authenticated) {
    creationController?.abort()
    created.value = null
    createError.value = ''
    url.value = ''
    creating.value = false
  }
}, { flush: 'sync' })
onUnmounted(() => creationController?.abort())
</script>

<template>
  <div v-if="auth.isAuthenticated" class="workspace links-page">
    <div class="links-page-intro"><p class="workspace-eyebrow">MEUS LINKS</p><h1>Seus links, à mão<span class="brand-dot">.</span></h1><p>Crie, compartilhe e acompanhe em um só lugar.</p></div>

    <section class="account-create" aria-labelledby="account-create-title">
      <form novalidate :aria-busy="creating" @submit.prevent="create">
        <label id="account-create-title" for="account-url">Encurtar um novo link</label>
        <div class="account-create-row">
          <input id="account-url" ref="input" v-model="url" type="url" required placeholder="Cole uma URL começando com https://" autocomplete="url" autocapitalize="none" spellcheck="false" :readonly="creating" :aria-invalid="Boolean(createError)" :aria-describedby="createError ? 'account-create-error' : undefined" />
          <button type="submit" class="account-primary-button" :disabled="creating"><LoaderCircle v-if="creating" :size="17" class="animate-spin" aria-hidden="true" /><Plus v-else :size="17" aria-hidden="true" />{{ creating ? 'Encurtando…' : 'Encurtar link' }}</button>
        </div>
        <p v-if="createError" id="account-create-error" class="account-error" role="alert">{{ createError }}</p>
      </form>
      <div v-if="created" ref="result" class="account-create-result" tabindex="-1" role="status">
        <span class="account-create-success"><Check :size="16" aria-hidden="true" /> Link pronto para compartilhar.</span>
        <div><a :href="created.shortUrl" target="_blank" rel="noopener noreferrer">{{ created.shortUrl }}<span class="sr-only"> (abre em nova aba e contabiliza um clique)</span></a><CopyButton :text="created.shortUrl" /></div>
      </div>
    </section>

    <dl class="account-summary" aria-label="Resumo da conta">
      <div><dt>Links na conta</dt><dd>{{ hasLoaded ? number.format(links.length) : '—' }}</dd></div>
      <div><dt>Cliques acumulados</dt><dd>{{ hasLoaded ? number.format(totalClicks) : '—' }}</dd></div>
    </dl>

    <section class="account-history" aria-labelledby="account-history-title" :aria-busy="loading">
      <div class="account-history-heading"><div><h2 id="account-history-title">Meus links</h2><p>Mais recentes primeiro</p></div><button type="button" class="account-refresh-button" :disabled="loading" @click="load"><RefreshCw :size="15" :class="{ 'animate-spin': loading }" aria-hidden="true" />{{ loading ? 'Atualizando…' : 'Atualizar' }}</button></div>
      <div v-if="error" class="account-error-state" role="alert"><p>{{ error }}</p><button type="button" class="account-details-button" :disabled="loading" @click="load">Tentar novamente</button></div>
      <p v-if="selectionMessage" class="account-selection-message" role="status">{{ selectionMessage }}</p>
      <div v-if="!hasLoaded && !error" class="account-empty-state" role="status"><LoaderCircle :size="24" class="animate-spin" aria-hidden="true" /><p>Buscando seus links…</p></div>
      <template v-else-if="hasLoaded">
        <LinkList v-if="links.length" :links="links" :selected-code="selectedCode" :created-code="created?.code || ''" @select="select" />
        <div v-else class="account-empty-state"><span class="account-empty-icon"><Link2 :size="26" aria-hidden="true" /></span><h3>Seu primeiro link começa aqui.</h3><p>Encurte um link para guardá-lo na sua conta e acompanhar os cliques.</p><button type="button" class="account-primary-button" @click="input?.focus()"><Plus :size="16" aria-hidden="true" />Criar meu primeiro link</button></div>
      </template>
    </section>
  </div>
</template>
