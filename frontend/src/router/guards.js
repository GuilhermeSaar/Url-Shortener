import { useAuthStore } from '../stores/auth.js'

export function authenticationGuard(to, pinia) {
  const auth = useAuthStore(pinia)
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: to.query, replace: true }
  }
  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'stats', query: to.query, replace: true }
  }
}
