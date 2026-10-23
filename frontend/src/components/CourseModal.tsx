import { useEffect, useState } from 'react'
import type { Course } from '../services/catalog'
import { formatDuration, levels, loadCourse } from '../services/catalog'

interface CourseModalProps {
  course: Course
  onClose: () => void
}

export function CourseModal({ course, onClose }: CourseModalProps) {
  const [details, setDetails] = useState<Course>(course)
  const [error, setError] = useState('')

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
      if (event.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', onEscape)
    return () => {
      controller.abort()
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', onEscape)
    }
  }, [course.slug, onClose])

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={event => {
      if (event.target === event.currentTarget) onClose()
    }}>
      <section className="course-modal" role="dialog" aria-modal="true" aria-labelledby="course-modal-title">
        <button className="close-modal" type="button" onClick={onClose} aria-label="Fechar">×</button>
        <div className="modal-label">CONHEÇA O CURSO</div>
        <h2 id="course-modal-title">{details.title}</h2>
        <p className="modal-description">{details.description}</p>
        {error && <p className="inline-error" role="alert">{error}</p>}
        <div className="modal-facts">
          <span>◈ {details.category}</span>
          <span>◷ {formatDuration(details.estimatedMinutes)}</span>
          <span>◎ {levels[details.level]}</span>
        </div>
        <div className="modal-info">
          <strong>Catálogo público disponível</strong>
          <p>O catálogo é funcional. Matrículas, login e acesso às aulas serão implementados nas próximas etapas.</p>
        </div>
        <button type="button" className="primary-btn full" onClick={onClose}>Voltar ao catálogo</button>
      </section>
    </div>
  )
}
