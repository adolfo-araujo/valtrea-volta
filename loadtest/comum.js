// Funções usadas pelo preparo (seed.js) e pelo teste (carga.js).
import http from 'k6/http'
import { sleep } from 'k6'

export const API = __ENV.API_URL || 'http://localhost:8080'
export const emailDe = (n) => `carga-${n}@test.dev`
export const SENHA = 'senha-de-carga-123'

// Cada pessoa virtual tem um IP simulado, como na vida real: os limites de pedidos da API são por IP
// (em produção esse cabeçalho vem da Cloudflare).
export const ipDe = (n) => `10.${(n >> 16) & 255}.${(n >> 8) & 255}.${n & 255}`

// Com 0,1 CPU (plano grátis do Render), a API leva alguns minutos para subir.
export function esperarApi() {
    for (let i = 0; i < 180; i++) {
        const r = http.get(`${API}/actuator/health`, { timeout: '10s', responseCallback: http.expectedStatuses(200, 503) })
        if (r.status === 200) return
        sleep(5)
    }
    throw new Error('A API não subiu em 15 minutos. Veja o log com: docker compose logs api')
}

export function entrar(n, ip) {
    const res = http.post(`${API}/api/v1/auth/login`, JSON.stringify({ email: emailDe(n), senha: SENHA }), {
        headers: { 'Content-Type': 'application/json', 'CF-Connecting-IP': ip },
        tags: { tipo: 'login', rota: 'POST /auth/login' },
        timeout: '60s',
    })
    return res.status === 200 ? res.json('accessToken') : null
}

export const espera = (min, max) => sleep(min + Math.random() * (max - min))

// Resumo em português, salvo em loadtest/resultado/resumo.md.
export function resumo(data, nome) {
    const m = data.metrics
    const ms = (v) => (v === undefined ? '—' : `${Math.round(v)} ms`)
    const trend = (k) => (m[k] ? m[k].values : {})
    const linhas = [
        ['Leituras (abrir telas)', trend('http_req_duration{tipo:leitura}')],
        ['Escritas (salvar, apagar)', trend('http_req_duration{tipo:escrita}')],
        ['Login', trend('http_req_duration{tipo:login}')],
    ]
    const falhas = m.http_req_failed ? m.http_req_failed.values.rate : 0
    const pedidos = m.http_reqs ? m.http_reqs.values.count : 0
    const porSeg = m.http_reqs ? m.http_reqs.values.rate : 0
    const passou = Object.values(m).every((x) => !x.thresholds || Object.values(x.thresholds).every((t) => t.ok))
    let md = `# Teste de carga: ${nome}\n\n`
    md += `**${passou ? '✅ Passou' : '❌ Não passou'}** nos critérios: 95% das respostas em menos de 1 s (login: 2 s) e menos de 1% de erros.\n\n`
    md += `| | Mediana | 95% em menos de | Pior |\n|---|---|---|---|\n`
    for (const [rot, v] of linhas) md += `| ${rot} | ${ms(v.med)} | ${ms(v['p(95)'])} | ${ms(v.max)} |\n`
    md += `\n| Pedidos | ${pedidos} (${porSeg.toFixed(1)} por segundo) |\n|---|---|\n| Erros | ${(falhas * 100).toFixed(2)}% |\n`
    md += `| Pessoas ao mesmo tempo | ${__ENV.USERS || 100} |\n| CPU da API | ${__ENV.CPUS || '0.1'} (0.1 = plano grátis do Render; 0.5 = Starter) |\n`
    return { '/resultado/resumo.md': md, stdout: '\n' + md + '\n' }
}
