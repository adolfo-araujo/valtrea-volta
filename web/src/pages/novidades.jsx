import { Pagina } from '../components/ui'

// O que mudou em cada versão. Acrescente no topo a cada novidade.
const VERSOES = [
  [
    "7 de novembro de 2026",
    [
      "Busca rápida: aperte Ctrl+K (ou / ) no computador para ir a qualquer tela."
    ]
  ],
  [
    "6 de novembro de 2026",
    [
      "Imprimir todas as etiquetas numa folha A4, em três tamanhos e com cópias de cada.",
      "Novo visual da marca Valtrea: fonte Inter e logo própria do produto.",
      "Dá para instalar como app no iPhone: no Safari, Compartilhar → Adicionar à Tela de Início.",
      "Quando o servidor estava parado, aparece \"Acordando o servidor\" em vez de parecer travado.",
      "Central de ajuda com as dúvidas mais comuns e prévia com imagem ao compartilhar o link.",
      "Termos de uso e Política de privacidade revisados (cookies e propriedade intelectual)."
    ]
  ],
  [
    "5 de novembro de 2026",
    [
      "Telas de entrar e criar conta mais simples, com confirmação de senha."
    ]
  ],
  [
    "4 de novembro de 2026",
    [
      "Lançamento da versão de testes: objetos, etiquetas com QR Code, avisos de quem encontrou, objeto perdido, baixar meus dados e excluir conta."
    ]
  ]
]

export function NovidadesPage() {
  return (
    <Pagina titulo="Novidades" largura="max-w-3xl">
      <p className="text-muted">O que mudou no Valtrea Volta, do mais recente para o mais antigo.</p>
      <ol className="mt-8 space-y-8 border-l-2 border-border pl-6">
        {VERSOES.map(([data, itens]) => (
          <li key={data}>
            <h2 className="text-lg font-bold">{data}</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5 text-muted">
              {itens.map((i) => <li key={i}>{i}</li>)}
            </ul>
          </li>
        ))}
      </ol>
    </Pagina>
  )
}
