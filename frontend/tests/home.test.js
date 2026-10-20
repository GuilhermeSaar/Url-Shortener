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
let HomeView
let App

before(async () => {
  // Compile the real Vue components without starting an HTTP/WebSocket listener.
  const httpServer = createHttpServer()
  vite = await createServer({
    root: fileURLToPath(new URL('../', import.meta.url)),
    server: { middlewareMode: true, hmr: { server: httpServer }, watch: null },
    optimizeDeps: { noDiscovery: true, include: [] },
  })
  HomeView = (await vite.ssrLoadModule('/src/views/HomeView.vue')).default
  App = (await vite.ssrLoadModule('/src/App.vue')).default
})

after(async () => { await vite?.close() })

async function renderHome(url) {
  const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/', component: HomeView }] })
  await router.push(url)
  const app = createSSRApp(App).use(createPinia()).use(router)
  return renderToString(app)
}

test('home sem código renderiza a cápsula e as estatísticas vazias', async () => {
  const html = await renderHome('/')
  assert.match(html, /Encurtar link/)
  assert.match(html, /Cada clique conta/)
  assert.match(html, /Nenhum link selecionado/)
  assert.match(html, /Total de cliques/)
  assert.match(html, /Histórico/)
})

test('código vazio ou inválido não impede a renderização da home', async () => {
  const empty = await renderHome('/?code=')
  assert.match(empty, /Nenhum link selecionado/)
  const invalid = await renderHome('/?code=a%2Fb')
  assert.match(invalid, /Encurtar link/)
  assert.match(invalid, /Informe um código válido/)
  assert.match(invalid, /Nenhum link selecionado/)
})
