// Teste de carga do Valtrea Volta (k6: https://k6.io). Rode pelo rodar-teste-de-carga.bat.
import http from 'k6/http'
import { check } from 'k6'
import { API, entrar, espera, ipDe, resumo } from './comum.js'

const USERS = Number(__ENV.USERS || 100)
export const options = {
    setupTimeout: '10m',
    scenarios: {
        uso: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: __ENV.RAMP || '1m', target: USERS },
                { duration: __ENV.HOLD || '3m', target: USERS },
                { duration: '20s', target: 0 },
            ],
            gracefulRampDown: '15s',
        },
    },
    // Mesmo critério do Valtrea Finance e do Pay.
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{tipo:leitura}': ['p(95)<1000'],
        'http_req_duration{tipo:escrita}': ['p(95)<1000'],
        'http_req_duration{tipo:login}': ['p(95)<2000'],
        checks: ['rate>0.99'],
    },
    summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
}

let token = null
const conta = () => (__VU - 1) % USERS
const ip = () => ipDe(__VU + 1000)
const p = (tipo, rota, extra = {}) => ({
    headers: { 'Content-Type': 'application/json', 'CF-Connecting-IP': ip(), Authorization: `Bearer ${token}` },
    tags: { tipo, rota },
    timeout: '60s',
    ...extra,
})
const ok = (r) => r.status >= 200 && r.status < 300

// Cada pessoa: entra, abre a lista de objetos, "lê" a tela por 5 a 15 s e às vezes cadastra e apaga um objeto
// ou abre os avisos recebidos.
export default function () {
    if (!token) {
        token = entrar(conta(), ip())
        if (!check(token, { 'login ok': (t) => !!t })) return espera(5, 10)
    }
    const inicio = http.batch([
        ['GET', `${API}/api/v1/eu`, null, p('leitura', 'GET /eu')],
        ['GET', `${API}/api/v1/objetos`, null, p('leitura', 'GET /objetos')],
    ])
    check(inicio, { 'início carregou': (rs) => rs.every(ok) })
    espera(5, 15)
    const sorte = Math.random()
    if (sorte < 0.2) {
        const novo = http.post(`${API}/api/v1/objetos`, JSON.stringify({
            nome: 'Mochila de teste', categoria: 'MOCHILA_BOLSA', descricao: 'Criada pelo teste de carga',
            mensagemPublica: 'Se encontrar, me avise por aqui.',
        }), p('escrita', 'POST /objetos'))
        if (check(novo, { 'objeto criado': (r) => r.status === 201 })) {
            const id = novo.json('id')
            check(http.get(`${API}/api/v1/objetos/${id}`, p('leitura', 'GET /objetos/{id}')), { 'objeto abriu': ok })
            espera(2, 5)
            check(http.del(`${API}/api/v1/objetos/${id}`, null, p('escrita', 'DELETE /objetos/{id}')), { 'objeto apagado': ok })
        }
    } else if (sorte < 0.4) {
        check(http.get(`${API}/api/v1/avisos`, p('leitura', 'GET /avisos')), { 'avisos abriram': ok })
    }
}

export function handleSummary(data) {
    return resumo(data, 'Valtrea Volta')
}
