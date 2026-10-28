import { createRouter, createWebHistory } from 'vue-router'
import { routes, scrollBehavior } from './routes'
import { authenticationGuard } from './guards'

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior,
})

router.beforeEach((to) => authenticationGuard(to))

export default router
