import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { AlertTriangle, CheckCircle2, ShieldCheck } from 'lucide-react'
import { CATEGORIAS, chamar } from '../lib/api'
import { Campo, CampoRobo, Mensagem, Pagina, estilos, useEnvio } from '../components/ui'

/**
 * Página aberta pelo QR Code. Quem encontrou não precisa de conta e não vê nada sobre o dono: só o nome do
 * objeto, a categoria e o recado que o dono escreveu.
 */
export function PublicoPage() {
  const { codigo } = useParams()
  const [objeto, setObjeto] = useState(null)
  const [naoEncontrado, setNaoEncontrado] = useState('')
  const [mensagem, setMensagem] = useState('')
  const [contato, setContato] = useState('')
  const [aceite, setAceite] = useState(false)
  const [website, setWebsite] = useState('')
  const [enviado, setEnviado] = useState(false)
  const { enviando, erro, setErro, executar } = useEnvio()

  useEffect(() => {
    chamar('GET', `/publico/${encodeURIComponent(codigo)}`, null, { autenticar: false })
      .then(setObjeto)
      .catch((e) => setNaoEncontrado(e.message))
  }, [codigo])

  const enviar = (e) => {
    e.preventDefault()
    if (mensagem.trim().length < 5) return setErro('Escreva uma mensagem curta para o dono (5 caracteres ou mais).')
    if (!aceite) return setErro('Marque que concorda em enviar a mensagem ao dono.')
    executar(async () => {
      await chamar('POST', `/publico/${encodeURIComponent(codigo)}/avisos`, { mensagem, contato, aceite, website }, { autenticar: false })
      setEnviado(true)
    })
  }

  if (naoEncontrado) {
    return (
      <Pagina titulo="Etiqueta não encontrada" largura="max-w-xl">
        <Mensagem tipo="erro">{naoEncontrado}</Mensagem>
      </Pagina>
    )
  }
  if (!objeto) return <Pagina largura="max-w-xl"><p className="text-muted">Carregando…</p></Pagina>

  return (
    <Pagina titulo="Você encontrou um objeto com dono" largura="max-w-xl">
      <section aria-labelledby="objeto-titulo" className={estilos.cartao}>
        {objeto.perdido && (
          <p className="mb-3 flex items-center gap-2 font-semibold text-danger">
            <AlertTriangle className="h-5 w-5" aria-hidden="true" /> O dono está procurando este objeto
          </p>
        )}
        <h2 id="objeto-titulo" className="text-xl font-extrabold">{objeto.nome}</h2>
        <p className="text-sm text-muted">{CATEGORIAS[objeto.categoria]}</p>
        {objeto.mensagemPublica && (
          <blockquote className="mt-4 border-l-2 border-primary pl-3 text-foreground">
            <p className="text-xs text-muted">Recado do dono</p>
            “{objeto.mensagemPublica}”
          </blockquote>
        )}
      </section>

      <div className="mt-4 flex gap-3 text-sm text-muted">
        <ShieldCheck className="h-5 w-5 shrink-0 text-primary" aria-hidden="true" />
        <p>
          Você não vê os dados do dono e ele não vê os seus, a não ser o contato que você decidir deixar. Para devolver,
          combine um lugar público e movimentado ou deixe em um balcão de achados e perdidos.
        </p>
      </div>

      {enviado ? (
        <div className={`${estilos.cartao} mt-6`}>
          <p role="status" className="flex items-start gap-2 font-semibold">
            <CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0 text-primary" aria-hidden="true" />
            Pronto! Avisamos o dono. Obrigado por ajudar.
          </p>
          {!contato && (
            <p className="mt-2 text-sm text-muted">
              Como você não deixou contato, o dono não tem como responder. Se quiser, envie outro aviso com um e-mail ou
              telefone.
            </p>
          )}
        </div>
      ) : (
        <form onSubmit={enviar} className={`${estilos.cartao} relative mt-6 space-y-4`} noValidate>
          <h2 className="text-lg font-bold">Avisar o dono</h2>
          <Campo rotulo="Mensagem" as="textarea" maxLength={1000} value={mensagem} onChange={(e) => setMensagem(e.target.value)}
            placeholder="Ex.: Encontrei na estação Uruguai do metrô. Posso entregar amanhã de manhã." />
          <Campo rotulo="Seu contato (opcional)" maxLength={120} value={contato} onChange={(e) => setContato(e.target.value)}
            placeholder="E-mail ou telefone" dica="Só o dono vê, para combinar a devolução. Você pode deixar em branco." />
          <CampoRobo valor={website} aoMudar={setWebsite} />
          <div className="flex items-start gap-2.5 text-sm">
            <input id="aceite-aviso" type="checkbox" checked={aceite} onChange={(e) => setAceite(e.target.checked)} className="mt-1 h-4 w-4 accent-[#55B02E]" />
            <label htmlFor="aceite-aviso" className="leading-relaxed text-muted">
              Concordo em enviar esta mensagem (e o contato, se eu deixar) ao dono do objeto, conforme a{' '}
              <Link to="/privacidade" target="_blank" className={estilos.link}>Política de privacidade</Link>. Ela é apagada em 90 dias.
            </label>
          </div>
          <Mensagem tipo="erro">{erro}</Mensagem>
          <button type="submit" className={`${estilos.primario} w-full`} disabled={enviando}>
            {enviando ? 'Enviando…' : 'Enviar aviso'}
          </button>
        </form>
      )}
    </Pagina>
  )
}
