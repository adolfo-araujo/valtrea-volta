// Cliente da API do Valtrea Volta. O token de acesso dura 15 minutos e é renovado sozinho com o token
// de sessão (cada token de sessão vale uma vez).
const BASE = `${(import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')}/api/v1`

const KEY = 'valtrea-volta:sessao'

export const sessao = {
  ler() {
    try {
      return JSON.parse(localStorage.getItem(KEY)) || null
    } catch {
      return null
    }
  },
  salvar(tokens) {
    try {
      localStorage.setItem(KEY, JSON.stringify({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken }))
    } catch {
      // navegação privada sem armazenamento: a sessão dura até fechar a aba
    }
    memoria = tokens
  },
  limpar() {
    try {
      localStorage.removeItem(KEY)
    } catch {
      // nada a fazer
    }
    memoria = null
  },
}

// Pedidos que passaram de 4 s: mostram o aviso de "servidor acordando" (veja AvisoServidorLento).
let lentos = 0
const marcarLento = (delta) => {
  lentos = Math.max(0, lentos + delta)
  window.dispatchEvent(new CustomEvent('valtrea:lento', { detail: lentos }))
}

// Acorda a API (plano grátis do Render) assim que o site abre, enquanto a pessoa ainda lê ou digita.
if (import.meta.env.PROD) {
  fetch(`${BASE.replace(/\/api\/v1$/, '')}/actuator/health`, { mode: 'no-cors', cache: 'no-store' }).catch(() => {})
}

let memoria = sessao.ler()
let renovando = null

export class ErroApi extends Error {
  constructor(status, mensagem, campos) {
    super(mensagem)
    this.status = status
    this.campos = campos
  }
}

async function renovar() {
  const refreshToken = memoria?.refreshToken
  if (!refreshToken) throw new ErroApi(401, 'Sua sessão expirou. Entre novamente.')
  const res = await fetch(`${BASE}/auth/renovar`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
  if (!res.ok) {
    sessao.limpar()
    window.dispatchEvent(new Event('volta:saiu'))
    throw new ErroApi(401, 'Sua sessão expirou. Entre novamente.')
  }
  sessao.salvar(await res.json())
}

async function mensagemDe(res) {
  const corpo = await res.json().catch(() => ({}))
  const primeiroCampo = corpo.campos && Object.entries(corpo.campos)[0]
  const texto = primeiroCampo ? `${primeiroCampo[1]}` : corpo.detail || corpo.message
  if (res.status === 429) return new ErroApi(429, texto || 'Muitas tentativas. Aguarde alguns minutos.')
  return new ErroApi(res.status, texto || 'Não foi possível concluir agora. Tente de novo em instantes.', corpo.campos)
}

export async function chamar(metodo, rota, corpo, { autenticar = true, tentativa = 0 } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (autenticar && memoria?.accessToken) headers.Authorization = `Bearer ${memoria.accessToken}`
  let res
  let avisou = false
  const relogio = setTimeout(() => {
    avisou = true
    marcarLento(1)
  }, 4000)
  try {
    res = await fetch(`${BASE}${rota}`, { method: metodo, headers, body: corpo ? JSON.stringify(corpo) : undefined })
  } catch {
    throw new ErroApi(0, 'Sem conexão com o servidor. Confira a internet e tente de novo. Na primeira vez do dia, ele pode levar até 1 minuto para acordar.')
  } finally {
    clearTimeout(relogio)
    if (avisou) marcarLento(-1)
  }
  if (res.status === 401 && autenticar && tentativa === 0 && memoria?.refreshToken) {
    renovando ??= renovar().finally(() => (renovando = null))
    await renovando
    return chamar(metodo, rota, corpo, { autenticar, tentativa: 1 })
  }
  if (!res.ok) throw await mensagemDe(res)
  if (res.status === 204 || res.status === 202) return null
  const tipo = res.headers.get('Content-Type') || ''
  return tipo.includes('json') ? res.json() : res.text()
}

export const estaLogado = () => Boolean(memoria?.refreshToken)
export const tokenDeSessao = () => memoria?.refreshToken

export const CATEGORIAS = {
  MOCHILA_BOLSA: 'Mochila ou bolsa',
  CARTEIRA_DOCUMENTOS: 'Carteira ou documentos',
  CHAVES: 'Chaves',
  ELETRONICO: 'Eletrônico',
  OCULOS: 'Óculos',
  ROUPA: 'Roupa',
  BRINQUEDO: 'Brinquedo',
  BICICLETA: 'Bicicleta',
  MALA: 'Mala',
  PET: 'Pet (coleira)',
  OUTRO: 'Outro',
}
