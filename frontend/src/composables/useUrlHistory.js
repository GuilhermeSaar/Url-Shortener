import { onMounted, onUnmounted, ref, watch } from 'vue'
import { errorMessage, getUrlHistory } from '../services/api.js'
import { useAuthStore } from '../stores/auth.js'

export function useUrlHistory({ onSessionExpired } = {}) {
  const auth = useAuthStore()
  const links = ref([])
  const loading = ref(false)
  const error = ref('')
  let controller
  let disposed = false

  function clear() {
    controller?.abort()
    links.value = []
    loading.value = false
    error.value = ''
  }

  async function load() {
    clear()
    if (!auth.isAuthenticated) return
    const request = new AbortController()
    controller = request
    loading.value = true
    try {
      const result = await getUrlHistory(request.signal)
      if (!request.signal.aborted && auth.isAuthenticated) links.value = result
    } catch (cause) {
      if (request.signal.aborted) return
      if (cause?.response?.status === 401) {
        try { await auth.signOut() } catch { /* The session cannot be used. */ }
        if (!disposed && !auth.isAuthenticated && onSessionExpired) onSessionExpired()
      } else {
        error.value = errorMessage(cause)
      }
    } finally {
      if (!request.signal.aborted) loading.value = false
    }
  }

  watch(() => auth.isAuthenticated, (authenticated) => {
    if (authenticated) load()
    else clear()
  }, { flush: 'sync' })
  onMounted(() => { if (auth.isAuthenticated) load() })
  onUnmounted(() => { disposed = true; clear() })

  return { links, loading, error, load }
}
