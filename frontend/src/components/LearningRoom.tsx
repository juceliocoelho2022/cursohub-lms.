import { useEffect, useMemo, useState } from 'react'
import type { CourseTheme } from '../services/courseTheme'
import { CourseThemeSelector } from './CourseThemeSelector'
import { ProtectedVideoPlayer } from './ProtectedVideoPlayer'
import { CourseDownloadMaterials } from './CourseDownloadMaterials'
import { CourseAssessment } from './CourseAssessment'
import { listStudentAssets } from '../services/assets'
import type { CourseAsset } from '../services/assets'
import { MarkdownLessonContent } from './MarkdownLessonContent'
import { completeLesson, getCourseLessons, safeMaterialUrl } from '../services/learning'
import type { LearningCourse, LearningLesson } from '../services/learning'

interface Props {
  slug: string
  token: string
  theme: CourseTheme
  onThemeChange: (value: CourseTheme) => void
  onBack: () => void
}

export function LearningRoom({ slug, token, theme, onThemeChange, onBack }: Props) {
  const [course, setCourse] = useState<LearningCourse | null>(null)
  const [assets,setAssets] = useState<CourseAsset[]>([])
  const [assetsError,setAssetsError] = useState('')
  const [activeId, setActiveId] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')
  const [feedback, setFeedback] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    setLoading(true)
    setError('')
    setAssetsError('')
    setAssets([])
    setCourse(null)
    getCourseLessons(slug, token, controller.signal)
      .then(data => {
        setCourse(data)
        setActiveId(id =>
          id && data.modules.some(module => module.lessons.some(lesson => lesson.id === id))
            ? id : data.modules.flatMap(module => module.lessons)[0]?.id ?? null
        )
      })
      .catch(err => { if (!controller.signal.aborted) setError(err instanceof Error ? err.message : 'Erro inesperado') })
      .finally(() => { if (!controller.signal.aborted) setLoading(false) })
    // Materiais complementares não devem impedir a leitura da aula principal.
    listStudentAssets(token, slug, controller.signal)
      .then(data => { if (!controller.signal.aborted) setAssets(data) })
      .catch(err => { if (!controller.signal.aborted) setAssetsError(
        err instanceof Error ? err.message : 'Materiais temporariamente indisponíveis') })
    return () => controller.abort()
  }, [slug, token])

  const activeLesson = useMemo<LearningLesson | null>(() =>
    course?.modules.flatMap(module => module.lessons).find(lesson => lesson.id === activeId) ?? null,
    [course, activeId],
  )

  async function markAsCompleted() {
    if (!activeLesson || activeLesson.completed || saving) return
    setSaving(true)
    setActionError('')
    setFeedback('')
    try {
      const result = await completeLesson(slug, activeLesson.id, token)
      // Recarregamos o servidor: a UI mostra o progresso persistido, não um valor inventado.
      const updated = await getCourseLessons(slug, token)
      setCourse(updated)
      setFeedback(result.newlyCompleted ? 'Aula concluída e registrada no seu progresso.' : 'Esta aula já estava concluída.')
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Não foi possível salvar sua conclusão.')
    } finally {
      setSaving(false)
    }
  }

  const material = safeMaterialUrl(activeLesson?.contentUrl ?? null)

  return (
    <main className="learning-main">
      <div className="container">
        <div className="learning-top">
          <button className="secondary-btn" type="button" onClick={onBack}>← Voltar ao meu painel</button>
          <CourseThemeSelector value={theme} onChange={onThemeChange} compact />
        </div>
        {loading && <div className="dashboard-state" role="status"><div className="spinner" /><p>Carregando aulas do curso...</p></div>}
        {error && !loading && <div className="dashboard-state" role="alert"><h2>Não foi possível abrir este curso</h2><p>{error}</p><button type="button" className="secondary-btn" onClick={onBack}>Voltar ao painel</button></div>}
        {course && !loading && !error && (
          <>
            <div className="learning-header">
              <span className="section-topline"><span className="mini-rule" /> AMBIENTE DE APRENDIZAGEM</span>
              <h1>{course.title}</h1>
              <div className="learning-header-progress">
                <span>{course.completedRequiredLessons} de {course.requiredLessons} aulas obrigatórias concluídas</span>
                <strong>{course.progressPercent}%</strong>
              </div>
              <div className="dashboard-progress-track" role="progressbar"
                   aria-valuemin={0} aria-valuemax={100} aria-valuenow={course.progressPercent}
                   aria-label="Progresso nas aulas obrigatórias">
                <div style={{ width: `${course.progressPercent}%` }}/>
              </div>
            </div>
            {assetsError && <div className="learning-assets-notice" role="alert">
              Materiais complementares indisponíveis: {assetsError}. As aulas continuam acessíveis.
            </div>}
            <CourseDownloadMaterials token={token} slug={slug} assets={assets} />
            <div className="learning-layout">
              <aside className="learning-sidebar" aria-label="Módulos e aulas do curso">
                <h2>Conteúdo do curso</h2>
                {course.modules.length === 0 && <p>Nenhum módulo cadastrado até o momento.</p>}
                {course.modules.map(module => (
                  <section className="learning-module" key={module.id}>
                    <h3>{module.position}. {module.title}</h3>
                    {module.lessons.length === 0 && <p className="learning-empty-module">Ainda sem aulas.</p>}
                    {module.lessons.map(lesson => (
                      <button type="button" key={lesson.id}
                              aria-current={lesson.id === activeId ? 'true' : undefined}
                              disabled={lesson.locked}
                              title={lesson.locked ? 'Conclua a aula anterior para desbloquear' : lesson.title}
                              className={`learning-lesson-nav ${lesson.id === activeId ? 'is-active' : ''}`}
                              onClick={() => { setActiveId(lesson.id); setActionError(''); setFeedback('') }}>
                        <span aria-hidden="true">{lesson.locked ? '🔒' : lesson.completed ? '✓' : lesson.kind === 'VIDEO' ? '▶' : lesson.kind === 'PDF' ? '▤' : '◉'}</span>
                        <span>{lesson.title}</span>
                        {lesson.completed && <span className="sr-only">Concluída</span>}
                      </button>
                    ))}
                  </section>
                ))}
              </aside>
              <section className="learning-content" aria-label="Conteúdo da aula selecionada">
                {!activeLesson ? (
                  <div className="learning-no-lesson"><h2>Aulas em preparação</h2><p>O instrutor ainda não disponibilizou conteúdo neste curso.</p></div>
                ) : (
                  <>
                    {activeLesson.locked ? <div className="learning-no-lesson" role="status"><h2>Aula bloqueada</h2><p>Conclua a aula obrigatória anterior para avançar.</p></div> : <>
                    <div className="learning-lesson-eyebrow">
                      {activeLesson.kind === 'TEXT' ? 'AULA DE LEITURA' : activeLesson.kind === 'VIDEO' ? 'VIDEOAULA' : 'MATERIAL PDF'}
                      {' · '}
                      {activeLesson.required ? 'OBRIGATÓRIA' : 'COMPLEMENTAR'}
                    </div>
                    <h2>{activeLesson.title}</h2>
                    {activeLesson.kind === 'TEXT' && (
                      <div className="learning-reading">
                        <MarkdownLessonContent content={activeLesson.bodyText?.trim() || 'O conteúdo de texto desta aula ainda não foi disponibilizado.'} />
                      </div>
                    )}
                    {activeLesson.kind === 'VIDEO' && (
                      <div className="learning-resource">
                        {assets.find(item=>item.lessonId===activeLesson.id) ? (
                          <ProtectedVideoPlayer
                            key={activeLesson.id}
                            token={token} slug={slug} lessonId={activeLesson.id}
                            assetId={assets.find(item=>item.lessonId===activeLesson.id)!.id}
                            onCompleted={async ()=>{setCourse(await getCourseLessons(slug,token))}}
                          />
                        ) : <p>O instrutor precisa enviar o MP4/WEBM pelo Estúdio para habilitar a videoaula controlada.</p>}
                      </div>
                    )}
                    {activeLesson.kind === 'PDF' && (
                      <div className="learning-resource">
                        {material
                          ? <a href={material} target="_blank" rel="noopener noreferrer" className="learning-resource-link">Abrir material PDF ↗</a>
                          : <p>O material PDF ainda não está disponível. Consulte o instrutor.</p>}
                      </div>
                    )}
                    <div className="learning-finish">
                      {activeLesson.kind === 'VIDEO' ? (
                        <p>A conclusão da videoaula é automática após 100% de exibição validada.</p>
                      ) : activeLesson.completed
                        ? <span className="learning-complete-label">✓ Aula concluída</span>
                        : <button type="button" className="primary-btn" onClick={markAsCompleted} disabled={saving}>
                            {saving ? 'Salvando conclusão...' : 'Marcar aula como concluída ✓'}
                          </button>}
                      <p>O progresso é registrado quando você confirma a conclusão da aula.</p>
                      {feedback && <p className="learning-feedback" role="status">{feedback}</p>}
                      {actionError && <p className="inline-error" role="alert">{actionError}</p>}
                    </div>
                    </>}
                  </>
                )}
              </section>
            </div>
            <CourseAssessment token={token} slug={slug} courseProgress={course.progressPercent}/>
          </>
        )}
      </div>
    </main>
  )
}
