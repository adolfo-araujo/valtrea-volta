import { test, expect } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { execSync } from 'node:child_process'

// Confirma o e-mail direto no banco do CI (o código vai por e-mail, que no CI só aparece no log da API).
const confirmarNoBanco = (email) =>
  execSync(`psql "${process.env.DATABASE_URL_PSQL}" -c "UPDATE usuarios SET email_confirmado_em = now() WHERE email = '${email}'"`)

async function semProblemasDeAcessibilidade(page, nome) {
  const { violations } = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze()
  const lista = violations.map((v) => `- ${v.id}: ${v.help} (${v.nodes.length}x) ${v.nodes[0]?.target.join(' ')}`).join('\n')
  expect(violations, `Problemas de acessibilidade em ${nome}:\n${lista}`).toEqual([])
}

// Todos os testes falham se a CSP de produção bloquear algo ou se a página tiver erro de JavaScript.
test.beforeEach(async ({ page }, testInfo) => {
  testInfo.problemas = []
  page.on('console', (msg) => {
    if (msg.type() === 'error' && /Content Security Policy|Refused to/i.test(msg.text())) testInfo.problemas.push(msg.text())
  })
  page.on('pageerror', (e) => testInfo.problemas.push(e.message))
})
test.afterEach(async ({}, testInfo) => {
  expect(testInfo.problemas, testInfo.problemas.join('\n')).toEqual([])
})

for (const [nome, rota] of [
  ['início', '/'],
  ['entrar', '/entrar'],
  ['criar conta', '/cadastro'],
  ['esqueci a senha', '/esqueci-senha'],
  ['termos', '/termos'],
  ['privacidade', '/privacidade'],
  ['acessibilidade', '/acessibilidade'],
  ['excluir conta', '/excluir-conta'],
  ['etiqueta inexistente', '/v/codigo-que-nao-existe'],
]) {
  test(`acessibilidade: ${nome}`, async ({ page }) => {
    await page.goto(rota)
    await page.waitForLoadState('networkidle')
    await expect(page.locator('h1')).toBeVisible()
    await semProblemasDeAcessibilidade(page, nome)
  })
}

test('fluxo completo: cadastrar, gerar etiqueta, quem encontra avisa e o dono lê', async ({ page, browser }, testInfo) => {
  const email = `dono-${testInfo.project.name}-${Date.now()}@test.dev`

  // Dono cria a conta
  await page.goto('/cadastro')
  await page.getByLabel('Nome').fill('Dona Teste')
  await page.getByLabel('E-mail').fill(email)
  await page.getByLabel('Senha').fill('senha-forte-123')
  await page.getByLabel(/Tenho 18 anos/).check()
  await page.getByRole('button', { name: 'Criar conta' }).click()
  await expect(page.getByRole('heading', { name: 'Confirme seu e-mail' })).toBeVisible()
  confirmarNoBanco(email)

  // Cadastra o objeto e gera a etiqueta
  await page.goto('/objetos')
  await page.getByRole('button', { name: 'Cadastrar objeto' }).click()
  await page.getByLabel('Nome do objeto').fill('Mochila azul')
  await page.getByLabel('Recado para quem encontrar (opcional)').fill('Obrigada por avisar!')
  await page.getByLabel('Anotações só suas (opcional)').fill('Tem um chaveiro de gato')
  await page.getByRole('button', { name: 'Salvar objeto' }).click()
  await expect(page.getByRole('heading', { name: 'Mochila azul' })).toBeVisible()
  await semProblemasDeAcessibilidade(page, 'objeto')
  await page.getByRole('button', { name: 'Gerar etiqueta' }).click()
  await expect(page.getByRole('img', { name: 'QR Code da etiqueta de Mochila azul' })).toBeVisible()

  // O endereço do QR Code, como a API devolve
  const API = process.env.API_URL || 'http://localhost:5175'
  const accessToken = await page.evaluate(() => JSON.parse(localStorage.getItem('valtrea-volta:sessao')).accessToken)
  const headers = { Authorization: `Bearer ${accessToken}` }
  const [objeto] = await (await page.request.get(`${API}/api/v1/objetos`, { headers })).json()
  const etiquetas = await page.request.get(`${API}/api/v1/objetos/${objeto.id}/etiquetas`, { headers })
  const [etiqueta] = await etiquetas.json()
  const caminho = new URL(etiqueta.url).pathname

  // Quem encontrou: outra janela, sem conta
  const contexto = await browser.newContext()
  const achador = await contexto.newPage()
  await achador.goto(caminho)
  await expect(achador.getByRole('heading', { name: 'Mochila azul' })).toBeVisible()
  await expect(achador.getByText('Obrigada por avisar!')).toBeVisible()
  await expect(achador.getByText('Dona Teste')).toHaveCount(0)
  await expect(achador.getByText('chaveiro de gato')).toHaveCount(0)
  await semProblemasDeAcessibilidade(achador, 'página de quem encontrou')
  await achador.getByRole('textbox', { name: 'Mensagem', exact: true }).fill('Achei no ônibus 485, posso entregar amanhã.')
  await achador.getByLabel('Seu contato (opcional)').fill('achador@test.dev')
  await achador.getByLabel(/Concordo em enviar/).check()
  await achador.getByRole('button', { name: 'Enviar aviso' }).click()
  await expect(achador.getByText('Pronto! Avisamos o dono.')).toBeVisible()
  await contexto.close()

  // O dono lê o aviso
  await page.goto('/avisos')
  await expect(page.getByText('Achei no ônibus 485, posso entregar amanhã.')).toBeVisible()
  await expect(page.getByText('achador@test.dev')).toBeVisible()
  await semProblemasDeAcessibilidade(page, 'avisos')
  await page.getByRole('button', { name: 'Recuperei o objeto' }).click()
  await expect(page.getByText('Resolvido')).toBeVisible()
})
