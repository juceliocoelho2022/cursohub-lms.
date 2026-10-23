import { useEffect, useState } from 'react'
import type { AuthSession } from '../services/auth'
import type { Course } from '../services/catalog'
import { formatDuration, levels, loadCourse } from '../services/catalog'
import { enrollInCourse } from '../services/enrollment'

interface CourseModalProps {
  course: Course
  session: AuthSession | null
  onClose: () => void
  onRequestLogin: () => void
  onEnrolled: () => void
}

export function CourseModal({ course, session, onClose, onRequestLogin, onEnrolled }: CourseModalProps) {
  const [details, setDetails] = useState<Course>(course)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    const controller = new AbortController()
    loadCourse(course.slug, controller.signal)
      .then(setDetails)
      .catch(err => {
        if (!controller.signal.aborted) setError(err instanceof Error ? err.message : 'Erro inesperado')
      })

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    const onEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !busy) onClose()
    }
    window.addEventListener('keydown', onEscape)
    return () => {
      controller.abort()
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', onEscape)
    }
  }, [course.slug, onClose, busy])

  async function handleEnrollment() {
    if (!session || session.role !== 'STUDENT' || busy) return
    setBusy(true)
    setError('')
    try {
      await enrollInCourse(details.slug, session.accessToken)
      onEnrolled()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro inesperado')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={event => {
      if (event.target === event.currentTarget && !busy) onClose()
    }}>
      <section className="course-modal" role="dialog" aria-modal="true" aria-labelledby="course-modal-title">
        <button className="close-modal" type="button" onClick={onClose} disabled={busy} aria-label="Fechar">×</button>
        <div className="modal-label">CONHEÇA O CURSO</div>
        <h2 id="course-modal-title">{details.title}</h2>
        <p className="modal-description">{details.description}</p>
        <div className="modal-facts">
          <span>◈ {details.category}</span>
          <span>◷ {formatDuration(details.estimatedMinutes)}</span>
          <span>◎ {levels[details.level]}</span>
        </div>
        <div className="modal-info">
          <strong>Aprenda no seu ritmo</strong>
          <p>Inscreva-se no curso para adicioná-lo ao seu painel. As aulas e a marcação de progresso serão disponibilizadas em uma próxima etapa.</p>
        </div>
        {error && <p className="inline-error" role="alert">{error}</p>}

        {!session ? (
          <button type="button" className="primary-btn full" onClick={onRequestLogin}>Entre para se matricular ↗</button>
        ) : session.role === 'STUDENT' ? (
          <button type="button" className="primary-btn full" onClick={handleEnrollment} disabled={busy || !!error}>
            {busy ? 'Realizando matrícula...' : 'Matricular-se gratuitamente ↗'}
          </button>
        ) : (
          <p className="modal-info">A matrícula está disponível somente para contas de alunos.</p>
        )}
        <button type="button" className="secondary-btn modal-secondary" disabled={busy} onClick={onClose}>Voltar ao catálogo</button>
      </section>
    </div>
  )
}
