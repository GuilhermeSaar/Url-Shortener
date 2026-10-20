export function isHttpUrl(value) {
  if (typeof value !== 'string' || !/^https?:\/\/[^/?#\s]+(?:[/?#]|$)/i.test(value)) return false
  try {
    const url = new URL(value)
    return /^https?:$/.test(url.protocol) && Boolean(url.hostname) && !/\s/.test(value)
  } catch {
    return false
  }
}

export function extractCode(value) {
  const input = value.trim()
  const code = isHttpUrl(input) ? new URL(input).pathname.replace(/^\/+|\/+$/g, '') : input
  if (!/^[a-zA-Z0-9]+$/.test(code)) throw new Error('Informe um código válido ou uma URL curta completa.')
  return code
}

export function formatDate(value) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'Data indisponível'
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium', timeStyle: 'short' }).format(date)
}
