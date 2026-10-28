import assert from 'node:assert/strict'
import { beforeEach, test } from 'node:test'
import axios from 'axios'
import { createPinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'

let browserHasSession = false

function response(config, data = undefined) {
  return Promise.resolve({ config, data, status: 204, statusText: 'No Content', headers: {} })
}

axios.defaults.adapter = (config) => {
  if (config.url === '/api/auth/session') {
    if (browserHasSession) return response(config)
    return Promise.reject(new axios.AxiosError('Unauthorized', 'ERR_BAD_REQUEST', config, null, { status: 401, data: {} }))
  }
  if (config.url === '/logout') {
    browserHasSession = false
    return response(config)
  }
  throw new Error(`Requisição inesperada: ${config.url}`)
}

const { routes, scrollBehavior } = await import('../src/router/routes.js')
const { authenticationGuard } = await import('../src/router/guards.js')
const { useAuthStore } = await import('../src/stores/auth.js')

beforeEach(() => { browserHasSession = false })

function createTestRouter(authenticated = false) {
  browserHasSession = authenticated
  const pinia = createPinia()
  const router = createRouter({
    history: createMemoryHistory(),
    // Exercise navigation without requiring a DOM to render Vue components.
    routes: routes.map((route) => route.component ? { ...route, component: { render: () => null } } : route),
  })
  router.beforeEach((to) => authenticationGuard(to, pinia))
  return { router, auth: useAuthStore(pinia) }
}

test('a criação é pública e métricas exigem login preservando o código escolhido', async () => {
  const { router } = createTestRouter()
  await router.push('/')
  assert.equal(router.currentRoute.value.name, 'home')
  await router.push('/stats?code=aB3xYz')
  assert.equal(router.currentRoute.value.name, 'login')
  assert.equal(router.currentRoute.value.path, '/entrar')
  assert.equal(router.currentRoute.value.query.code, 'aB3xYz')
})

test('endereços antigos chegam às métricas e também respeitam a autenticação', async () => {
  for (const path of ['/stats/aB3xYz', '/?code=aB3xYz']) {
    const { router } = createTestRouter()
    await router.push(path)
    assert.equal(router.currentRoute.value.name, 'login')
    assert.equal(router.currentRoute.value.query.code, 'aB3xYz')
  }
  const { router } = createTestRouter(true)
  await router.push('/stats/aB3xYz')
  assert.equal(router.currentRoute.value.name, 'stats')
  assert.equal(router.currentRoute.value.query.code, 'aB3xYz')
})

test('usuário autenticado acessa as métricas e muda o código na mesma tela', async () => {
  const { router } = createTestRouter(true)
  await router.push('/stats')
  const workspace = router.currentRoute.value.matched[0]
  await router.replace({ name: 'stats', query: { code: 'first' } })
  await router.replace({ name: 'stats', query: { code: 'second' } })
  assert.equal(router.currentRoute.value.name, 'stats')
  assert.equal(router.currentRoute.value.matched[0], workspace)
  assert.equal(router.currentRoute.value.query.code, 'second')
  assert.equal(router.options.history.state.back, undefined)
})

test('sair bloqueia novo acesso às métricas, inclusive pelo histórico de navegação', async () => {
  const { router, auth } = createTestRouter(true)
  await router.push('/stats?code=aB3xYz')
  await auth.signOut()
  await router.push('/')
  router.back()
  await new Promise((resolve) => router.afterEach(resolve))
  assert.equal(router.currentRoute.value.name, 'login')
  assert.equal(router.currentRoute.value.query.code, 'aB3xYz')
})

test('usuário autenticado pula o login preservando a seleção', async () => {
  const { router } = createTestRouter(true)
  await router.push('/entrar?code=aB3xYz')
  assert.equal(router.currentRoute.value.name, 'stats')
  assert.equal(router.currentRoute.value.query.code, 'aB3xYz')
})

test('trocar o código mantém a rolagem e trocar a tela volta ao topo', () => {
  assert.equal(scrollBehavior({ path: '/stats', hash: '' }, { path: '/stats' }), false)
  assert.deepEqual(scrollBehavior({ path: '/stats', hash: '' }, { path: '/' }), { top: 0 })
  const position = { left: 0, top: 240 }
  assert.equal(scrollBehavior({ path: '/stats' }, { path: '/stats' }, position), position)
})
