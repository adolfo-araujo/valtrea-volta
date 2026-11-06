// Prepara o teste de carga: espera a API subir e cria as contas carga-N@test.dev.
// Depois, o passo "confirmar" marca os e-mails como confirmados direto no banco.
import http from 'k6/http'
import exec from 'k6/execution'
import { check } from 'k6'
import { API, emailDe, esperarApi, ipDe, SENHA } from './comum.js'

const USERS = Number(__ENV.USERS || 100)

export const options = {
    setupTimeout: '20m',
    scenarios: {
        preparo: { executor: 'shared-iterations', vus: Math.min(5, USERS), iterations: USERS, maxDuration: '40m' },
    },
    thresholds: { checks: ['rate>0.99'] },
}

export function setup() {
    esperarApi()
}

export default function () {
    const n = exec.scenario.iterationInTest
    const r = http.post(`${API}/api/v1/auth/cadastro`,
        JSON.stringify({ nome: `Carga Pessoa ${n}`, email: emailDe(n), senha: SENHA }),
        { headers: { 'Content-Type': 'application/json', 'CF-Connecting-IP': ipDe(n + 1) }, timeout: '120s' })
    if (!check(r, { 'cadastro 201': (x) => x.status === 201 })) console.error(`cadastro ${n}: ${r.status} ${r.body}`)
}
