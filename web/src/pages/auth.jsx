import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { chamar } from '../lib/api'
import { useAuth } from '../lib/auth'
import { Campo, Mensagem, Pagina, estilos, useEnvio } from '../components/ui'

const SENHA_DICA = '8 ou mais caracteres, com letras e números.'
const senhaFraca = (s) => s.length < 8 || !/\p{L}/u.test(s) || !/\d/.test(s)

export function EntrarPage() {
  const { perfil, entrar } = useAuth()
  const navegar = useNavigate()
  const avisoAnterior = useLocation().state?.aviso
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const { enviando, erro, executar } = useEnvio()
  if (perfil) return <Navigate to="/objetos" replace />

  const enviar = (e) => {
    e.preventDefault()
    executar(async () => {
      const tokens = await chamar('POST', '/auth/login', { email, senha }, { autenticar: false })
      await entrar(tokens)
      navegar('/objetos')
    })
  }

  return (
    <Pagina titulo="Entrar" largura="max-w-md">
      <form onSubmit={enviar} className="space-y-4" noValidate>
        <Mensagem>{avisoAnterior}</Mensagem>
        <Campo rotulo="E-mail" type="email" autoComplete="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        <Campo rotulo="Senha" type="password" autoComplete="current-password" required value={senha} onChange={(e) => setSenha(e.target.value)} />
        <Mensagem tipo="erro">{erro}</Mensagem>
        <button type="submit" className={`${estilos.primario} w-full`} disabled={enviando}>
          {enviando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
      <div className="mt-6 flex flex-col gap-2 text-sm">
        <Link to="/esqueci-senha" className={estilos.link}>Esqueci minha senha</Link>
        <p className="text-muted">
          Ainda não tem conta? <Link to="/cadastro" className={estilos.link}>Criar conta</Link>
        </p>
      </div>
    </Pagina>
  )
}

export function CadastroPage() {
  const { perfil, entrar } = useAuth()
  const navegar = useNavigate()
  const [dados, setDados] = useState({ nome: '', email: '', senha: '' })
  const [aceite, setAceite] = useState(false)
  const [erros, setErros] = useState({})
  const { enviando, erro, executar } = useEnvio()
  if (perfil) return <Navigate to="/objetos" replace />

  const muda = (campo) => (e) => setDados({ ...dados, [campo]: e.target.value })

  const enviar = (e) => {
    e.preventDefault()
    const novos = {}
    if (!dados.nome.trim()) novos.nome = 'Digite seu nome.'
    if (!/^\S+@\S+\.\S+$/.test(dados.email)) novos.email = 'Digite um e-mail válido.'
    if (senhaFraca(dados.senha)) novos.senha = 'A senha precisa ter 8 ou mais caracteres, com letras e números.'
    if (!aceite) novos.aceite = 'Para criar a conta, marque que leu e aceita os Termos.'
    setErros(novos)
    if (Object.keys(novos).length) return
    executar(async () => {
      const tokens = await chamar('POST', '/auth/cadastro', dados, { autenticar: false })
      await entrar(tokens)
      navegar('/confirmar-email')
    })
  }

  return (
    <Pagina titulo="Criar conta" largura="max-w-md">
      <form onSubmit={enviar} className="space-y-4" noValidate>
        <Campo rotulo="Nome" autoComplete="name" required value={dados.nome} onChange={muda('nome')} erro={erros.nome} />
        <Campo rotulo="E-mail" type="email" autoComplete="email" required value={dados.email} onChange={muda('email')} erro={erros.email}
          dica="É por ele que avisamos quando alguém encontrar um objeto seu." />
        <Campo rotulo="Senha" type="password" autoComplete="new-password" required value={dados.senha} onChange={muda('senha')} erro={erros.senha} dica={SENHA_DICA} />
        <div>
          <div className="flex items-start gap-2.5 text-sm">
            <input id="aceite" type="checkbox" checked={aceite} onChange={(e) => setAceite(e.target.checked)} className="mt-1 h-4 w-4 accent-[#55B02E]"
              aria-describedby={erros.aceite ? 'aceite-erro' : undefined} />
            <label htmlFor="aceite" className="leading-relaxed text-muted">
              Tenho 18 anos ou mais e li e aceito os{' '}
              <Link to="/termos" target="_blank" className={estilos.link}>Termos de uso</Link> e a{' '}
              <Link to="/privacidade" target="_blank" className={estilos.link}>Política de privacidade</Link>.
            </label>
          </div>
          {erros.aceite && <p id="aceite-erro" className="mt-1 text-sm text-danger">{erros.aceite}</p>}
        </div>
        <Mensagem tipo="erro">{erro}</Mensagem>
        <button type="submit" className={`${estilos.primario} w-full`} disabled={enviando}>
          {enviando ? 'Criando…' : 'Criar conta'}
        </button>
      </form>
      <p className="mt-6 text-sm text-muted">
        Já tem conta? <Link to="/entrar" className={estilos.link}>Entrar</Link>
      </p>
    </Pagina>
  )
}

export function ConfirmarEmailPage() {
  const { perfil, carregando, recarregar } = useAuth()
  const navegar = useNavigate()
  const [codigo, setCodigo] = useState('')
  const [aviso, setAviso] = useState('')
  const { enviando, erro, executar } = useEnvio()
  if (!carregando && !perfil) return <Navigate to="/entrar" replace />
  if (perfil?.emailConfirmado) return <Navigate to="/objetos" replace />

  const confirmar = (e) => {
    e.preventDefault()
    executar(async () => {
      await chamar('POST', '/auth/confirmar-email', { codigo })
      await recarregar()
      navegar('/objetos')
    })
  }

  return (
    <Pagina titulo="Confirme seu e-mail" largura="max-w-md">
      <p className="mb-6 text-muted">
        Enviamos um código de 6 números para <strong className="text-foreground">{perfil?.email}</strong>. Confira
        também a caixa de spam.
      </p>
      <form onSubmit={confirmar} className="space-y-4" noValidate>
        <Campo rotulo="Código de 6 números" inputMode="numeric" autoComplete="one-time-code" maxLength={6} value={codigo}
          onChange={(e) => setCodigo(e.target.value.replace(/\D/g, ''))} />
        <Mensagem tipo="erro">{erro}</Mensagem>
        <Mensagem>{aviso}</Mensagem>
        <button type="submit" className={`${estilos.primario} w-full`} disabled={enviando || codigo.length !== 6}>
          Confirmar
        </button>
      </form>
      <button
        type="button"
        className={`${estilos.secundario} mt-3 w-full`}
        disabled={enviando}
        onClick={() => executar(async () => {
          await chamar('POST', '/auth/reenviar-confirmacao')
          setAviso('Enviamos outro código.')
        })}
      >
        Enviar outro código
      </button>
    </Pagina>
  )
}

export function EsqueciSenhaPage() {
  const navegar = useNavigate()
  const [etapa, setEtapa] = useState('email')
  const [email, setEmail] = useState('')
  const [codigo, setCodigo] = useState('')
  const [novaSenha, setNovaSenha] = useState('')
  const { enviando, erro, setErro, executar } = useEnvio()

  const pedir = (e) => {
    e.preventDefault()
    executar(async () => {
      await chamar('POST', '/auth/esqueci-senha', { email }, { autenticar: false })
      setEtapa('codigo')
    })
  }

  const redefinir = (e) => {
    e.preventDefault()
    if (senhaFraca(novaSenha)) return setErro('A senha precisa ter 8 ou mais caracteres, com letras e números.')
    executar(async () => {
      await chamar('POST', '/auth/redefinir-senha', { email, codigo, novaSenha }, { autenticar: false })
      navegar('/entrar', { state: { aviso: 'Senha alterada. Entre com a senha nova.' } })
    })
  }

  return (
    <Pagina titulo="Esqueci minha senha" largura="max-w-md">
      {etapa === 'email' ? (
        <form onSubmit={pedir} className="space-y-4" noValidate>
          <p className="text-muted">Vamos enviar um código para o seu e-mail.</p>
          <Campo rotulo="E-mail" type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} />
          <Mensagem tipo="erro">{erro}</Mensagem>
          <button type="submit" className={`${estilos.primario} w-full`} disabled={enviando}>Enviar código</button>
        </form>
      ) : (
        <form onSubmit={redefinir} className="space-y-4" noValidate>
          <Mensagem>Se o e-mail tiver conta, enviamos um código. Ele vale por 15 minutos.</Mensagem>
          <Campo rotulo="Código de 6 números" inputMode="numeric" autoComplete="one-time-code" maxLength={6} value={codigo}
            onChange={(e) => setCodigo(e.target.value.replace(/\D/g, ''))} />
          <Campo rotulo="Nova senha" type="password" autoComplete="new-password" value={novaSenha}
            onChange={(e) => setNovaSenha(e.target.value)} dica={SENHA_DICA} />
          <Mensagem tipo="erro">{erro}</Mensagem>
          <button type="submit" className={`${estilos.primario} w-full`} disabled={enviando || codigo.length !== 6}>
            Salvar nova senha
          </button>
        </form>
      )}
    </Pagina>
  )
}
