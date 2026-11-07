import { Link } from 'react-router-dom'
import { Pagina, estilos } from '../components/ui'
import { LEGAL } from '../lib/legal'

// Central de ajuda: perguntas frequentes do Valtrea Volta. Edite os textos aqui.
const SECOES = [
  [
    "Como funciona",
    [
      [
        "Como funciona o Volta?",
        "Você cadastra o objeto, gera uma etiqueta com QR Code e cola nele. Quem encontrar aponta a câmera do celular para o código, vê o seu recado e manda um aviso para você."
      ],
      [
        "Quem encontra precisa instalar alguma coisa ou criar conta?",
        "Não. Basta a câmera do celular: o QR Code abre uma página no navegador."
      ],
      [
        "O que quem encontra consegue ver?",
        "Só o nome do objeto e o recado que você escreveu. Nunca o seu nome, e-mail, telefone ou endereço."
      ],
      [
        "Perdi um objeto. O que eu faço?",
        "Abra o objeto e marque como perdido. O recado aparece em destaque para quem ler o QR Code, e você recebe o aviso por e-mail e no app quando alguém escrever."
      ]
    ]
  ],
  [
    "Etiquetas",
    [
      [
        "Como imprimo as etiquetas?",
        "Em cada objeto há os botões Baixar imagem e Imprimir. Para imprimir todas de uma vez, use \"Imprimir todas as etiquetas\" em Meus objetos e escolha o tamanho. Papel adesivo é o ideal; em papel comum, cole com fita larga transparente por cima."
      ],
      [
        "A etiqueta se perdeu ou o objeto mudou de dono.",
        "Abra o objeto e toque em Desativar na etiqueta. O QR Code para de funcionar na hora. Depois, gere uma nova se precisar."
      ]
    ]
  ],
  [
    "Avisos",
    [
      [
        "Recebi um aviso. E agora?",
        "Responda pelo contato que a pessoa deixou e combine a devolução em um lugar público e movimentado. Nunca pague para receber um objeto de volta e desconfie de quem pedir dinheiro."
      ],
      [
        "Alguém mandou um aviso falso ou ofensivo.",
        "Escreva para o e-mail de contato com o nome do objeto e a data do aviso. Os avisos são apagados sozinhos depois de 90 dias."
      ]
    ]
  ],
  [
    "Conta e dados",
    [
      [
        "O site demorou para abrir. É normal?",
        "Quando o serviço fica um tempo sem uso, a primeira ação pode levar até 1 minuto, porque o servidor estava em repouso. Aparece o aviso \"Acordando o servidor\" e, depois disso, fica rápido."
      ],
      [
        "Não recebi o e-mail de confirmação.",
        "Confira a caixa de spam e as abas Promoções ou Atualizações. Se não estiver lá, entre na conta e peça um novo código na tela de confirmação. O código vale pelo tempo informado no e-mail."
      ],
      [
        "Esqueci a senha.",
        "Na tela de entrar, toque em \"Esqueci a senha\" e informe seu e-mail. Você recebe um código para criar uma senha nova; ele vale pelo tempo informado no e-mail."
      ],
      [
        "Como baixo meus dados ou excluo a conta?",
        "Em Conta você baixa uma cópia de tudo o que guardamos sobre você e pode excluir a conta. A exclusão apaga seus dados na hora, exceto o que a lei manda guardar por um tempo (veja a Política de privacidade)."
      ],
      [
        "Dá para usar como app no celular?",
        "Sim. No Android, abra o site no Chrome e toque em \"Instalar app\" (ou baixe o app quando estiver na Play Store). No iPhone, abra no Safari, toque em Compartilhar e depois em \"Adicionar à Tela de Início\"."
      ]
    ]
  ]
]

export function AjudaPage() {
  return (
    <Pagina titulo="Central de ajuda" largura="max-w-3xl">
      <p className="text-muted">Respostas rápidas para as dúvidas mais comuns sobre o Valtrea Volta.</p>
      {SECOES.map(([titulo, perguntas]) => (
        <section key={titulo} aria-labelledby={`sec-${titulo}`} className="mt-8">
          <h2 id={`sec-${titulo}`} className="text-xl font-bold">{titulo}</h2>
          <div className="mt-3 divide-y divide-border rounded-2xl border border-border bg-surface">
            {perguntas.map(([p, r]) => (
              <details key={p} className="group p-5">
                <summary className="cursor-pointer list-none font-semibold marker:hidden">
                  <span className="flex items-center justify-between gap-4">{p}<span className="text-primary group-open:rotate-45" aria-hidden="true">+</span></span>
                </summary>
                <p className="mt-2 leading-relaxed text-muted">{r}</p>
              </details>
            ))}
          </div>
        </section>
      ))}
      <section className="mt-10 rounded-2xl border border-border bg-surface p-5">
        <h2 className="font-bold">Não achou a resposta?</h2>
        <p className="mt-1 text-sm text-muted">
          Escreva para <a href={`mailto:${LEGAL.email}`} className={estilos.link}>{LEGAL.email}</a>. Respondemos por e-mail.
          Veja também os <Link to="/termos" className={estilos.link}>Termos de uso</Link> e a{' '}
          <Link to="/privacidade" className={estilos.link}>Política de privacidade</Link>.
        </p>
      </section>
    </Pagina>
  )
}
