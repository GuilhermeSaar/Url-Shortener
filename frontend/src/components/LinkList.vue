<script setup>
import { ChevronDown, ExternalLink, Link2 } from 'lucide-vue-next'
import CopyButton from './CopyButton.vue'
import { formatDate } from '../utils/links'

defineProps({
  links: { type: Array, required: true },
  selectedCode: { type: String, default: '' },
  createdCode: { type: String, default: '' },
})
const emit = defineEmits(['select'])
const number = new Intl.NumberFormat('pt-BR')
</script>

<template>
  <div class="account-link-list">
    <div class="account-list-labels" aria-hidden="true"><span>Link curto</span><span>Ações</span></div>
    <ul>
      <li v-for="link in links" :key="link.code" class="account-link" :class="{ 'is-selected': selectedCode === link.code, 'is-new': createdCode === link.code }">
        <div class="account-link-row">
          <div class="account-link-info">
            <div class="account-short-row"><Link2 :size="15" aria-hidden="true" /><a :href="link.shortUrl" target="_blank" rel="noopener noreferrer" :title="link.shortUrl" class="account-short-url">{{ link.shortUrl }}<span class="sr-only"> (abre em nova aba e contabiliza um clique)</span></a><span v-if="createdCode === link.code" class="account-new-label">Novo</span></div>
          </div>
          <div class="account-link-actions">
            <CopyButton :text="link.shortUrl" />
            <button type="button" class="account-details-button" :aria-expanded="selectedCode === link.code" :aria-controls="`details-${link.code}`" :aria-label="`${selectedCode === link.code ? 'Fechar' : 'Ver'} detalhes do link ${link.code}`" @click="emit('select', link.code)">
              {{ selectedCode === link.code ? 'Fechar' : 'Detalhes' }}<ChevronDown :size="15" aria-hidden="true" />
            </button>
          </div>
        </div>
        <section v-if="selectedCode === link.code" :id="`details-${link.code}`" class="account-link-details" :aria-label="`Detalhes do link ${link.code}`">
          <dl>
            <div><dt>Destino</dt><dd><a :href="link.originalUrl" target="_blank" rel="noopener noreferrer">{{ link.originalUrl }}<ExternalLink :size="13" aria-hidden="true" /><span class="sr-only"> (abre em nova aba)</span></a></dd></div>
            <div><dt>Criado em</dt><dd><time :datetime="link.createdAt">{{ formatDate(link.createdAt) }}</time></dd></div>
            <div><dt>Total de cliques</dt><dd>{{ number.format(link.clickCount) }}</dd></div>
          </dl>
        </section>
      </li>
    </ul>
  </div>
</template>
