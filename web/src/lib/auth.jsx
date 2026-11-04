import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { chamar, estaLogado, sessao, tokenDeSessao } from './api'

const Contexto = createContext(null)

export function AuthProvider({ children }) {
  const [perfil, setPerfil] = useState(null)
  const [carregando, setCarregando] = useState(estaLogado())

  const recarregar = useCallback(async () => {
    if (!estaLogado()) {
      setPerfil(null)
      setCarregando(false)
      return null
    }
    try {
      const p = await chamar('GET', '/eu')
      setPerfil(p)
      return p
    } catch {
      setPerfil(null)
      return null
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    recarregar()
    const sair = () => setPerfil(null)
    window.addEventListener('volta:saiu', sair)
    return () => window.removeEventListener('volta:saiu', sair)
  }, [recarregar])

  const entrar = async (tokens) => {
    sessao.salvar(tokens)
    return recarregar()
  }

  const sair = async () => {
    const refreshToken = tokenDeSessao()
    if (refreshToken) await chamar('POST', '/auth/sair', { refreshToken }, { autenticar: false }).catch(() => {})
    sessao.limpar()
    setPerfil(null)
  }

  return (
    <Contexto.Provider value={{ perfil, carregando, entrar, sair, recarregar }}>{children}</Contexto.Provider>
  )
}

export const useAuth = () => useContext(Contexto)
