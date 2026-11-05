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
  assert.match(html, /Meus links/)
  assert.doesNotMatch(html, /id="link-stats"/)
  assert.doesNotMatch(html, /Total de cliques/)
  assert.doesNotMatch(html, /id="history-drawer"/)
})

test('home autenticada abre a lista e o encurtador compacto', async () => {
  const html = await renderPage('/', true)
  assert.match(html, /Seus links, à mão/)
  assert.match(html, /Encurtar um novo link/)
  assert.match(html, /Cliques acumulados/)
  assert.doesNotMatch(html, /id="history-drawer"/)
  assert.doesNotMatch(html, /class="link-capsule/)
})

test('visitante encontra login e cadastro ao acessar as métricas', async () => {
  const html = await renderPage('/stats?code=aB3xYz')
  assert.match(html, /id="auth-title"/)
  assert.match(html, /Cadastre-se/)
  assert.doesNotMatch(html, /Total de cliques/)
  assert.doesNotMatch(html, /id="history-drawer"/)
})

test('endereço antigo de métricas abre a lista de links', async () => {
  const html = await renderPage('/stats', true)
  assert.match(html, /Meus links/)
  assert.match(html, /Mais recentes primeiro/)
  assert.match(html, /Buscando seus links/)
  assert.doesNotMatch(html, /Nenhum link selecionado/)
  assert.doesNotMatch(html, /id="history-drawer"/)
})

test('código inválido recebe tratamento na página de links', async () => {
  const invalid = await renderPage('/stats?code=a%2Fb', true)
  assert.match(invalid, /O código informado é inválido/)
})

test('a lista apresenta os dados do servidor e expande detalhes no mesmo link', async () => {
  const LinkList = (await vite.ssrLoadModule('/src/components/LinkList.vue')).default
  const link = { code: 'abc123', shortUrl: 'https://short.example/api/abc123', originalUrl: 'https://example.com/article', createdAt: '2026-10-19T12:00:00', clickCount: 42 }
  const html = await renderToString(createSSRApp(LinkList, { links: [link], selectedCode: link.code, createdCode: link.code }))
  assert.match(html, /https:\/\/short.example\/api\/abc123/)
  assert.match(html, /https:\/\/example.com\/article/)
  assert.match(html, /Novo/)
  assert.match(html, /aria-expanded="true"/)
  assert.match(html, /id="details-abc123"/)
  assert.match(html, /Total de cliques/)
  assert.match(html, />42</)
  const collapsed = await renderToString(createSSRApp(LinkList, { links: [link] }))
  assert.match(collapsed, /aria-expanded="false"/)
  assert.doesNotMatch(collapsed, /id="details-abc123"/)
  assert.doesNotMatch(collapsed, /https:\/\/example.com\/article/)
  assert.doesNotMatch(collapsed, /Criado em|Total de cliques|>42</)
  const details = html.slice(html.indexOf('<section id="details-abc123"'))
  assert.doesNotMatch(details, /https:\/\/short.example\/api\/abc123/)
})
