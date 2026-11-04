# Segurança

Os mesmos 20 itens revisados no Valtrea Finance e no Pay. O CI confere boa parte deles a cada mudança (testes da API com PostgreSQL real, testes no navegador, gitleaks e OSV-Scanner).

| # | Item | Como está |
| --- | --- | --- |
| 1 | Chaves de API escondidas | O app e o site só têm o endereço da API; segredos ficam no Render |
| 2 | Segredos fora do Git | `.gitignore` + gitleaks em todo o histórico |
| 3 | Chave pública do banco | Não se aplica: só a API fala com o banco |
| 4 | Acesso por linha | Toda consulta usa o dono do token; objeto, etiqueta ou aviso de outra pessoa responde 404 (testado) |
| 5 | Criptografia | HTTPS; banco cifrado em repouso; bcrypt; sessões SHA-256; códigos de e-mail HMAC; código da etiqueta SHA-256 + AES-256-GCM; backup AES-256 |
| 6 | Autenticação no servidor | JWT de 15 min em toda rota não pública; sessão de 30 dias com token de uso único |
| 7 | Restringir acessos | Página pública sem nenhum dado do dono; avisos só para o dono |
| 8 | Mass assignment | Pedidos são `record`s com só os campos permitidos |
| 9 | Cookies | Não usa: o login vai no cabeçalho Authorization |
| 10 | Hash de senha | bcrypt; 8 a 72 caracteres com letras e números |
| 11 | Limite de pedidos | 300/5 min por IP; 30 logins e 20 cadastros por IP a cada 15 min; 10 avisos por IP por hora; bloqueio após 5 senhas erradas |
| 12 | Robôs | Campo invisível nos avisos, aceite obrigatório, até 20 avisos sem ler por objeto, e-mail confirmado para gerar etiqueta |
| 13 | Consultas parametrizadas | JPA e SQL fixo com parâmetros |
| 14 | Validação | Bean Validation em todos os pedidos; corpo até 100 KB; código da etiqueta só no formato esperado |
| 15 | Vazamento de conteúdo | Erros sem detalhes; logs e Sentry com a rota modelo (sem o código da etiqueta) |
| 16 | Uploads | Não se aplica |
| 17 | Respostas enxutas | Só o que o app e o site usam |
| 18 | Cabeçalhos | API: CSP `default-src 'none'`, nosniff, X-Frame-Options, HSTS, Referrer-Policy, Permissions-Policy, COOP. Site: CSP só com o próprio endereço e a API, `Referrer-Policy: no-referrer` (o código da etiqueta não vaza para outros sites) |
| 19 | Forçar HTTPS | Render e Vercel redirecionam; o app de produção bloqueia HTTP |
| 20 | Dependências | OSV-Scanner (reprova falha crítica) e Dependabot para Maven, Gradle, npm, Docker e Actions |

CORS: a API só aceita chamadas de navegador vindas de `VALTREA_CORS` (o site do Volta).
