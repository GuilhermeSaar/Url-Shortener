import assert from 'node:assert/strict'
import { createServer as createHttpServer } from 'node:http'
import { after, before, test } from 'node:test'
import { fileURLToPath } from 'node:url'
import axios from 'axios'
import { createSSRApp } from 'vue'
import { renderToString } from '@vue/server-renderer'
import { createPinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { createServer } from 'vite'

let vite
let App
let routes
let authenticationGuard
let browserHasSession = false

axios.defaults.adapter = (config) => {
  if (config.url === '/api/auth/session') {
    if (browserHasSession) {
      return Promise.resolve({ config, data: undefined, status: 204, statusText: 'No Content', headers: {} })
    }
    return Promise.reject(new axios.AxiosError('Unauthorized', 'ERR_BAD_REQUEST', config, null, { status: 401, data: {} }))
  }
  throw new Error(`Requisição inesperada: ${config.url}`)
}

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
})

after(async () => { await vite?.close() })

async function renderPage(url, authenticated = false) {
  browserHasSession = authenticated
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
