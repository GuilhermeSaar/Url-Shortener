import assert from 'node:assert/strict'
import { createServer as createHttpServer } from 'node:http'
import { after, before, test } from 'node:test'
import { fileURLToPath } from 'node:url'
import { createSSRApp } from 'vue'
import { renderToString } from '@vue/server-renderer'
import { createPinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { createServer } from 'vite'

let vite
let App
let routes
let authenticationGuard
let clearAuthToken
let setAuthToken

before(async () => {
  // Compile the real Vue components without starting an HTTP/WebSocket listener.
  const httpServer = createHttpServer()
  vite = await createServer({
    root: fileURLToPath(new URL('../', import.meta.url)),
    server: { middlewareMode: true, hmr: { server: httpServer }, watch: null },
    optimizeDeps: { noDiscovery: true, include: [] },
  })
  App = (await vite.ssrLoadModule('/src/App.vue')).default
  routes = (await vite.ssrLoadModule('/src/router/routes.js')).routes
  authenticationGuard = (await vite.ssrLoadModule('/src/router/guards.js')).authenticationGuard
  const api = await vite.ssrLoadModule('/src/services/api.js')
  clearAuthToken = api.clearAuthToken
  setAuthToken = api.setAuthToken
})

after(async () => { await vite?.close() })

async function renderPage(url, authenticated = false) {
  clearAuthToken()
  if (authenticated) setAuthToken('header.payload.signature')
  const pinia = createPinia()
  const router = createRouter({ history: createMemoryHistory(), routes })
  router.beforeEach((to) => authenticationGuard(to, pinia))
  const app = createSSRApp(App).use(pinia).use(router)
  await router.push(url)
  return renderToString(app)
}

test('home oferece o encurtador sem renderizar métricas ou histórico', async () => {
  const html = await renderPage('/')
  assert.match(html, /Encurtar link/)
  assert.match(html, /Métricas/)
  assert.doesNotMatch(html, /id="link-stats"/)
  assert.doesNotMatch(html, /Total de cliques/)
  assert.doesNotMatch(html, /id="history-drawer"/)
})

test('visitante encontra login e cadastro ao acessar as métricas', async () => {
  const html = await renderPage('/stats?code=aB3xYz')
  assert.match(html, /id="auth-title"/)
  assert.match(html, /Cadastre-se/)
  assert.doesNotMatch(html, /Total de cliques/)
  assert.doesNotMatch(html, /id="history-drawer"/)
})

test('a tela autenticada contém métricas e histórico, sem o formulário de criação', async () => {
  const html = await renderPage('/stats', true)
  assert.match(html, /Cada clique conta/)
  assert.match(html, /Nenhum link selecionado/)
  assert.match(html, /Total de cliques/)
  assert.match(html, /id="history-drawer"/)
  assert.doesNotMatch(html, /class="link-capsule/)
})

test('código vazio ou inválido recebe tratamento na tela de métricas', async () => {
  const empty = await renderPage('/stats?code=', true)
  assert.match(empty, /Nenhum link selecionado/)
  const invalid = await renderPage('/stats?code=a%2Fb', true)
  assert.match(invalid, /Informe um código válido/)
  assert.match(invalid, /Nenhum link selecionado/)
})
