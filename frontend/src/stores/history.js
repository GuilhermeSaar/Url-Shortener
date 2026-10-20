import { defineStore } from 'pinia'
import { ref } from 'vue'
import { isHttpUrl } from '../utils/links.js'

const STORAGE_KEY = 'elo:link-history:v1'
const HISTORY_LIMIT = 20

export const useHistoryStore = defineStore('history', () => {
  const links = ref([])
  const storageWarning = ref('')

  try {
    const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    if (Array.isArray(stored)) {
      links.value = stored.filter((item) => item && typeof item.code === 'string'
        && /^[a-zA-Z0-9]+$/.test(item.code) && isHttpUrl(item.shortUrl)
        && isHttpUrl(item.originalUrl) && !Number.isNaN(Date.parse(item.createdAt))).slice(0, HISTORY_LIMIT)
    }
  } catch {
    storageWarning.value = 'Não foi possível recuperar o histórico deste navegador.'
  }

  function add(link) {
    links.value = [link, ...links.value.filter((item) => item.shortUrl !== link.shortUrl)].slice(0, HISTORY_LIMIT)
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(links.value))
      storageWarning.value = ''
    } catch {
      storageWarning.value = 'O histórico ficará disponível apenas nesta sessão: o armazenamento local está indisponível.'
    }
  }

  return { links, storageWarning, add }
})
