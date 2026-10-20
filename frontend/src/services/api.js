import axios from 'axios'
import { extractCode, isHttpUrl } from '../utils/links.js'

class InvalidApiResponse extends Error {}

const api = axios.create({
  baseURL: import.meta.env?.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: { Accept: 'application/json' },
})

export async function shortenUrl(url) {
  const { data } = await api.post('/shorten', { url })
  if (!isHttpUrl(data?.response)) throw new InvalidApiResponse()
  try {
    extractCode(data.response)
  } catch {
    throw new InvalidApiResponse()
  }
  return data
}

export async function getUrlStats(code, signal) {
  const { data } = await api.get(`/api/url/${encodeURIComponent(code)}`, { signal })
  if (!data || !isHttpUrl(data.urlOriginal) || !Number.isSafeInteger(data.clickCount)
    || data.clickCount < 0 || typeof data.date !== 'string' || Number.isNaN(Date.parse(data.date))) {
    throw new InvalidApiResponse()
  }
  return data
}

export function errorMessage(error, field) {
  if (error instanceof InvalidApiResponse) return 'O serviço retornou uma resposta inesperada. Tente novamente em instantes.'
  const fields = error?.response?.data?.errors
  if (error?.response?.status === 400 && Array.isArray(fields)) {
    const messages = fields.filter((item) => item && (!field || item.field === field))
      .map((item) => item.message).filter((message) => typeof message === 'string' && message.trim())
    if (messages.length) return messages.join(' ')
  }
  if (error?.response?.status === 400) return 'Os dados enviados são inválidos. Confira o endereço e tente novamente.'
  if (error?.response?.status === 404) return 'Link não encontrado. Ele pode não existir ou já ter expirado.'
  if (error?.code === 'ECONNABORTED' || error?.code === 'ETIMEDOUT') return 'O servidor demorou a responder. Tente novamente.'
  if (!error?.response || error.response.status >= 500) return 'Não foi possível conectar ao serviço. Tente novamente em instantes.'
  return 'Não foi possível concluir a solicitação. Confira os dados e tente novamente.'
}
