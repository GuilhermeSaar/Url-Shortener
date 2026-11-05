import assert from 'node:assert/strict'
import { test } from 'node:test'
import axios from 'axios'
import { createPinia } from 'pinia'
import { createRenderer, nextTick } from 'vue'

let respond
axios.defaults.adapter = (config) => respond(config)
const { useAuthStore } = await import('../src/stores/auth.js')
const { useUrlHistory } = await import('../src/composables/useUrlHistory.js')
const { shortenUrl } = await import('../src/services/api.js')

const renderer = createRenderer({
  createComment: (text) => ({ text }),
  createText: (text) => ({ text }),
  createElement: (type) => ({ type, children: [] }),
  insert: (child, parent) => { (parent.children ||= []).push(child) },
  remove: () => {},
  setText: (node, text) => { node.text = text },
  setElementText: (node, text) => { node.text = text },
  parentNode: () => null,
  nextSibling: () => null,
  patchProp: () => {},
})

const flush = async () => { await new Promise((resolve) => setImmediate(resolve)); await nextTick() }
const item = (code) => ({ code, date: '2026-10-19T12:00:00', clickCount: 0, urlOriginal: `https://example.com/${code}` })
const response = (config, data) => ({ config, data, status: 200, statusText: 'OK', headers: {} })

test('histórico segue a sessão, atualiza após criação e descarta respostas antigas', async () => {
  const oldWindow = globalThis.window
  globalThis.window = { location: { origin: 'https://short.example' } }
  let account = null
  let historyRequests = 0
  let delayA = false
  let failHistory = false
  let completeA
  const serverLinks = { a: [item('accountA')], b: [item('accountB')] }

  respond = async (config) => {
    if (config.url === '/login') {
      account = JSON.parse(config.data).email
      return response(config)
    }
    if (config.url === '/logout') {
      account = null
      return response(config)
    }
    if (config.url === '/api/shorten') {
      if (account) serverLinks[account].push({ ...item('created'), date: '2026-11-05T12:00:00' })
      return response(config, { response: 'https://short.example/api/created' })
    }
    if (config.url === '/api/urls') {
      historyRequests++
      if (failHistory) throw new axios.AxiosError('Unavailable', 'ERR_BAD_RESPONSE', config, null, { status: 503, data: {} })
      const snapshot = [...serverLinks[account]]
      if (account === 'a' && delayA) {
        return new Promise((resolve) => { completeA = () => resolve(response(config, snapshot)) })
      }
      return response(config, snapshot)
    }
    throw new Error(`Requisição inesperada: ${config.url}`)
  }

  const pinia = createPinia()
  let history
  const app = renderer.createApp({ setup() { history = useUrlHistory(); return () => null } })
  app.use(pinia)
  app.mount({ children: [] })
  const auth = useAuthStore(pinia)
  try {
    await flush()
    assert.equal(historyRequests, 0)
    await shortenUrl('https://example.com/anonymous')
    assert.equal(historyRequests, 0)
    assert.deepEqual(history.links.value, [])

    await auth.signIn('a', 'password')
    await flush()
    assert.deepEqual(history.links.value.map((link) => link.code), ['accountA'])
    await shortenUrl('https://example.com/created')
    await history.load()
    assert.deepEqual(history.links.value.map((link) => link.code), ['created', 'accountA'])
    assert.equal(history.hasLoaded.value, true)

    failHistory = true
    await history.load()
    assert.deepEqual(history.links.value.map((link) => link.code), ['created', 'accountA'])
    assert.match(history.error.value, /Não foi possível/)
    failHistory = false

    delayA = true
    const oldRequest = history.load()
    await flush()
    assert.ok(completeA)
    await auth.signOut()
    assert.deepEqual(history.links.value, [])
    assert.equal(history.hasLoaded.value, false)
    await auth.signIn('b', 'password')
    await flush()
    assert.deepEqual(history.links.value.map((link) => link.code), ['accountB'])
    completeA()
    await oldRequest
    assert.deepEqual(history.links.value.map((link) => link.code), ['accountB'])
  } finally {
    app.unmount()
    globalThis.window = oldWindow
  }
})
