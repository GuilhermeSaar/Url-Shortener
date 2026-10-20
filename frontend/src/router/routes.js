export const routes = [
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
  {
    path: '/stats/:code?',
    redirect: (to) => ({ name: 'home', query: { ...to.query, ...(to.params.code ? { code: to.params.code } : {}) }, hash: '#link-stats' }),
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
