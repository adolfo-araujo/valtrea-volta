import { useId, useState, useEffect } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { Bell, LogOut, Package, User } from 'lucide-react'
import { useAuth } from '../lib/auth'
import { LEGAL } from '../lib/legal'

export const estilos = {
  primario:
    'inline-flex h-11 items-center justify-center gap-2 rounded-lg bg-primary px-5 font-semibold text-background transition-colors hover:bg-primary-hover disabled:opacity-60',
  secundario:
    'inline-flex h-11 items-center justify-center gap-2 rounded-lg border border-border bg-surface-2 px-5 font-semibold transition-colors hover:bg-border disabled:opacity-60',
  perigo:
    'inline-flex h-11 items-center justify-center gap-2 rounded-lg border border-danger/60 px-5 font-semibold text-danger transition-colors hover:bg-danger/10 disabled:opacity-60',
  link: 'font-semibold text-primary underline-offset-2 hover:underline',
  cartao: 'rounded-2xl border border-border bg-surface p-5 sm:p-6',
}

export function Logo({ className = '' }) {
  return (
    <span className={`flex items-center gap-2.5 ${className}`}>
      <svg viewBox="0 0 32 32" className="h-8 w-8" aria-hidden="true">
        <rect width="32" height="32" rx="8" fill="#0F3D2E" />
        <path d="M7.5 9.5a2 2 0 0 1 2-2h6.3a2 2 0 0 1 1.4.6l7.7 7.7a2 2 0 0 1 0 2.8l-6.3 6.3a2 2 0 0 1-2.8 0L8.1 17.2a2 2 0 0 1-.6-1.4z" stroke="#55B02E" strokeWidth="2.6" fill="none" strokeLinejoin="round" />
        <circle cx="12.3" cy="12.3" r="1.9" fill="#55B02E" />
      </svg>
      <span className="text-lg font-semibold tracking-tight">
        Valtrea <span className="text-primary">Volta</span>
      </span>
    </span>
  )
}

/** Campo com rótulo sempre visível e mensagem de erro ligada a ele (WCAG 1.3.1 e 3.3.1). */
export function Campo({ rotulo, erro, dica, as = 'input', className = '', ...props }) {
  const id = useId()
  const Elemento = as
  const descricao = [dica && `${id}-dica`, erro && `${id}-erro`].filter(Boolean).join(' ') || undefined
  return (
    <div className={className}>
      <label htmlFor={id} className="text-sm font-semibold">
        {rotulo}
      </label>
      <Elemento
        id={id}
        aria-invalid={erro ? true : undefined}
        aria-describedby={descricao}
        className={`mt-1 w-full rounded-lg border bg-background px-3 text-foreground placeholder:text-muted ${
          as === 'textarea' ? 'min-h-28 py-2' : 'h-11'
        } ${erro ? 'border-danger' : 'border-border'}`}
        {...props}
      />
      {dica && (
        <p id={`${id}-dica`} className="mt-1 text-xs text-muted">
          {dica}
        </p>
      )}
      {erro && (
        <p id={`${id}-erro`} className="mt-1 text-sm text-danger">
          {erro}
        </p>
      )}
    </div>
  )
}

/** Campo invisível para pessoas: robôs que preenchem tudo caem aqui. */
export function CampoRobo({ valor, aoMudar }) {
  return (
    <div className="absolute -left-[9999px] h-px w-px overflow-hidden" aria-hidden="true">
      <label htmlFor="site-campo">Site</label>
      <input id="site-campo" tabIndex={-1} autoComplete="off" value={valor} onChange={(e) => aoMudar(e.target.value)} />
    </div>
  )
}

export function Mensagem({ tipo = 'info', children }) {
  if (!children) return null
  const cor = tipo === 'erro' ? 'border-danger/50 text-danger' : 'border-primary/40 text-foreground'
  return (
    <p role={tipo === 'erro' ? 'alert' : 'status'} className={`rounded-lg border bg-surface-2 px-4 py-3 text-sm ${cor}`}>
      {children}
    </p>
  )
}

export function useEnvio() {
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')
  const executar = async (acao) => {
    setEnviando(true)
    setErro('')
    try {
      return await acao()
    } catch (e) {
      setErro(e.message)
      return undefined
    } finally {
      setEnviando(false)
    }
  }
  return { enviando, erro, setErro, executar }
}

function Rodape() {
  return (
    <footer className="no-print mt-16 border-t border-border">
      <div className="mx-auto flex max-w-5xl flex-col items-center gap-3 px-4 py-8 text-sm text-muted sm:flex-row sm:justify-between">
        <nav aria-label="Documentos" className="flex flex-wrap justify-center gap-x-5 gap-y-1">
          <Link to="/termos" className="hover:text-foreground hover:underline">Termos de uso</Link>
          <Link to="/privacidade" className="hover:text-foreground hover:underline">Privacidade</Link>
          <Link to="/acessibilidade" className="hover:text-foreground hover:underline">Acessibilidade</Link>
          <a href={`mailto:${LEGAL.email}`} className="hover:text-foreground hover:underline">{LEGAL.email}</a>
        </nav>
        <p className="text-xs">© 2026 Valtrea · {LEGAL.controller} · {LEGAL.forum}</p>
      </div>
    </footer>
  )
}

export function Pagina({ titulo, children, largura = 'max-w-5xl' }) {
  const { perfil, sair } = useAuth()
  const navegar = useNavigate()
  const item = ({ isActive }) =>
    `inline-flex h-10 items-center gap-1.5 rounded-lg px-3 text-sm font-semibold ${isActive ? 'bg-surface-2 text-foreground' : 'text-muted hover:text-foreground'}`
  return (
    <div className="flex min-h-screen flex-col">
      <a
        href="#conteudo"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-lg focus:bg-primary focus:px-4 focus:py-2 focus:font-semibold focus:text-background"
      >
        Pular para o conteúdo
      </a>
      <header className="no-print border-b border-border/60">
        <div className="mx-auto flex h-16 max-w-5xl items-center justify-between gap-2 px-4">
          <Link to={perfil ? '/objetos' : '/'} aria-label="Valtrea Volta, início">
            <Logo />
          </Link>
          {perfil ? (
            <nav aria-label="Principal" className="flex items-center gap-1">
              <NavLink to="/objetos" className={item}>
                <Package className="h-4 w-4" aria-hidden="true" /> <span className="hidden sm:inline">Objetos</span>
                <span className="sr-only sm:hidden">Objetos</span>
              </NavLink>
              <NavLink to="/avisos" className={item}>
                <Bell className="h-4 w-4" aria-hidden="true" /> <span className="hidden sm:inline">Avisos</span>
                <span className="sr-only sm:hidden">Avisos</span>
              </NavLink>
              <NavLink to="/conta" className={item}>
                <User className="h-4 w-4" aria-hidden="true" /> <span className="hidden sm:inline">Conta</span>
                <span className="sr-only sm:hidden">Conta</span>
              </NavLink>
              <button
                type="button"
                onClick={async () => {
                  await sair()
                  navegar('/')
                }}
                className="inline-flex h-10 w-10 items-center justify-center rounded-lg text-muted hover:text-foreground"
                aria-label="Sair"
              >
                <LogOut className="h-4 w-4" aria-hidden="true" />
              </button>
            </nav>
          ) : (
            <nav aria-label="Principal" className="flex items-center gap-2 text-sm font-semibold">
              <Link to="/entrar" className="rounded-lg px-3 py-2 text-muted hover:text-foreground">
                Entrar
              </Link>
              <Link to="/cadastro" className="inline-flex h-9 items-center rounded-lg bg-primary px-4 text-background hover:bg-primary-hover">
                Criar conta
              </Link>
            </nav>
          )}
        </div>
      </header>
      <main id="conteudo" tabIndex={-1} className={`mx-auto w-full flex-1 px-4 py-8 ${largura}`}>
        {titulo && <h1 className="no-print mb-6 text-2xl font-bold tracking-tight sm:text-3xl">{titulo}</h1>}
        {children}
      </main>
      <Rodape />
    </div>
  )
}

/**
 * Tela das páginas de conta (entrar, criar conta, senha): centralizada, com o logo em cima, o formulário num cartão,
 * os links logo abaixo e o rodapé enxuto. Mesmo visual do Valtrea Finance.
 */
export function TelaAuth({ titulo, subtitulo, children, abaixo }) {
  return (
    <div className="flex min-h-screen flex-col">
      <a
        href="#conteudo"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-lg focus:bg-primary focus:px-4 focus:py-2 focus:font-semibold focus:text-background"
      >
        Pular para o conteúdo
      </a>
      <main id="conteudo" tabIndex={-1} className="flex flex-1 flex-col items-center justify-center px-4 py-10">
        <Link to="/" aria-label="Valtrea Volta, início" className="mb-8">
          <Logo />
        </Link>
        <div className="w-full max-w-md rounded-2xl border border-border bg-surface p-6 shadow-xl shadow-black/20 sm:p-8">
          <h1 className="text-3xl font-bold tracking-tight">{titulo}</h1>
          {subtitulo && <p className="mt-1 text-muted">{subtitulo}</p>}
          <div className="mt-6">{children}</div>
        </div>
        {abaixo && <div className="mt-6 text-center text-sm text-muted">{abaixo}</div>}
      </main>
      <footer className="pb-8">
        <nav aria-label="Documentos" className="flex flex-wrap justify-center gap-x-5 gap-y-1 px-4 text-sm text-muted">
          <Link to="/termos" className="hover:text-foreground hover:underline">Termos de uso</Link>
          <Link to="/privacidade" className="hover:text-foreground hover:underline">Política de privacidade</Link>
          <Link to="/acessibilidade" className="hover:text-foreground hover:underline">Acessibilidade</Link>
          <a href={`mailto:${LEGAL.email}`} className="hover:text-foreground hover:underline">{LEGAL.email}</a>
        </nav>
      </footer>
    </div>
  )
}

/** Dica de instalação no iPhone: o Safari não tem botão de instalar, então explicamos o caminho. */
export function ComoInstalarNoIphone() {
  const iphone = typeof navigator !== 'undefined' && /iPhone|iPad|iPod/.test(navigator.userAgent)
  const instalado = typeof window !== 'undefined' &&
    (window.matchMedia?.('(display-mode: standalone)').matches || window.navigator.standalone === true)
  if (!iphone || instalado) return null
  return (
    <p className="mt-6 rounded-xl border border-border bg-surface p-4 text-sm leading-relaxed text-muted">
      <strong className="text-foreground">Use como app no iPhone:</strong> no Safari, toque em{' '}
      <strong className="text-foreground">Compartilhar</strong> e depois em{' '}
      <strong className="text-foreground">Adicionar à Tela de Início</strong>.
    </p>
  )
}

/**
 * Aviso de "servidor acordando". No plano grátis do Render, a API dorme depois de 15 minutos sem uso e
 * leva cerca de 1 minuto para voltar. Sem este aviso, parece que o site travou.
 */
export function AvisoServidorLento() {
  const [lento, setLento] = useState(false)
  useEffect(() => {
    const ouvir = (e) => setLento(e.detail > 0)
    window.addEventListener('valtrea:lento', ouvir)
    return () => window.removeEventListener('valtrea:lento', ouvir)
  }, [])
  return (
    <div role="status" aria-live="polite" className="pointer-events-none fixed inset-x-0 bottom-4 z-50 flex justify-center px-4">
      {lento && (
        <p className="max-w-md rounded-xl border border-border bg-surface px-4 py-3 text-sm leading-relaxed text-foreground shadow-xl">
          <strong>Acordando o servidor…</strong> Quando o site fica um tempo sem uso, a primeira ação pode levar até
          1 minuto. Depois fica rápido.
        </p>
      )}
    </div>
  )
}
