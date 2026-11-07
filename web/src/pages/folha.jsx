import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Printer } from 'lucide-react'
import { useQrCode } from '../components/qr'
import { chamar } from '../lib/api'

const TAMANHOS = {
  pequena: { rotulo: 'Pequena (chaveiro, 3 cm)', qr: 'h-[24mm] w-[24mm]', caixa: 'w-[42mm]', colunas: 'grid-cols-4', texto: 'text-[8pt]' },
  media: { rotulo: 'Média (mochila, 5 cm)', qr: 'h-[40mm] w-[40mm]', caixa: 'w-[58mm]', colunas: 'grid-cols-3', texto: 'text-[9pt]' },
  grande: { rotulo: 'Grande (mala, 7 cm)', qr: 'h-[60mm] w-[60mm]', caixa: 'w-[84mm]', colunas: 'grid-cols-2', texto: 'text-[10pt]' },
  pet: { rotulo: 'Plaquinha redonda de coleira (pet, 4 cm)', qr: 'h-[22mm] w-[22mm]', caixa: 'h-[40mm] w-[40mm] justify-center rounded-full', colunas: 'grid-cols-4', texto: 'text-[7pt]', pet: true },
}

function Etiqueta({ url, nome, tamanho }) {
  const qr = useQrCode(url, 1024)
  const t = TAMANHOS[tamanho]
  return (
    <div className={`${t.caixa} flex break-inside-avoid flex-col items-center ${t.pet ? '' : 'rounded-md'} border border-dashed border-black/40 p-[3mm] text-center`}>
      {t.pet && <p className={`${t.texto} mb-0.5 font-bold leading-tight`}>Me achou?</p>}
      {qr ? <img src={qr} alt={`QR Code da etiqueta de ${nome}`} className={t.qr} /> : <div className={`${t.qr} bg-black/5`} aria-hidden="true" />}
      {t.pet ? (
        <p className={`${t.texto} mt-0.5 leading-tight`}>Leia e avise meu dono</p>
      ) : (
        <>
          <p className={`${t.texto} mt-1 font-bold leading-tight`}>Achou? Leia o código</p>
          <p className={`${t.texto} leading-tight`}>e avise o dono</p>
        </>
      )}
    </div>
  )
}

/** Folha A4 com as etiquetas de todos os objetos, para imprimir de uma vez (de preferência em papel adesivo). */
export function FolhaEtiquetasPage() {
  const [itens, setItens] = useState(null)
  const [erro, setErro] = useState('')
  const [tamanho, setTamanho] = useState('media')
  const [copias, setCopias] = useState(1)

  useEffect(() => {
    ;(async () => {
      try {
        const objetos = (await chamar('GET', '/objetos')).filter((o) => o.etiquetasAtivas > 0)
        const listas = await Promise.all(objetos.map(async (o) => {
          const etiquetas = await chamar('GET', `/objetos/${o.id}/etiquetas`)
          return etiquetas.filter((e) => e.ativa).map((e) => ({ id: e.id, url: e.url, nome: o.nome }))
        }))
        setItens(listas.flat())
      } catch (e) {
        setErro(e.message)
      }
    })()
  }, [])

  const t = TAMANHOS[tamanho]
  const repetidas = (itens ?? []).flatMap((e) => Array.from({ length: copias }, (_, n) => ({ ...e, chave: `${e.id}-${n}` })))

  return (
    <main className="min-h-screen bg-white px-4 py-6 text-black print:p-0">
      <div className="mx-auto mb-6 max-w-[210mm] space-y-4 print:hidden">
        <Link to="/objetos" className="text-sm font-semibold underline">← Meus objetos</Link>
        <h1 className="text-2xl font-bold">Imprimir etiquetas</h1>
        <p className="text-sm">
          Todas as etiquetas ativas dos seus objetos numa folha A4. Funciona em papel comum (cole com fita larga
          transparente por cima) ou em papel adesivo. Na impressão, deixe a escala em 100%.
        </p>
        {tamanho === 'pet' && (
          <p className="rounded-lg border border-black/20 bg-black/5 p-3 text-sm">
            Para a coleira: recorte no círculo, cole num disco de acrílico ou de metal de 4 cm (vendido em papelarias e
            lojas de chaveiros) e proteja com plástico adesivo transparente. Uma gráfica rápida também imprime direto no
            acrílico com este arquivo: em Imprimir, escolha &quot;Salvar como PDF&quot; e leve o PDF.
          </p>
        )}
        <div className="flex flex-wrap items-end gap-4">
          <label className="text-sm font-semibold">
            Tamanho
            <select value={tamanho} onChange={(e) => setTamanho(e.target.value)}
              className="mt-1 block h-11 rounded-lg border border-black/30 bg-white px-3">
              {Object.entries(TAMANHOS).map(([v, x]) => <option key={v} value={v}>{x.rotulo}</option>)}
            </select>
          </label>
          <label className="text-sm font-semibold">
            Cópias de cada
            <select value={copias} onChange={(e) => setCopias(Number(e.target.value))}
              className="mt-1 block h-11 rounded-lg border border-black/30 bg-white px-3">
              {[1, 2, 3, 4, 6].map((n) => <option key={n} value={n}>{n}</option>)}
            </select>
          </label>
          <button type="button" onClick={() => window.print()} disabled={!itens?.length}
            className="inline-flex h-11 items-center gap-2 rounded-lg bg-black px-5 font-semibold text-white disabled:opacity-50">
            <Printer className="h-4 w-4" aria-hidden="true" /> Imprimir
          </button>
        </div>
        {erro && <p role="alert" className="text-sm font-semibold text-red-700">{erro}</p>}
        {itens === null && !erro && <p>Carregando…</p>}
        {itens?.length === 0 && (
          <p>Nenhuma etiqueta ativa ainda. Abra um objeto e toque em &quot;Gerar etiqueta&quot;.</p>
        )}
      </div>
      {repetidas.length > 0 && (
        <div className={`mx-auto grid max-w-[210mm] justify-items-center gap-[4mm] ${t.colunas}`}>
          {repetidas.map((e) => <Etiqueta key={e.chave} url={e.url} nome={e.nome} tamanho={tamanho} />)}
        </div>
      )}
    </main>
  )
}
