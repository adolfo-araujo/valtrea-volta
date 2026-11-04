# Valtrea Volta

Etiquetas com QR Code para objetos. Quem encontra um objeto lê o código com a câmera do celular e avisa o dono, sem que um veja os dados do outro.

| Parte | Pasta | Tecnologia | Onde roda |
| --- | --- | --- | --- |
| API | `api/` | Java 21, Spring Boot 4.1, PostgreSQL, Flyway | Render (`volta-api.valtrea.com.br`) |
| Site | `web/` | React, Vite, Tailwind | Vercel (`volta.valtrea.com.br`) |
| App | `android/` | Kotlin, Jetpack Compose | Google Play |

Quem encontra o objeto usa só o site (página `/v/{código}`), sem baixar app nem criar conta. O dono usa o site ou o app.

## Como funciona

1. O dono cadastra o objeto e gera uma etiqueta. O QR Code leva só `https://volta.valtrea.com.br/v/{código}`, com um código aleatório de 256 bits. Nada sobre o dono.
2. Quem encontra abre a página e vê o nome do objeto, a categoria e o recado do dono. Escreve uma mensagem e, se quiser, deixa um contato.
3. O dono recebe um e-mail ("Alguém encontrou: …"), sem a mensagem nem o contato, e lê tudo depois de entrar.
4. O dono marca o aviso como lido ou resolvido. Uma etiqueta perdida ou trocada pode ser desativada na hora.

## Segurança e privacidade

- O código da etiqueta fica no banco só como SHA-256 (para achar a etiqueta) e cifrado com AES-256-GCM (para o dono baixar o QR Code de novo).
- A página pública nunca devolve dado do dono; código errado, inexistente ou desativado têm a mesma resposta.
- Avisos sem conta têm limite por IP (10 por hora), campo invisível contra robôs, aceite obrigatório e no máximo 20 avisos sem ler por objeto.
- Só gera etiqueta quem confirmou o e-mail. Excluir a conta apaga tudo na hora e desliga as etiquetas.
- Retenção: avisos 90 dias, etiquetas desativadas 30 dias, cadastros nunca confirmados 30 dias.
- Os mesmos controles do Valtrea Pay: bcrypt, sessões de uso único, bloqueio de login, limites por IP, cabeçalhos de segurança, CORS só para o site, Sentry sem dados pessoais, backup diário criptografado, OSV-Scanner e gitleaks no CI.

## Rotas da API

Todas em `/api/v1`. Documentação interativa em `/swagger-ui.html`.

| Método | Rota | O que faz |
| --- | --- | --- |
| POST | `/auth/cadastro`, `/auth/login`, `/auth/renovar`, `/auth/sair` | Conta e sessão |
| POST | `/auth/confirmar-email`, `/auth/reenviar-confirmacao`, `/auth/esqueci-senha`, `/auth/redefinir-senha` | Códigos por e-mail |
| GET, PATCH, DELETE | `/eu` | Perfil, troca do nome e exclusão da conta |
| GET | `/eu/dados` | Baixar meus dados (JSON) |
| POST | `/eu/termos` | Aceite da versão vigente dos Termos |
| GET, POST | `/objetos` | Listar e cadastrar objetos (até 100) |
| GET, PUT, DELETE | `/objetos/{id}` | Ver, editar e excluir |
| POST | `/objetos/{id}/perdido` | Marcar como perdido |
| GET, POST | `/objetos/{id}/etiquetas` | Etiquetas (até 5 ativas por objeto) |
| POST | `/etiquetas/{id}/desativar` | Desativar etiqueta |
| GET | `/avisos` | Avisos recebidos |
| POST, DELETE | `/avisos/{id}/status`, `/avisos/{id}` | Marcar e apagar aviso |
| GET | `/publico/{código}` | Página de quem encontrou (sem login) |
| POST | `/publico/{código}/avisos` | Avisar o dono (sem login) |
| GET | `/admin/metricas` | Números de uso somados (só administradores) |

## Rodar no computador

```bash
# Banco
docker run -d --name volta-db -p 5432:5432 -e POSTGRES_USER=valtrea -e POSTGRES_PASSWORD=valtrea -e POSTGRES_DB=valtrea_volta postgres:18-alpine
# API (porta 8080)
cd api
VALTREA_JWT_SECRET=segredo-de-desenvolvimento-com-32-caracteres \
VALTREA_CHAVE_CIFRA=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY= \
VALTREA_URL_SITE=http://localhost:5175 VALTREA_CORS=http://localhost:5175 mvn spring-boot:run
# Site (porta 5175)
cd ../web && npm install && npm run dev
```

Sem SMTP configurado, os e-mails (códigos) aparecem no log da API. O app Android (variante `debug`) usa `http://10.0.2.2:8080/`.

## Publicar

1. **Banco (Neon):** crie o projeto `valtrea-volta` na região **AWS US East 1 (N. Virginia)**, a mesma do Render. O teste de capacidade do Pay mostrou que o banco em São Paulo com a API nos EUA deixa cada ida ao banco com ~120 ms.
2. **API (Render):** New → Blueprint → repositório `valtrea-volta`. Preencha `DATABASE_URL` (`jdbc:postgresql://<host>/<banco>?sslmode=require`), `DATABASE_USER`, `DATABASE_PASSWORD` e `RESEND_API_KEY`. Depois, em Settings → Custom Domains, adicione `volta-api.valtrea.com.br`.
3. **Site (Vercel):** Add New → Project → repositório `valtrea-volta`, **Root Directory** `web`, framework Vite. Em Environment Variables, `VITE_API_URL` = `https://volta-api.valtrea.com.br`. Em Domains, adicione `volta.valtrea.com.br`.
4. **DNS:** crie os registros que o Render e a Vercel mostrarem para `volta-api` e `volta`.
5. **App:** a variante `release` já aponta para `https://volta-api.valtrea.com.br/`.

O endereço do site vai dentro de cada QR Code: não mude `VALTREA_URL_SITE` depois de imprimir etiquetas.
