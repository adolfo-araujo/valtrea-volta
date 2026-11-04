import { Link } from 'react-router-dom'
import { chamar } from '../lib/api'
import { useAuth } from '../lib/auth'
import { LEGAL } from '../lib/legal'
import { estilos, useEnvio } from './ui'

/** Aviso de nova versão dos Termos e da Política, com o novo aceite (data e versão). */
export function AvisoTermos() {
  const { perfil, recarregar } = useAuth()
  const { enviando, executar } = useEnvio()
  if (!perfil || perfil.termosAceitos) return null
  return (
    <section aria-labelledby="termos-novos" className="no-print border-b border-primary/40 bg-surface">
      <div className="mx-auto flex max-w-5xl flex-col gap-3 px-4 py-4 sm:flex-row sm:items-center">
        <p id="termos-novos" className="flex-1 text-sm">
          Atualizamos os <Link to="/termos" className={estilos.link}>Termos de uso</Link> e a{' '}
          <Link to="/privacidade" className={estilos.link}>Política de privacidade</Link> ({LEGAL.updatedAt}). Se não
          concordar, você pode excluir a conta em Conta.
        </p>
        <button type="button" className={estilos.primario} disabled={enviando}
          onClick={() => executar(async () => {
            await chamar('POST', '/eu/termos', { versao: LEGAL.version })
            await recarregar()
          })}>
          Li e concordo
        </button>
      </div>
    </section>
  )
}
