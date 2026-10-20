import assert from 'node:assert/strict'
import test from 'node:test'
import { createMemoryHistory, createRouter } from 'vue-router'
import { routes, scrollBehavior } from '../src/router/routes.js'

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    // Exercise navigation without requiring a DOM to render Vue components.
    routes: routes.map((route) => route.name === 'home' ? { ...route, component: { render: () => null } } : route),
  })
}

test('endereços antigos de estatísticas abrem a home com o mesmo código', async () => {
  const router = createTestRouter()
  await router.push('/stats/aB3xYz')
  assert.equal(router.currentRoute.value.name, 'home')
  assert.equal(router.currentRoute.value.path, '/')
  assert.equal(router.currentRoute.value.query.code, 'aB3xYz')
  assert.equal(router.currentRoute.value.hash, '#link-stats')

  await router.push('/stats')
  assert.equal(router.currentRoute.value.name, 'home')
  assert.equal(router.currentRoute.value.query.code, undefined)
})

test('selecionar outro link mantém a mesma tela e não cria passos extras no histórico', async () => {
  const router = createTestRouter()
  await router.push('/')
  const workspace = router.currentRoute.value.matched[0]
  await router.replace({ name: 'home', query: { code: 'first' } })
  await router.replace({ name: 'home', query: { code: 'second' } })
  assert.equal(router.currentRoute.value.matched[0], workspace)
  assert.equal(router.currentRoute.value.query.code, 'second')
  assert.equal(router.options.history.state.back, undefined)
})

test('trocar o código preserva a rolagem e endereços antigos apontam para o painel', () => {
  assert.equal(scrollBehavior({ path: '/', hash: '' }, { path: '/' }), false)
  assert.deepEqual(scrollBehavior({ path: '/', hash: '#link-stats' }, { path: '/' }), { el: '#link-stats' })
  const position = { left: 0, top: 240 }
  assert.equal(scrollBehavior({ path: '/' }, { path: '/' }, position), position)
})
