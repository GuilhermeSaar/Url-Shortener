import assert from 'node:assert/strict'
import test from 'node:test'
import axios from 'axios'

// Exercise the real Axios request/response pipeline without a running backend.
let respond
axios.defaults.adapter = (config) => respond(config)
const { clearAuthToken, getUrlStats, login, setAuthToken, shortenUrl, errorMessage } = await import('../src/services/api.js')

function response(config, data) {
  return Promise.resolve({ config, data, status: 200, statusText: 'OK', headers: {} })
}

test('criação envia JSON para /shorten e preserva o domínio do link retornado', async () => {
  respond = (config) => {
    assert.equal(config.url, '/shorten')
    assert.equal(config.method, 'post')
    assert.match(config.headers.getContentType(), /application\/json/)
    assert.deepEqual(JSON.parse(config.data), { url: 'https://example.com/article' })
    return response(config, { response: 'http://localhost:8080/aB3xYz' })
  }
  assert.deepEqual(await shortenUrl('https://example.com/article'), { response: 'http://localhost:8080/aB3xYz' })
})

test('400 apresenta apenas as mensagens do campo url e aceita erros sem o formato padrão', async () => {
  respond = (config) => Promise.reject(new axios.AxiosError('Bad request', 'ERR_BAD_REQUEST', config, null, {
    status: 400,
    data: { errors: [null, { field: 'other', message: 'Outra mensagem' }, { field: 'url', message: 'URL inválida' }] },
  }))
  await assert.rejects(shortenUrl('https://example.com'), (error) => {
    assert.equal(errorMessage(error, 'url'), 'URL inválida')
    return true
  })
  assert.match(errorMessage({ response: { status: 400, data: '<html>erro</html>' } }, 'url'), /inválidos/)
})

test('consulta usa o endpoint correto sem consumir o redirecionamento', async () => {
  const stats = { clickCount: 7, date: '2026-10-19T12:00:00', urlOriginal: 'https://example.com/article' }
  respond = (config) => {
    assert.equal(config.url, '/api/url/aB3xYz')
    assert.equal(config.method, 'get')
    return response(config, stats)
  }
  assert.deepEqual(await getUrlStats('aB3xYz'), stats)
})

test('consulta protegida envia o JWT em memória como Bearer e não o envia ao login', async () => {
  const token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkZXZAZXhhbXBsZS5jb20ifQ.signature'
  setAuthToken(token)
  try {
    respond = (config) => {
      assert.equal(config.url, '/api/url/aB3xYz')
      assert.equal(config.headers.get('Authorization'), `Bearer ${token}`)
      return response(config, { clickCount: 7, date: '2026-10-19T12:00:00', urlOriginal: 'https://example.com/article' })
    }
    await getUrlStats('aB3xYz')

    respond = (config) => {
      assert.equal(config.url, '/login')
      assert.equal(config.headers.get('Authorization'), undefined)
      assert.deepEqual(JSON.parse(config.data), { email: 'dev@example.com', password: 'password123' })
      return response(config, { token })
    }
    assert.equal(await login('dev@example.com', 'password123'), token)
  } finally {
    clearAuthToken()
  }
})

test('404 preserva o status para o estado visual de link não encontrado', async () => {
  respond = (config) => Promise.reject(new axios.AxiosError('Not found', 'ERR_BAD_REQUEST', config, null, { status: 404, data: {} }))
  await assert.rejects(getUrlStats('unknown'), (error) => {
    assert.equal(error.response.status, 404)
    assert.match(errorMessage(error), /não encontrado/)
    return true
  })
})

test('401 orienta a renovar a autenticação', () => {
  assert.match(errorMessage({ response: { status: 401, data: {} } }), /sessão.*Entre novamente/i)
})

test('respostas incompletas ou inseguras não viram links nem métricas', async () => {
  for (const data of [null, {}, { response: 'javascript:alert(1)' }, { response: 'https://example.com/a/b' }]) {
    respond = (config) => response(config, data)
    await assert.rejects(shortenUrl('https://example.com'), (error) => {
      assert.match(errorMessage(error), /resposta inesperada/)
      return true
    })
  }
  for (const data of [{}, { date: 'invalid', clickCount: 1, urlOriginal: 'https://example.com' }, { date: '2026-10-19', clickCount: -1, urlOriginal: 'https://example.com' }]) {
    respond = (config) => response(config, data)
    await assert.rejects(getUrlStats('aB3xYz'))
  }
})

test('requisições canceladas e falhas de conexão têm tratamento separado', async () => {
  const controller = new AbortController()
  controller.abort()
  await assert.rejects(getUrlStats('aB3xYz', controller.signal), (error) => axios.isCancel(error))
  assert.match(errorMessage({ code: 'ECONNABORTED' }), /demorou/)
  assert.match(errorMessage({ code: 'ERR_NETWORK' }), /conectar/)
  assert.match(errorMessage({ response: { status: 502 } }), /conectar/)
})
