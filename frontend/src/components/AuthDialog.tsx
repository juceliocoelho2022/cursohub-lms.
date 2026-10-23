import { useEffect, useId, useState } from 'react'
import type { FormEvent } from 'react'
import { login, registerStudent } from '../services/auth'
import type { AuthSession } from '../services/auth'

export type AuthMode = 'login' | 'register'

interface Props {
  initialMode: AuthMode
  onClose: () => void
  onAuthenticated: (session: AuthSession) => void
}

export function AuthDialog({ initialMode, onClose, onAuthenticated }: Props) {
  const [mode, setMode] = useState<AuthMode>(initialMode)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const headingId = useId()

  useEffect(() => {
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !busy) onClose()
    }
    window.addEventListener('keydown', onKeyDown)
    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', onKeyDown)
    }
  }, [onClose, busy])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (busy) return
    setError('')
    setBusy(true)
    try {
      const session = mode === 'register'
        ? await registerStudent(name, email, password)
        : await login(email, password)
      onAuthenticated(session)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro inesperado')
    } finally {
      setBusy(false)
    }
  }

  const switchMode = () => {
    setMode(mode === 'login' ? 'register' : 'login')
    setError('')
    setPassword('')
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={event => {
      if (event.target === event.currentTarget && !busy) onClose()
    }}>
      <section className="course-modal auth-dialog" role="dialog" aria-modal="true" aria-labelledby={headingId}>
        <button className="close-modal" type="button" disabled={busy} onClick={onClose} aria-label="Fechar">×</button>
        <div className="modal-label">CURSOHUB / CONTA</div>
        <h2 id={headingId}>{mode === 'login' ? 'Bem-vindo de volta' : 'Crie sua conta de aluno'}</h2>
        <p className="modal-description">{mode === 'login'
          ? 'Entre para acessar sua conta.'
          : 'Cadastre-se para iniciar sua jornada de aprendizagem.'}</p>

        <form className="auth-form" onSubmit={submit}>
          {mode === 'register' && <label>Nome completo
            <input autoFocus required minLength={3} maxLength={150} value={name}
                   autoComplete="name" onChange={event => setName(event.target.value)}
                   placeholder="Como você se chama?" />
          </label>}
          <label>E-mail
            <input autoFocus={mode === 'login'} type="email" required maxLength={254}
                   autoComplete="email" value={email} onChange={event => setEmail(event.target.value)}
                   placeholder="seuemail@exemplo.com" />
          </label>
          <label>Senha
            <input type="password" required minLength={mode === 'register' ? 12 : 1}
                   maxLength={mode === 'register' ? 72 : undefined}
                   autoComplete={mode === 'register' ? 'new-password' : 'current-password'}
                   value={password} onChange={event => setPassword(event.target.value)}
                   placeholder={mode === 'register' ? 'Mínimo de 12 caracteres' : 'Sua senha'} />
          </label>
          {mode === 'register' && <p className="auth-note">A senha deve ter entre 12 e 72 bytes (UTF-8). O cadastro público cria exclusivamente uma conta de aluno.</p>}
          {error && <p role="alert" className="inline-error auth-error">{error}</p>}
          <button className="primary-btn full" disabled={busy} type="submit">
            {busy ? 'Processando...' : mode === 'login' ? 'Entrar na plataforma' : 'Criar conta gratuita'}
          </button>
        </form>
        <p className="auth-switch">
          {mode === 'login' ? 'Ainda não tem uma conta?' : 'Já possui cadastro?'}
          {' '}
          <button type="button" onClick={switchMode} disabled={busy}>
            {mode === 'login' ? 'Cadastre-se' : 'Fazer login'}
          </button>
        </p>
      </section>
    </div>
  )
}
