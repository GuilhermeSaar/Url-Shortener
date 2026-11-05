import { onMounted, onUnmounted, ref, watch } from 'vue'
import { errorMessage, getUrlHistory } from '../services/api.js'
import { useAuthStore } from '../stores/auth.js'

export function useUrlHistory({ onSessionExpired } = {}) {
  const auth = useAuthStore()
  const links = ref([])
  const loading = ref(false)
  const error = ref('')
  const hasLoaded = ref(false)
  let controller
  let disposed = false

  function clear() {
    controller?.abort()
    links.value = []
    loading.value = false
    error.value = ''
    hasLoaded.value = false
  }

  async function load() {
    controller?.abort()
    if (!auth.isAuthenticated || disposed) { clear(); return }
    const request = new AbortController()
    controller = request
    loading.value = true
    error.value = ''
    try {
      const result = await getUrlHistory(request.signal)
      if (!request.signal.aborted && auth.isAuthenticated) {
        links.value = [...result].sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt))
        hasLoaded.value = true
      }
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

  return { links, loading, error, hasLoaded, load }
}
