import { defineStore } from 'pinia'
import { ref } from 'vue'
const HISTORY_LIMIT = 20

export const useHistoryStore = defineStore('history', () => {
  const links = ref([])

  function add(link) {
    links.value = [link, ...links.value.filter((item) => item.shortUrl !== link.shortUrl)].slice(0, HISTORY_LIMIT)
  }

  return { links, add }
})
