export const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue'),
    beforeEnter: (to) => ('code' in to.query ? { name: 'links', query: to.query, replace: true } : true),
  },
  { path: '/entrar', name: 'login', component: () => import('../views/AuthView.vue') },
  { path: '/meus-links', name: 'links', component: () => import('../views/LinksView.vue'), meta: { requiresAuth: true } },
  { path: '/stats', name: 'stats', redirect: (to) => ({ name: 'links', query: to.query }) },
  {
    path: '/stats/:code',
    redirect: (to) => ({ name: 'links', params: {}, query: { ...to.query, code: to.params.code } }),
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
