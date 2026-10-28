export const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue'),
    beforeEnter: (to) => ('code' in to.query ? { name: 'stats', query: to.query, replace: true } : true),
  },
  { path: '/entrar', name: 'login', component: () => import('../views/AuthView.vue') },
  { path: '/stats', name: 'stats', component: () => import('../views/StatsView.vue'), meta: { requiresAuth: true } },
  {
    path: '/stats/:code',
    redirect: (to) => ({ name: 'stats', params: {}, query: { ...to.query, code: to.params.code } }),
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

// Updating the selected link must not scroll the workspace back to the top.
export function scrollBehavior(to, from, savedPosition) {
  if (savedPosition) return savedPosition
  if (to.hash) return { el: to.hash }
  if (to.path !== from.path) return { top: 0 }
  return false
}
