import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './lib/auth'
import { AvisoTermos } from './components/aviso-termos'
import { Carregando, Pagina } from './components/ui'
import { InicioPage } from './pages/inicio'
import { CadastroPage, ConfirmarEmailPage, EntrarPage, EsqueciSenhaPage } from './pages/auth'
import { ObjetoPage, ObjetosPage } from './pages/objetos'
import { FolhaEtiquetasPage } from './pages/folha'
import { AvisosPage } from './pages/avisos'
import { ContaPage } from './pages/conta'
import { PublicoPage } from './pages/publico'
import { AcessibilidadePage, ExcluirContaPage, PrivacidadePage, TermosPage } from './pages/legal'
import { AjudaPage } from './pages/ajuda'
import { NovidadesPage } from './pages/novidades'
import { BuscaRapida } from './components/busca-rapida'

// Telas da busca rápida (Ctrl+K): rótulo, endereço, palavras para achar
const TELAS = [
  ["Início", "/", "home"],
  ["Meus objetos", "/objetos", "etiquetas qr"],
  ["Imprimir etiquetas em folha A4", "/etiquetas/imprimir", "imprimir folha"],
  ["Avisos de quem encontrou", "/avisos", "mensagens achados"],
  ["Minha conta", "/conta", "perfil senha baixar dados excluir"],
  ["Entrar", "/entrar", "login"],
  ["Criar conta", "/cadastro", "cadastro"],
  ["Central de ajuda", "/ajuda", "duvidas perguntas suporte"],
  ["Novidades", "/novidades", "mudancas versao"],
  ["Termos de uso", "/termos", "contrato"],
  ["Política de privacidade", "/privacidade", "lgpd dados"],
  ["Acessibilidade", "/acessibilidade", ""],
  ["Excluir conta (como fazer)", "/excluir-conta", "apagar"],
]

function Protegida({ children }) {
  const { perfil, carregando } = useAuth()
  if (carregando) return <Pagina><Carregando /></Pagina>
  if (!perfil) return <Navigate to="/entrar" replace />
  return children
}

export default function App() {
  return (
    <>
      <BuscaRapida itens={TELAS} />
      <AvisoTermos />
      <Routes>
        <Route path="/" element={<InicioPage />} />
        <Route path="/entrar" element={<EntrarPage />} />
        <Route path="/cadastro" element={<CadastroPage />} />
        <Route path="/esqueci-senha" element={<EsqueciSenhaPage />} />
        <Route path="/confirmar-email" element={<ConfirmarEmailPage />} />
        <Route path="/v/:codigo" element={<PublicoPage />} />
        <Route path="/objetos" element={<Protegida><ObjetosPage /></Protegida>} />
        <Route path="/etiquetas/imprimir" element={<Protegida><FolhaEtiquetasPage /></Protegida>} />
        <Route path="/objetos/:id" element={<Protegida><ObjetoPage /></Protegida>} />
        <Route path="/avisos" element={<Protegida><AvisosPage /></Protegida>} />
        <Route path="/conta" element={<Protegida><ContaPage /></Protegida>} />
        <Route path="/novidades" element={<NovidadesPage />} />
        <Route path="/ajuda" element={<AjudaPage />} />
        <Route path="/termos" element={<TermosPage />} />
        <Route path="/privacidade" element={<PrivacidadePage />} />
        <Route path="/acessibilidade" element={<AcessibilidadePage />} />
        <Route path="/excluir-conta" element={<ExcluirContaPage />} />
        <Route path="*" element={<Pagina titulo="Página não encontrada"><p className="text-muted">Confira o endereço.</p></Pagina>} />
      </Routes>
    </>
  )
}
