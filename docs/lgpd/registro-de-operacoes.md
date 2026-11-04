# Registro das Operações de Tratamento de Dados Pessoais: Valtrea Volta

Documento interno (LGPD art. 37), no formato simplificado para agente de tratamento de pequeno porte (Resolução CD/ANPD nº 2/2022). Não publicar.

- **Controlador:** João Adolfo Martins Araújo (pessoa física), Rio de Janeiro/RJ
- **Encarregado / canal:** sem encarregado indicado (dispensa de pequeno porte); canal: contato@valtrea.com.br
- **Elaborado em:** 04/11/2026 · **Revisar:** a cada 12 meses ou a cada nova funcionalidade que trate dados
- **Serviço:** Valtrea Volta (site volta.valtrea.com.br, app Android e API volta-api.valtrea.com.br), em testes

## Operações

| # | Operação | Titulares | Dados pessoais | Sensíveis? | Finalidade | Base legal | Onde no sistema | Operadores | Transferência internacional | Retenção | Segurança |
|---|----------|-----------|----------------|-----------|-----------|-----------|-----------------|-----------|-----------------------------|----------|-----------|
| 1 | Cadastro e login | Donos (18+) | Nome, e-mail, senha (hash), aceite dos Termos | Não | Criar e manter a conta | Art. 7º, V | `/api/v1/auth/*` | Render, Neon | Sim (EUA) | Enquanto a conta existir; nunca confirmados sem objetos: 30 dias | bcrypt, JWT curto, sessões só como SHA-256, limites por IP, bloqueio por tentativas |
| 2 | Objetos | Donos | Nome, categoria, recado público, anotações privadas, "perdido" | Não | Prestar o serviço | Art. 7º, V | `/api/v1/objetos` | Render, Neon | Sim (EUA) | Enquanto a conta existir | Acesso só do dono (testado) |
| 3 | Etiquetas (QR Code) | Donos | Código aleatório ligado ao objeto | Não | Ligar o QR Code ao objeto | Art. 7º, V | `/api/v1/objetos/{id}/etiquetas` | Render, Neon | Sim (EUA) | Até desativar + 30 dias | SHA-256 para busca, AES-256-GCM para reexibir; 256 bits |
| 4 | Página pública | Visitantes | Nenhum dado do dono; IP só na memória | Não | Mostrar o objeto a quem encontrou | Art. 7º, V e IX | `/api/v1/publico/{código}`, site `/v/{código}` | Render, Vercel | Sim (EUA) | Não guarda | Resposta igual para código errado ou desativado |
| 5 | Avisos de quem encontrou | Quem encontrou | Mensagem e contato opcional | Não | Entregar a mensagem ao dono | Art. 7º, I (consentimento, caixa de aceite) | `/api/v1/publico/{código}/avisos` | Render, Neon | Sim (EUA) | 90 dias, ou até o dono apagar | Só o dono vê; 10 por hora por IP; campo invisível contra robôs; até 20 sem ler por objeto |
| 6 | E-mails | Donos | E-mail, nome, códigos, nome do objeto | Não | Confirmar e-mail, nova senha, avisos de segurança, "alguém encontrou" | Art. 7º, V | `email/*` | Resend | Não (São Paulo) | Códigos: 15 min, apagados 1 dia após vencer | O aviso não leva a mensagem nem o contato de quem encontrou |
| 7 | Prova do aceite | Donos | Versão e data do aceite | Não | Comprovar o aceite | Art. 7º, VI | `/api/v1/eu/termos`, cadastro | Render, Neon | Sim (EUA) | Enquanto a conta existir | — |
| 8 | Baixar meus dados | Donos | Tudo da conta | Não | Acesso e portabilidade | Art. 18, II e V | `/api/v1/eu/dados` | Render | Sim (EUA) | Não guarda | Exige login |
| 9 | Proteção contra abuso | Todos | IP, contagem de pedidos | Não | Bloquear força bruta e spam | Art. 7º, IX | `protecao/*` | Render | Sim (EUA) | IP só em memória, até 1 hora | — |
| 10 | Registro de erros | Todos | Dados técnicos, rota modelo (sem o código da etiqueta) | Não | Corrigir falhas | Art. 7º, IX | `monitoramento/*`, `ValtreaApp.kt` | Sentry | Sim (EUA) | Até 90 dias | Sem usuário, IP, corpo, cabeçalhos |
| 11 | Backup | Todos | Cópia do banco | Não | Recuperar dados | Art. 7º, IX | `.github/workflows/backup.yml` | GitHub | Sim (EUA) | 30 dias | AES-256, senha fora do GitHub |
| 12 | Painel de administração | Todos (em conjunto) | Só contagens | Não | Acompanhar o uso | Art. 7º, IX | `/api/v1/admin/metricas` | Render, Neon | Sim (EUA) | Não guarda | Só administradores |

## Direitos do titular

- Dono: corrigir nome, baixar os dados e excluir a conta em **Conta** (site ou app). Sem acesso: `/excluir-conta`.
- Quem enviou um aviso: pedir a exclusão por contato@valtrea.com.br (o dono também pode apagar o aviso).
- Resposta em até 15 dias.

## Operadores

| Fornecedor | Serviço | País | Contrato/DPA verificado? |
|-----------|---------|------|--------------------------|
| Render | API | EUA | Pendente |
| Neon | Banco de dados (AWS US East 1) | EUA | Pendente |
| Vercel | Site | EUA e rede global | Pendente |
| Resend | E-mails | Brasil (São Paulo) | Pendente |
| Sentry | Registro de erros | EUA | Pendente |
| GitHub | Backup criptografado | EUA | Pendente |
| ImprovMX / Google (Gmail) | E-mail de contato | EUA | Pendente |

## Pontos de atenção

- Uso indevido: alguém pode colar uma etiqueta em objeto alheio para receber mensagens. Os Termos proíbem, e o dono não descobre nada sobre quem encontrou, a não ser o contato que a pessoa deixar.
- O recado público é escrito pelo dono: o formulário avisa para não colocar telefone nem endereço.
