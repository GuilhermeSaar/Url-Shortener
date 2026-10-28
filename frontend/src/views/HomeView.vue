<script setup>
import { nextTick, onUnmounted, ref } from 'vue'
import { ArrowRight, ArrowUpRight, BarChart3, Check, CornerDownLeft, ExternalLink, Link2, LoaderCircle, LockKeyhole, Plus, X } from 'lucide-vue-next'
import CopyButton from '../components/CopyButton.vue'
import { useHistoryStore } from '../stores/history'
import { errorMessage, shortenUrl } from '../services/api'
import { extractCode, isHttpUrl } from '../utils/links'

const history = useHistoryStore()
const expanded = ref(false)
const url = ref('')
const loading = ref(false)
const error = ref('')
const result = ref(null)
const input = ref(null)
const trigger = ref(null)
const resultPanel = ref(null)
const copyButton = ref(null)
let active = true

async function expand() {
  expanded.value = true
  await nextTick()
  const focusTarget = result.value ? resultPanel.value : input.value
  focusTarget?.focus()
}

async function collapse() {
  if (loading.value) return
  expanded.value = false
  await nextTick()
  trigger.value?.focus()
}

async function startAnother() {
  expanded.value = true
  result.value = null
  url.value = ''
  error.value = ''
  await nextTick()
  input.value?.focus()
}

async function submit() {
  if (loading.value) return
  error.value = ''
  const originalUrl = url.value.trim()
  if (!isHttpUrl(originalUrl)) {
    error.value = 'Cole um endereço válido começando com http:// ou https://.'
    input.value?.focus()
    return
  }
  loading.value = true
  try {
    const data = await shortenUrl(originalUrl)
    const link = { shortUrl: data.response, originalUrl, code: extractCode(data.response), createdAt: new Date().toISOString() }
    history.add(link)
    if (active) {
      result.value = link
      await nextTick()
      resultPanel.value?.focus()
    }
  } catch (cause) {
    if (active) {
      error.value = errorMessage(cause, 'url')
      await nextTick()
      input.value?.focus()
    }
  } finally {
    loading.value = false
  }
}

function handleKeydown(event) {
  if (event.defaultPrevented || event.isComposing) return
  if (event.key === 'Escape' && expanded.value) {
    event.preventDefault()
    collapse()
  }
  if (event.code !== 'Space' || !result.value || !expanded.value || event.repeat
    || event.ctrlKey || event.altKey || event.metaKey || event.shiftKey) return
  if (event.target instanceof Element && event.target.closest('input, textarea, select, button, a, [contenteditable]:not([contenteditable="false"]), [role="button"]')) return
  event.preventDefault()
  copyButton.value?.copy()
}

onUnmounted(() => {
  active = false
})
</script>

<template>
    <div class="workspace workspace-single workspace-home">
      <div class="workspace-intro"><p class="workspace-eyebrow">PEQUENOS LINKS. NOVAS POSSIBILIDADES.</p><h1>Seu link, do início ao clique<span class="brand-dot">.</span></h1><p>Encurte e compartilhe. Sem precisar de uma conta.</p></div>
        <section class="workspace-create" aria-labelledby="create-title">
          <div class="workspace-section-heading"><div><p class="workspace-eyebrow"><Plus :size="13" /> CRIAR</p><h2 id="create-title">Um caminho mais curto<span class="brand-dot">.</span></h2></div></div>
          <div class="widget-stage">
            <div class="link-capsule" :class="{ 'is-expanded': expanded, 'has-result': expanded && result, 'has-error': expanded && error }" @keydown="handleKeydown">
              <Transition name="capsule-content">
                <button v-if="!expanded" ref="trigger" key="idle" type="button" class="capsule-trigger" @click="expand">
                  <span class="capsule-plus"><Plus :size="20" /></span>
                  <span>Encurtar link</span>
                  <span class="capsule-trigger-arrow"><ArrowUpRight :size="17" /></span>
                </button>

                <div v-else-if="result" ref="resultPanel" key="result" class="capsule-content capsule-result" tabindex="-1" aria-label="Link pronto. Pressione Espaço para copiar.">
                  <div class="capsule-heading">
                    <div class="capsule-heading-text"><span class="capsule-status"><Check :size="14" /></span><h3>Pronto para ir além.</h3></div>
                    <button type="button" class="capsule-close" aria-label="Recolher cápsula" @click="collapse"><X :size="17" /></button>
                  </div>
                  <div class="result-link-row">
                    <a :href="result.shortUrl" target="_blank" rel="noopener noreferrer" class="result-link" :title="result.shortUrl"><span class="result-link-text">{{ result.shortUrl }}</span><ExternalLink :size="15" /><span class="sr-only"> (abre em nova aba)</span></a>
                    <CopyButton ref="copyButton" :text="result.shortUrl" />
                  </div>
                  <p class="result-original" :title="result.originalUrl">{{ result.originalUrl }}</p>
                  <div class="capsule-bottom result-bottom">
                    <button type="button" class="new-link-button" @click="startAnother"><Plus :size="14" /> Encurtar outro</button>
                    <RouterLink :to="{ name: 'stats', query: { code: result.code } }" class="result-stats" aria-label="Ver métricas deste link"><BarChart3 :size="15" /><span>Ver métricas</span></RouterLink>
                    <span class="keyboard-hint"><kbd>espaço</kbd> para copiar</span>
                  </div>
                </div>

                <form v-else key="editor" class="capsule-content" novalidate :aria-busy="loading" @submit.prevent="submit">
                  <div class="capsule-heading">
                    <label for="original-url" class="capsule-heading-text"><span class="capsule-label-icon"><Link2 :size="17" /></span><span>Qual link vai ficar menor?</span></label>
                    <button type="button" class="capsule-close" :disabled="loading" aria-label="Recolher cápsula" @click="collapse"><X :size="17" /></button>
                  </div>
                  <div class="capsule-input-row" :class="{ 'input-invalid': error }">
                    <input id="original-url" ref="input" v-model="url" name="url" type="url" placeholder="Cole seu link aqui…" autocomplete="url" autocapitalize="none" spellcheck="false" required :aria-invalid="Boolean(error)" :aria-describedby="error ? 'url-error' : 'url-help'" :readonly="loading" />
                    <button type="submit" class="capsule-submit" :disabled="loading" :aria-label="loading ? 'Encurtando link' : 'Encurtar link'"><LoaderCircle v-if="loading" :size="19" class="animate-spin" /><ArrowRight v-else :size="19" /></button>
                  </div>
                  <p v-if="error" id="url-error" class="capsule-error" role="alert">{{ error }}</p>
                  <div class="capsule-bottom">
                    <p id="url-help" class="capsule-help">{{ loading ? 'Deixando seu link mais leve…' : 'Links HTTP e HTTPS. Sem cadastro.' }}</p>
                    <span class="keyboard-hint"><kbd><CornerDownLeft :size="11" /><span class="sr-only">Enter</span></kbd> encurta <span class="hint-divider">·</span> <kbd>esc</kbd> recolhe</span>
                  </div>
                </form>
              </Transition>
            </div>
            <p class="sr-only" role="status">{{ loading ? 'Encurtando link…' : result && expanded ? 'Seu link está pronto para compartilhar.' : '' }}</p>
          </div>
          <p class="workspace-tip"><Link2 :size="14" /> Menos caracteres. O mesmo destino.</p>
        </section>
      <p class="workspace-footnote"><LockKeyhole :size="12" /> Quer acompanhar os cliques? Entre ou crie uma conta para ver as métricas.</p>
    </div>
</template>
