import { useEffect, useState } from 'react'
import { CourseThemeSelector } from './CourseThemeSelector'
import type { CourseTheme } from '../services/courseTheme'
import type { DashboardData } from '../services/dashboard'
import { getStudentDashboard } from '../services/dashboard'

interface Props {
  token: string
  name: string
  onExplore: () => void
  onOpenCourse: (slug: string) => void
  onLogout: () => void
  theme: CourseTheme
  onThemeChange: (theme: CourseTheme) => void
}

export function StudentDashboard({ token, name, onExplore, onOpenCourse, onLogout, theme, onThemeChange }: Props) {
  const [dashboard, setDashboard] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    setLoading(true)
    setError('')
    getStudentDashboard(token, controller.signal)
      .then(setDashboard)
      .catch(err => {
        if (!controller.signal.aborted) setError(err instanceof Error ? err.message : 'Erro inesperado')
      })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [token])

  const nextCourse = dashboard?.courses.find(course => course.status === 'ACTIVE' && course.requiredLessons > 0 && course.progressPercent < 100)

  return (
    <main className="dashboard-main" id="student-dashboard">
      <div className="container">
        <div className="dashboard-breadcrumb">CURSOHUB <span> / </span> ÁREA DO ALUNO</div>
        <div className="dashboard-heading">
          <div>
            <div className="eyebrow dashboard-eyebrow"><span className="eyebrow-dot" /> SUA JORNADA</div>
            <h1>Olá, {name.split(' ')[0]}<span className="dashboard-sparkle">✳</span></h1>
            <p>Acompanhe seus cursos e continue aprendendo no seu ritmo.</p>
          </div>
          <div className="dashboard-actions">
            <CourseThemeSelector value={theme} onChange={onThemeChange} />
            <button className="primary-btn" type="button" onClick={onExplore}>Explorar cursos ↗</button>
          </div>
        </div>

        {loading ? (
          <div className="dashboard-state" role="status"><div className="spinner" /><p>Carregando suas matrículas...</p></div>
        ) : error ? (
          <div className="dashboard-state" role="alert">
            <div className="dashboard-empty-icon">!</div>
            <h2>Não conseguimos carregar seu painel</h2>
            <p>{error}</p>
            <button type="button" className="secondary-btn" onClick={onLogout}>Sair e entrar novamente</button>
          </div>
        ) : dashboard && (
          <>
            <div className="dashboard-stats">
              <div className="dashboard-stat">
                <span className="stat-icon">▦</span>
                <span className="stat-value">{dashboard.enrolledCourses}</span>
                <span className="stat-label">Cursos matriculados</span>
              </div>
              <div className="dashboard-stat">
                <span className="stat-icon">◷</span>
                <span className="stat-value">{dashboard.inProgressCourses}</span>
                <span className="stat-label">Em andamento</span>
              </div>
              <div className="dashboard-stat">
                <span className="stat-icon">✳</span>
                <span className="stat-value">{dashboard.completedCourses}</span>
                <span className="stat-label">Concluídos</span>
              </div>
            </div>

            {nextCourse && (
              <section className="dashboard-continue" aria-labelledby="continue-course-heading">
                <div>
                  <span className="dashboard-continue-eyebrow">CONTINUE SUA JORNADA</span>
                  <h2 id="continue-course-heading">{nextCourse.title}</h2>
                  <p>{nextCourse.completedLessons} de {nextCourse.requiredLessons} aulas obrigatórias concluídas · {nextCourse.progressPercent}% de progresso</p>
                  <div className="dashboard-progress-track" role="progressbar"
                    aria-label={`Progresso de ${nextCourse.title}`}
                    aria-valuemin={0} aria-valuemax={100} aria-valuenow={nextCourse.progressPercent}>
                    <div style={{ width: `${nextCourse.progressPercent}%` }} />
                  </div>
                </div>
                <button type="button" className="primary-btn" onClick={() => onOpenCourse(nextCourse.slug)}>
                  {nextCourse.progressPercent === 0 ? 'Começar a estudar ↗' : 'Continuar estudando ↗'}
                </button>
              </section>
            )}

            <div className="dashboard-section-header">
              <div>
                <div className="section-topline"><span className="mini-rule" /> MEU APRENDIZADO</div>
                <h2>Meus cursos</h2>
              </div>
            </div>
            {dashboard.courses.length === 0 ? (
              <div className="dashboard-empty">
                <div className="dashboard-empty-icon" aria-hidden="true">✦</div>
                <h3>Sua jornada começa aqui.</h3>
                <p>Você ainda não possui matrículas ativas ou concluídas. Explore os cursos disponíveis e encontre seu próximo desafio.</p>
                <button className="primary-btn" type="button" onClick={onExplore}>Conhecer o catálogo ↗</button>
                <span>Você já pode se matricular nos cursos disponíveis no catálogo.</span>
              </div>
            ) : (
              <div className="dashboard-course-grid">
                {dashboard.courses.map(course => (
                  <article className="dashboard-course" key={course.enrollmentId}>
                    <div className="dashboard-course-top">
                      <span className="dashboard-course-category">{course.category}</span>
                      <span className={`dashboard-course-status ${course.status === 'COMPLETED' ? 'is-completed' : ''}`}>
                        {course.status === 'COMPLETED' ? 'Concluído' : 'Em andamento'}
                      </span>
                    </div>
                    <h3>{course.title}</h3>
                    <div className="dashboard-progress-info">
                      <span>Progresso das aulas obrigatórias</span>
                      <strong>{course.progressPercent}%</strong>
                    </div>
                    <div className="dashboard-progress-track" role="progressbar"
                         aria-valuenow={course.progressPercent} aria-valuemin={0} aria-valuemax={100}
                         aria-label={`Progresso de ${course.title}`}>
                      <div style={{ width: `${course.progressPercent}%` }} />
                    </div>
                    <p className="dashboard-lessons">
                      {course.requiredLessons === 0
                        ? 'Ainda não há aulas obrigatórias cadastradas.'
                        : `${course.completedLessons} de ${course.requiredLessons} aulas concluídas`}
                    </p>
                    <button className="secondary-btn learning-open-btn" type="button"
                            onClick={() => onOpenCourse(course.slug)}>
                      Acessar aulas ↗
                    </button>
                  </article>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </main>
  )
}
