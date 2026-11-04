import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { CheckCircle2, ShieldCheck, Trash2 } from 'lucide-react'
import { chamar } from '../lib/api'
import { Mensagem, Pagina, estilos, useEnvio } from '../components/ui'

const STATUS = { NOVO: 'Novo', LIDO: 'Lido', RESOLVIDO: 'Resolvido' }

export function AvisosPage() {
  const [avisos, setAvisos] = useState(null)
  const { enviando, erro, executar } = useEnvio()

  const carregar = () => executar(async () => setAvisos(await chamar('GET', '/avisos')))
  useEffect(() => {
    carregar()
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const marcar = (id, status) => executar(async () => {
    const novo = await chamar('POST', `/avisos/${id}/status`, { status })
    setAvisos((lista) => lista.map((a) => (a.id === id ? novo : a)))
  })

  const apagar = (id) => executar(async () => {
    await chamar('DELETE', `/avisos/${id}`)
    setAvisos((lista) => lista.filter((a) => a.id !== id))
  })

  return (
    <Pagina titulo="Avisos de quem encontrou">
      <div className={`${estilos.cartao} mb-6 flex gap-3 text-sm`}>
        <ShieldCheck className="h-5 w-5 shrink-0 text-primary" aria-hidden="true" />
        <p className="text-muted">
          Para a devolução, combine um lugar público e movimentado, de dia, ou peça para deixarem o objeto em um balcão de
          achados e perdidos. Nunca pague para receber algo de volta. Os avisos são apagados 90 dias depois de chegarem.
        </p>
      </div>
      <Mensagem tipo="erro">{erro}</Mensagem>
      {avisos === null && !erro && <p className="text-muted">Carregando…</p>}
      {avisos?.length === 0 && <p className="text-muted">Nenhum aviso por enquanto.</p>}
      {avisos?.length > 0 && (
        <ul className="space-y-4">
          {avisos.map((a) => (
            <li key={a.id} className={estilos.cartao}>
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="font-bold">
                  <Link to={`/objetos/${a.objetoId}`} className="hover:underline">{a.objetoNome}</Link>
                </h2>
                <span className={`rounded-full border px-2.5 py-0.5 text-xs font-semibold ${a.status === 'NOVO' ? 'border-primary text-primary' : 'border-border text-muted'}`}>
                  {STATUS[a.status]}
                </span>
                <span className="text-sm text-muted">
                  {new Date(a.criadoEm).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}
                </span>
              </div>
              <p className="mt-3 whitespace-pre-line">{a.mensagem}</p>
              <p className="mt-2 text-sm text-muted">
                Contato deixado: <span className="text-foreground">{a.contato || 'nenhum (a pessoa preferiu não deixar)'}</span>
              </p>
              <div className="mt-4 flex flex-wrap gap-2">
                {a.status === 'NOVO' && (
                  <button type="button" className={estilos.secundario} disabled={enviando} onClick={() => marcar(a.id, 'LIDO')}>
                    Marcar como lido
                  </button>
                )}
                {a.status !== 'RESOLVIDO' && (
                  <button type="button" className={estilos.primario} disabled={enviando} onClick={() => marcar(a.id, 'RESOLVIDO')}>
                    <CheckCircle2 className="h-4 w-4" aria-hidden="true" /> Recuperei o objeto
                  </button>
                )}
                <button type="button" className={estilos.perigo} disabled={enviando} onClick={() => apagar(a.id)}
                  aria-label={`Apagar aviso sobre ${a.objetoNome}`}>
                  <Trash2 className="h-4 w-4" aria-hidden="true" /> Apagar
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </Pagina>
  )
}
