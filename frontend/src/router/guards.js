import { useAuthStore } from '../stores/auth.js'

export function authenticationGuard(to, pinia) {
  const auth = useAuthStore(pinia)
  return auth.restoreSession().then(() => {
    if (to.meta.requiresAuth && !auth.isAuthenticated) {
      return { name: 'login', query: to.query, replace: true }
    }
    if ((to.name === 'login' || to.name === 'home') && auth.isAuthenticated) {
      return { name: 'links', query: to.query, replace: true }
    }
  })
}
