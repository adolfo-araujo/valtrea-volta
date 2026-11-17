import { Link, Navigate } from 'react-router-dom'
import { BellRing, EyeOff, QrCode, ShieldCheck } from 'lucide-react'
import { useAuth } from '../lib/auth'
import { Pagina, estilos, ComoInstalarNoIphone, Abertura, IconeVidro } from '../components/ui'

const PASSOS = [
  { icone: QrCode, titulo: 'Cadastre e cole a etiqueta', texto: 'Cada objeto ganha um QR Code próprio. Imprima e cole na mochila, na chave, na mala.' },
  { icone: EyeOff, titulo: 'Quem encontrar lê o código', texto: 'Abre uma página com o nome do objeto e o seu recado. Nada sobre você: nem nome, nem telefone, nem endereço.' },
  { icone: BellRing, titulo: 'Você recebe o aviso', texto: 'A mensagem chega para você, com o contato que a pessoa decidir deixar, para combinar a devolução.' },
]

export function InicioPage() {
  const { perfil, carregando } = useAuth()
  if (!carregando && perfil) return <Navigate to="/objetos" replace />
  return (
    <Pagina>
      <Abertura selo="Valtrea Volta · em testes" titulo="Se perdeu," destaque="pode voltar."
        texto="Etiquetas com QR Code para os seus objetos. Quem encontrar avisa você sem ver o seu telefone nem o seu endereço.">
        <Link to="/cadastro" className={estilos.primario}>Criar conta grátis</Link>
        <Link to="/entrar" className={estilos.secundario}>Já tenho conta</Link>
      </Abertura>
      <ComoInstalarNoIphone />
      <section aria-labelledby="como-funciona" className="py-10">
        <h2 id="como-funciona" className="text-3xl font-extrabold sm:text-4xl">Como funciona</h2>
        <ol className="mt-8 grid gap-6 sm:grid-cols-3">
          {PASSOS.map(({ icone: Icone, titulo, texto }) => (
            <li key={titulo} className={estilos.cartao}>
              <IconeVidro icone={Icone} />
              <h3 className="mt-4 text-lg font-extrabold">{titulo}</h3>
              <p className="mt-1 text-sm leading-relaxed text-muted">{texto}</p>
            </li>
          ))}
        </ol>
        <p className="mt-8 flex items-start gap-2 text-sm text-muted">
          <ShieldCheck className="h-5 w-5 shrink-0 text-primary" aria-hidden="true" />
          O Volta facilita o contato, mas não garante a devolução: ela é combinada entre as pessoas.
        </p>
      </section>
    </Pagina>
  )
}
