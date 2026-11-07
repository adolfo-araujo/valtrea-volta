import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { AlertTriangle, ChevronRight, Download, Plus, Printer, QrCode as QrIcone, Trash2 } from 'lucide-react'
import { CATEGORIAS, chamar } from '../lib/api'
import { useAuth } from '../lib/auth'
import { Campo, Mensagem, Pagina, PrimeirosPassos, estilos, useEnvio } from '../components/ui'
import { QrCode, useQrCode } from '../components/qr'

const VAZIO = { nome: '', categoria: 'MOCHILA_BOLSA', descricao: '', mensagemPublica: '' }

function FormularioObjeto({ inicial = VAZIO, aoSalvar, rotuloBotao, aoCancelar }) {
  const [dados, setDados] = useState(inicial)
  const [erroNome, setErroNome] = useState('')
  const { enviando, erro, executar } = useEnvio()
  const muda = (campo) => (e) => setDados({ ...dados, [campo]: e.target.value })

  const enviar = (e) => {
    e.preventDefault()
    if (!dados.nome.trim()) return setErroNome('Dê um nome ao objeto.')
    setErroNome('')
    executar(() => aoSalvar(dados))
  }

  return (
    <form onSubmit={enviar} className="space-y-4" noValidate>
      <div className="grid gap-4 sm:grid-cols-2">
        <Campo rotulo="Nome do objeto" maxLength={80} value={dados.nome} onChange={muda('nome')} erro={erroNome}
          placeholder="Ex.: Mochila preta" />
        <Campo rotulo="Categoria" as="select" value={dados.categoria} onChange={muda('categoria')}>
          {Object.entries(CATEGORIAS).map(([valor, rotulo]) => (
            <option key={valor} value={valor}>{rotulo}</option>
          ))}
        </Campo>
      </div>
      <Campo rotulo="Recado para quem encontrar (opcional)" as="textarea" maxLength={200} value={dados.mensagemPublica}
        onChange={muda('mensagemPublica')} placeholder="Ex.: Obrigado por avisar! Tem valor sentimental."
        dica="Aparece para quem ler o QR Code. Não coloque telefone, endereço nem outros dados seus." />
      <Campo rotulo="Anotações só suas (opcional)" as="textarea" maxLength={500} value={dados.descricao}
        onChange={muda('descricao')} placeholder="Ex.: tem um chaveiro azul no zíper"
        dica="Ninguém mais vê. Ajuda a confirmar que o objeto achado é mesmo seu." />
      <Mensagem tipo="erro">{erro}</Mensagem>
      <div className="flex flex-col gap-2 sm:flex-row">
        <button type="submit" className={estilos.primario} disabled={enviando}>{rotuloBotao}</button>
        {aoCancelar && <button type="button" onClick={aoCancelar} className={estilos.secundario}>Cancelar</button>}
      </div>
    </form>
  )
}

function AvisoConfirmarEmail() {
  const { perfil } = useAuth()
  if (!perfil || perfil.emailConfirmado) return null
  return (
    <div className="mb-6">
      <Mensagem>
        Confirme seu e-mail para gerar etiquetas: é por ele que avisamos quando alguém encontrar.{' '}
        <Link to="/confirmar-email" className={estilos.link}>Confirmar agora</Link>
      </Mensagem>
    </div>
  )
}

export function ObjetosPage() {
  const { perfil } = useAuth()
  const [objetos, setObjetos] = useState(null)
  const [criando, setCriando] = useState(false)
  const [erro, setErro] = useState('')
  const navegar = useNavigate()

  useEffect(() => {
    chamar('GET', '/objetos').then(setObjetos).catch((e) => setErro(e.message))
  }, [])

  return (
    <Pagina titulo="Meus objetos">
      <AvisoConfirmarEmail />
      {objetos && (
        <PrimeirosPassos passos={[
          { feito: Boolean(perfil?.emailConfirmado), titulo: 'Confirme seu e-mail', texto: 'É por ele que avisamos quando alguém encontrar.', para: '/confirmar-email', rotulo: 'Confirmar agora' },
          { feito: objetos.length > 0, titulo: 'Cadastre o objeto que você mais teme perder', texto: 'Mochila, chaves, carteira, mala, bicicleta.' },
          { feito: objetos.some((o) => o.etiquetasAtivas > 0), titulo: 'Gere a etiqueta e cole no objeto', texto: 'Abra o objeto e toque em Gerar etiqueta. Depois, imprima e cole.' },
        ]} />
      )}
      <Mensagem tipo="erro">{erro}</Mensagem>
      {criando ? (
        <section aria-labelledby="novo-titulo" className={`${estilos.cartao} mb-6`}>
          <h2 id="novo-titulo" className="mb-4 text-lg font-bold">Novo objeto</h2>
          <FormularioObjeto rotuloBotao="Salvar objeto" aoCancelar={() => setCriando(false)}
            aoSalvar={async (dados) => {
              const novo = await chamar('POST', '/objetos', dados)
              navegar(`/objetos/${novo.id}`)
            }} />
        </section>
      ) : (
        <div className="mb-6 flex flex-wrap gap-2">
          <button type="button" onClick={() => setCriando(true)} className={estilos.primario}>
            <Plus className="h-4 w-4" aria-hidden="true" /> Cadastrar objeto
          </button>
          {objetos?.some((o) => o.etiquetasAtivas > 0) && (
            <Link to="/etiquetas/imprimir" className={estilos.secundario}>
              <Printer className="h-4 w-4" aria-hidden="true" /> Imprimir todas as etiquetas
            </Link>
          )}
        </div>
      )}

      {objetos === null && !erro && <p className="text-muted">Carregando…</p>}
      {objetos?.length === 0 && !criando && (
        <div className={estilos.cartao}>
          <h2 className="font-bold">Comece pelo objeto que você mais teme perder</h2>
          <p className="mt-2 text-muted">
            Cadastre, gere a etiqueta com QR Code, imprima e cole. Se alguém encontrar, lê o código com a câmera do
            celular e avisa você, sem ver seu telefone nem seu endereço.
          </p>
        </div>
      )}
      {objetos?.length > 0 && (
        <ul className="divide-y divide-border overflow-hidden rounded-2xl border border-border bg-surface">
          {objetos.map((o) => (
            <li key={o.id}>
              <Link to={`/objetos/${o.id}`} className="flex items-center gap-4 px-5 py-4 hover:bg-surface-2">
                <div className="min-w-0 flex-1">
                  <p className="truncate font-bold">{o.nome}</p>
                  <p className="text-sm text-muted">
                    {CATEGORIAS[o.categoria]} · {o.etiquetasAtivas === 1 ? '1 etiqueta' : `${o.etiquetasAtivas} etiquetas`}
                  </p>
                </div>
                {o.perdido && (
                  <span className="rounded-full border border-danger/60 px-2.5 py-0.5 text-xs font-semibold text-danger">Perdido</span>
                )}
                {o.avisosNovos > 0 && (
                  <span className="rounded-full bg-primary px-2.5 py-0.5 text-xs font-bold text-background">
                    {o.avisosNovos} {o.avisosNovos === 1 ? 'aviso novo' : 'avisos novos'}
                  </span>
                )}
                <ChevronRight className="h-5 w-5 text-muted" aria-hidden="true" />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </Pagina>
  )
}

function CartaoEtiqueta({ etiqueta, nome, aoDesativar, aoImprimir }) {
  const imagem = useQrCode(etiqueta.url, 1024)
  const nomeArquivo = `etiqueta-${nome.toLowerCase().normalize('NFD').replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')}.png`
  if (!etiqueta.ativa) return null
  return (
    <li className="flex flex-col items-center gap-4 rounded-xl border border-border bg-surface-2 p-4 sm:flex-row sm:items-start">
      <QrCode url={etiqueta.url} nome={nome} />
      <div className="flex flex-1 flex-col gap-2">
        <p className="text-sm text-muted">Criada em {new Date(etiqueta.criadaEm).toLocaleDateString('pt-BR')}</p>
        <div className="flex flex-wrap gap-2">
          <a href={imagem ?? undefined} download={nomeArquivo} className={estilos.secundario} aria-disabled={!imagem}>
            <Download className="h-4 w-4" aria-hidden="true" /> Baixar imagem
          </a>
          <button type="button" onClick={() => imagem && aoImprimir(imagem)} className={estilos.secundario}>
            <Printer className="h-4 w-4" aria-hidden="true" /> Imprimir
          </button>
          <button type="button" onClick={aoDesativar} className={estilos.perigo}>
            Desativar
          </button>
        </div>
        <p className="text-xs text-muted">
          Desative se a etiqueta se perder ou se o objeto mudar de dono: o QR Code para de funcionar na hora.
        </p>
      </div>
    </li>
  )
}

export function ObjetoPage() {
  const { id } = useParams()
  const navegar = useNavigate()
  const { perfil } = useAuth()
  const [objeto, setObjeto] = useState(null)
  const [etiquetas, setEtiquetas] = useState([])
  const [editando, setEditando] = useState(false)
  const [confirmarExclusao, setConfirmarExclusao] = useState(false)
  const [paraImprimir, setParaImprimir] = useState(null)
  const { enviando, erro, executar } = useEnvio()

  // Monta a versão de impressão (só a etiqueta) e abre a janela de impressão do navegador
  useEffect(() => {
    if (!paraImprimir) return undefined
    const t = setTimeout(() => window.print(), 50)
    const depois = () => setParaImprimir(null)
    window.addEventListener('afterprint', depois)
    return () => {
      clearTimeout(t)
      window.removeEventListener('afterprint', depois)
    }
  }, [paraImprimir])

  const carregar = useCallback(() => executar(async () => {
    const [o, e] = await Promise.all([chamar('GET', `/objetos/${id}`), chamar('GET', `/objetos/${id}/etiquetas`)])
    setObjeto(o)
    setEtiquetas(e)
  }), [id]) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    carregar()
  }, [carregar])

  if (!objeto) {
    return (
      <Pagina titulo="Objeto">
        {erro ? <Mensagem tipo="erro">{erro}</Mensagem> : <p className="text-muted">Carregando…</p>}
      </Pagina>
    )
  }

  const ativas = etiquetas.filter((e) => e.ativa)

  return (
    <Pagina titulo={objeto.nome}>
      <div className="no-print space-y-6">
        <AvisoConfirmarEmail />
        <Mensagem tipo="erro">{erro}</Mensagem>

        <section aria-labelledby="perdido-titulo" className={`${estilos.cartao} flex flex-col gap-3 sm:flex-row sm:items-center`}>
          <AlertTriangle className={`h-6 w-6 shrink-0 ${objeto.perdido ? 'text-danger' : 'text-muted'}`} aria-hidden="true" />
          <div className="flex-1">
            <h2 id="perdido-titulo" className="font-bold">{objeto.perdido ? 'Marcado como perdido' : 'Perdeu este objeto?'}</h2>
            <p className="text-sm text-muted">
              {objeto.perdido
                ? 'Quem ler o QR Code vê um destaque pedindo para avisar você.'
                : 'Marque como perdido para destacar o pedido de ajuda na página do QR Code.'}
            </p>
          </div>
          <button type="button" className={objeto.perdido ? estilos.secundario : estilos.perigo} disabled={enviando}
            onClick={() => executar(async () => setObjeto(await chamar('POST', `/objetos/${id}/perdido`, { perdido: !objeto.perdido })))}>
            {objeto.perdido ? 'Já encontrei' : 'Marcar como perdido'}
          </button>
        </section>

        <section aria-labelledby="etiquetas-titulo" className={estilos.cartao}>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <h2 id="etiquetas-titulo" className="text-lg font-bold">Etiquetas com QR Code</h2>
            <button type="button" className={estilos.primario} disabled={enviando || !perfil?.emailConfirmado}
              onClick={() => executar(async () => {
                await chamar('POST', `/objetos/${id}/etiquetas`)
                await carregar()
              })}>
              <QrIcone className="h-4 w-4" aria-hidden="true" /> Gerar etiqueta
            </button>
          </div>
          {ativas.length === 0 ? (
            <p className="text-sm text-muted">
              Nenhuma etiqueta ativa. Gere uma, baixe ou imprima e cole no objeto (uma etiqueta adesiva resistente à água
              funciona bem).
            </p>
          ) : (
            <ul className="space-y-3">
              {ativas.map((e) => (
                <CartaoEtiqueta key={e.id} etiqueta={e} nome={objeto.nome} aoImprimir={setParaImprimir}
                  aoDesativar={() => executar(async () => {
                    await chamar('POST', `/etiquetas/${e.id}/desativar`)
                    await carregar()
                  })} />
              ))}
            </ul>
          )}
        </section>

        <section aria-labelledby="dados-titulo" className={estilos.cartao}>
          <div className="mb-4 flex items-center justify-between">
            <h2 id="dados-titulo" className="text-lg font-bold">Dados do objeto</h2>
            {!editando && <button type="button" className={estilos.secundario} onClick={() => setEditando(true)}>Editar</button>}
          </div>
          {editando ? (
            <FormularioObjeto rotuloBotao="Salvar alterações" aoCancelar={() => setEditando(false)}
              inicial={{ nome: objeto.nome, categoria: objeto.categoria, descricao: objeto.descricao ?? '', mensagemPublica: objeto.mensagemPublica ?? '' }}
              aoSalvar={async (dados) => {
                setObjeto(await chamar('PUT', `/objetos/${id}`, dados))
                setEditando(false)
              }} />
          ) : (
            <dl className="grid gap-4 text-sm sm:grid-cols-2">
              <div><dt className="text-muted">Categoria</dt><dd className="font-semibold">{CATEGORIAS[objeto.categoria]}</dd></div>
              <div><dt className="text-muted">Recado para quem encontrar</dt><dd>{objeto.mensagemPublica || '—'}</dd></div>
              <div className="sm:col-span-2"><dt className="text-muted">Anotações só suas</dt><dd>{objeto.descricao || '—'}</dd></div>
            </dl>
          )}
        </section>

        <section aria-labelledby="excluir-titulo" className={estilos.cartao}>
          <h2 id="excluir-titulo" className="font-bold">Excluir objeto</h2>
          <p className="mt-1 text-sm text-muted">Apaga o objeto, as etiquetas (os QR Codes param de funcionar) e os avisos dele.</p>
          {confirmarExclusao ? (
            <div className="mt-3 flex flex-col gap-2 sm:flex-row">
              <button type="button" className={estilos.perigo} disabled={enviando}
                onClick={() => executar(async () => {
                  await chamar('DELETE', `/objetos/${id}`)
                  navegar('/objetos')
                })}>
                <Trash2 className="h-4 w-4" aria-hidden="true" /> Sim, excluir
              </button>
              <button type="button" className={estilos.secundario} onClick={() => setConfirmarExclusao(false)}>Cancelar</button>
            </div>
          ) : (
            <button type="button" className={`${estilos.perigo} mt-3`} onClick={() => setConfirmarExclusao(true)}>Excluir objeto</button>
          )}
        </section>
      </div>

      {/* Versão para imprimir: QR Code com uns 5 cm e a instrução para quem encontrar */}
      {paraImprimir && (
        <div className="print-only" style={{ width: '6cm', textAlign: 'center', border: '1px dashed #999', padding: '0.4cm' }}>
          <img src={paraImprimir} alt="" style={{ width: '5cm', height: '5cm', margin: '0 auto' }} />
          <p style={{ fontSize: '10pt', fontWeight: 700, marginTop: '0.2cm' }}>Achou? Leia o QR Code e avise o dono.</p>
          <p style={{ fontSize: '8pt' }}>Valtrea Volta · sem expor os dados de ninguém</p>
        </div>
      )}
    </Pagina>
  )
}
