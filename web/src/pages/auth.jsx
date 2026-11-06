import { useRef, useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { chamar } from '../lib/api'
import { useAuth } from '../lib/auth'
import { Campo, Mensagem, TelaAuth, estilos, useEnvio } from '../components/ui'

const SENHA_DICA = '8+ caracteres, letras e números'
const senhaFraca = (s) => s.length < 8 || !/\p{L}/u.test(s) || !/\d/.test(s)
const DESTINO = '/objetos'

export function EntrarPage() {
  const { perfil, entrar } = useAuth()
  const navegar = useNavigate()
  const avisoAnterior = useLocation().state?.aviso
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const { enviando, erro, executar } = useEnvio()
  if (perfil) return <Navigate to={DESTINO} replace />

  const enviar = (e) => {
    e.preventDefault()
    executar(async () => {
      const tokens = await chamar('POST', '/auth/login', { email, senha }, { autenticar: false })
      await entrar(tokens)
      navegar(DESTINO)
    })
  }

  return (
    <TelaAuth
      titulo="Entrar"
      subtitulo="Acesse seus objetos e os avisos de quem encontrou."
      abaixo={<>Ainda não tem conta? <Link to="/cadastro" className={estilos.link}>Criar conta</Link></>}
    >
      <form onSubmit={enviar} className="space-y-4" noValidate>
        <Mensagem>{avisoAnterior}</Mensagem>
        <Campo rotulo="E-mail" type="email" autoComplete="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        <Campo rotulo="Senha" type="password" autoComplete="current-password" required value={senha} onChange={(e) => setSenha(e.target.value)} />
        <p className="pt-1 text-center">
          <Link to="/esqueci-senha" className={estilos.link}>Esqueci a senha</Link>
        </p>
        <Mensagem tipo="erro">{erro}</Mensagem>
        <button type="submit" className={`${estilos.primario} h-12 w-full`} disabled={enviando}>
          {enviando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </TelaAuth>
  )
}

export function CadastroPage() {
  const { perfil, entrar } = useAuth()
  const [dados, setDados] = useState({ nome: '', sobrenome: '', email: '', senha: '', confirmacao: '' })
  const [aceite, setAceite] = useState(false)
  const [erros, setErros] = useState({})
  const acabouDeCriar = useRef(false)
  const { enviando, erro, executar } = useEnvio()
  // Quem acabou de criar a conta vai para a confirmação do e-mail
  if (perfil) return <Navigate to={acabouDeCriar.current ? '/confirmar-email' : DESTINO} replace />

  const muda = (campo) => (e) => setDados({ ...dados, [campo]: e.target.value })

  const enviar = (e) => {
    e.preventDefault()
    const novos = {}
    if (!dados.nome.trim()) novos.nome = 'Digite seu nome.'
    if (!dados.sobrenome.trim()) novos.sobrenome = 'Digite seu sobrenome.'
    if (!/^\S+@\S+\.\S+$/.test(dados.email)) novos.email = 'Digite um e-mail válido.'
    if (senhaFraca(dados.senha)) novos.senha = 'A senha precisa ter 8 ou mais caracteres, com letras e números.'
    else if (dados.confirmacao !== dados.senha) novos.confirmacao = 'As senhas não são iguais.'
    if (!aceite) novos.aceite = 'Para criar a conta, marque que leu e aceita os Termos.'
    setErros(novos)
    if (Object.keys(novos).length) return
    executar(async () => {
      const nome = `${dados.nome.trim()} ${dados.sobrenome.trim()}`
      const tokens = await chamar('POST', '/auth/cadastro', { nome, email: dados.email, senha: dados.senha }, { autenticar: false })
      acabouDeCriar.current = true
      await entrar(tokens)
    })
  }

  return (
    <TelaAuth
      titulo="Criar conta"
      subtitulo="Etiquete seus objetos e receba o aviso de quem encontrar."
      abaixo={<>Já tem conta? <Link to="/entrar" className={estilos.link}>Entrar</Link></>}
    >
      <form onSubmit={enviar} className="space-y-4" noValidate>
        <div className="grid grid-cols-2 gap-3">
          <Campo rotulo="Nome" autoComplete="given-name" required value={dados.nome} onChange={muda('nome')} erro={erros.nome} />
          <Campo rotulo="Sobrenome" autoComplete="family-name" required value={dados.sobrenome} onChange={muda('sobrenome')} erro={erros.sobrenome} />
        </div>
        <Campo rotulo="E-mail" type="email" autoComplete="email" required value={dados.email} onChange={muda('email')} erro={erros.email}
          dica="É por ele que avisamos quando alguém encontrar um objeto seu." />
        <Campo rotulo="Senha" type="password" autoComplete="new-password" required placeholder={SENHA_DICA} value={dados.senha}
          onChange={muda('senha')} erro={erros.senha} />
        <Campo rotulo="Confirmar senha" type="password" autoComplete="new-password" required value={dados.confirmacao}
          onChange={muda('confirmacao')} erro={erros.confirmacao} />
        <div>
          <div className="flex items-start gap-2.5 text-sm">
            <input id="aceite" type="checkbox" checked={aceite} onChange={(e) => setAceite(e.target.checked)} className="mt-1 h-4 w-4 accent-[#22C55E]"
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
        <button type="submit" className={`${estilos.primario} h-12 w-full`} disabled={enviando}>
          {enviando ? 'Criando…' : 'Criar conta'}
        </button>
      </form>
    </TelaAuth>
  )
}

export function ConfirmarEmailPage() {
  const { perfil, carregando, recarregar, sair } = useAuth()
  const navegar = useNavigate()
  const [codigo, setCodigo] = useState('')
  const [aviso, setAviso] = useState('')
  const { enviando, erro, executar } = useEnvio()
  if (!carregando && !perfil) return <Navigate to="/entrar" replace />
  if (perfil?.emailConfirmado) return <Navigate to={DESTINO} replace />

  const confirmar = (e) => {
    e.preventDefault()
    executar(async () => {
      await chamar('POST', '/auth/confirmar-email', { codigo })
      await recarregar()
      navegar(DESTINO)
    })
  }

  return (
    <TelaAuth
      titulo="Confirme seu e-mail"
      subtitulo={<>Enviamos um código de 6 números para <strong className="text-foreground">{perfil?.email}</strong>. Confira também a caixa de spam.</>}
      abaixo={<>E-mail errado? <button type="button" className={estilos.link} onClick={async () => { await sair(); navegar('/cadastro') }}>Criar com outro e-mail</button></>}
    >
      <form onSubmit={confirmar} className="space-y-4" noValidate>
        <Campo rotulo="Código de 6 números" inputMode="numeric" autoComplete="one-time-code" maxLength={6} value={codigo}
          onChange={(e) => setCodigo(e.target.value.replace(/\D/g, ''))} />
        <Mensagem tipo="erro">{erro}</Mensagem>
        <Mensagem>{aviso}</Mensagem>
        <button type="submit" className={`${estilos.primario} h-12 w-full`} disabled={enviando || codigo.length !== 6}>
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
    </TelaAuth>
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
    <TelaAuth
      titulo="Esqueci a senha"
      subtitulo={etapa === 'email' ? 'Vamos enviar um código para o seu e-mail.' : 'Se o e-mail tiver conta, enviamos um código. Ele vale por 15 minutos.'}
      abaixo={<>Lembrou? <Link to="/entrar" className={estilos.link}>Entrar</Link></>}
    >
      {etapa === 'email' ? (
        <form onSubmit={pedir} className="space-y-4" noValidate>
          <Campo rotulo="E-mail" type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} />
          <Mensagem tipo="erro">{erro}</Mensagem>
          <button type="submit" className={`${estilos.primario} h-12 w-full`} disabled={enviando}>Enviar código</button>
        </form>
      ) : (
        <form onSubmit={redefinir} className="space-y-4" noValidate>
          <Campo rotulo="Código de 6 números" inputMode="numeric" autoComplete="one-time-code" maxLength={6} value={codigo}
            onChange={(e) => setCodigo(e.target.value.replace(/\D/g, ''))} />
          <Campo rotulo="Nova senha" type="password" autoComplete="new-password" placeholder={SENHA_DICA} value={novaSenha}
            onChange={(e) => setNovaSenha(e.target.value)} />
          <Mensagem tipo="erro">{erro}</Mensagem>
          <button type="submit" className={`${estilos.primario} h-12 w-full`} disabled={enviando || codigo.length !== 6}>
            Salvar nova senha
          </button>
        </form>
      )}
    </TelaAuth>
  )
}
