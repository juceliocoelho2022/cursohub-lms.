import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { CourseThemeSelector } from './CourseThemeSelector'
import type { CourseTheme } from '../services/courseTheme'
import { instructorApi } from '../services/instructor'
import type { CourseDraftForm, InstructorCourse, InstructorCourseContent, LessonForm } from '../services/instructor'

interface Props {
  token: string
  name: string
  theme: CourseTheme
  onThemeChange: (theme: CourseTheme) => void
}

const emptyDraft: CourseDraftForm = {
  slug: '', title: '', description: '', category: 'Programação',
  level: 'INICIANTE', estimatedMinutes: 180,
}
const emptyLesson: LessonForm = {
  title: '', kind: 'TEXT', contentUrl: null, bodyText: '',
  durationSeconds: 600, required: true,
}

export function InstructorStudio({ token, name, theme, onThemeChange }: Props) {
  const [courses, setCourses] = useState<InstructorCourse[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [content, setContent] = useState<InstructorCourseContent | null>(null)
  const [draft, setDraft] = useState<CourseDraftForm>(emptyDraft)
  const [moduleTitle, setModuleTitle] = useState('')
  const [moduleId, setModuleId] = useState('')
  const [lesson, setLesson] = useState<LessonForm>(emptyLesson)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [loading, setLoading] = useState(true)

  async function refresh(courseId?: string | null) {
    const result = await instructorApi.list(token)
    setCourses(result)
    const target = courseId === undefined ? selectedId : courseId
    if (target) {
      const data = await instructorApi.content(token, target)
      setContent(data)
      setSelectedId(target)
      setModuleId(old => data.modules.some(m => m.id === old) ? old : data.modules[0]?.id ?? '')
    } else {
      setContent(null)
    }
  }

  useEffect(() => {
    let active = true
    instructorApi.list(token)
      .then(items => { if (active) setCourses(items) })
      .catch(err => { if (active) setError(err instanceof Error ? err.message : 'Falha ao carregar cursos') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [token])

  async function act(operation: () => Promise<void>) {
    if (busy) return
    setBusy(true)
    setError('')
    setNotice('')
    try { await operation() } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro inesperado')
    } finally { setBusy(false) }
  }

  function createCourse(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void act(async () => {
      const course = await instructorApi.create(token, draft)
      setDraft(emptyDraft)
      await refresh(course.id)
      setNotice('Curso criado como rascunho. Agora adicione módulos e aulas.')
    })
  }

  function createModule(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedId) return
    void act(async () => {
      await instructorApi.module(token, selectedId, moduleTitle)
      setModuleTitle('')
      await refresh(selectedId)
      setNotice('Módulo adicionado com sucesso.')
    })
  }

  function createLesson(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedId || !moduleId) return
    void act(async () => {
      await instructorApi.lesson(token, selectedId, moduleId, lesson)
      setLesson(emptyLesson)
      await refresh(selectedId)
      setNotice('Aula salva no PostgreSQL.')
    })
  }

  function publishCourse() {
    if (!selectedId) return
    void act(async () => {
      await instructorApi.publish(token, selectedId)
      await refresh(selectedId)
      setNotice('Curso publicado. Agora está disponível no catálogo.')
    })
  }

  const editable = content?.course.status === 'RASCUNHO'

  return (
    <main className="instructor-main">
      <div className="container">
        <div className="instructor-top">
          <div>
            <div className="section-topline"><span className="mini-rule"/> ESTÚDIO DO INSTRUTOR</div>
            <h1>Olá, {name.split(' ')[0]}!</h1>
            <p>Crie conteúdos, organize aulas e publique seus cursos.</p>
          </div>
          <CourseThemeSelector value={theme} onChange={onThemeChange} />
        </div>

        {error && <p className="instructor-message instructor-error" role="alert">{error}</p>}
        {notice && <p className="instructor-message instructor-success" role="status">{notice}</p>}

        <div className="instructor-grid">
          <section className="instructor-panel">
            <h2>Meus cursos</h2>
            {loading && <p>Carregando seus cursos...</p>}
            {!loading && courses.length === 0 && <p>Você ainda não criou cursos.</p>}
            <div className="instructor-course-list">
              {courses.map(item => (
                <button key={item.id} type="button"
                  aria-pressed={item.id === selectedId}
                  className={`instructor-course-item ${selectedId === item.id ? 'is-current' : ''}`}
                  onClick={() => void act(async () => { await refresh(item.id) })}>
                  <strong>{item.title}</strong>
                  <span>{item.status === 'RASCUNHO' ? 'Rascunho' : item.status === 'PUBLICADO' ? 'Publicado' : 'Arquivado'}</span>
                </button>
              ))}
            </div>
            <div className="instructor-rule" />
            <h3>Criar curso</h3>
            <form className="instructor-form" onSubmit={createCourse}>
              <label>Nome do curso
                <input required minLength={5} maxLength={180} value={draft.title}
                       onChange={e => setDraft(v => ({ ...v, title: e.target.value }))}
                       placeholder="Ex.: Java do Zero ao Backend" />
              </label>
              <label>Identificador (slug)
                <input required maxLength={160} pattern="[a-z0-9]+(-[a-z0-9]+)*"
                       value={draft.slug}
                       onChange={e => setDraft(v => ({ ...v, slug: e.target.value }))}
                       placeholder="java-do-zero" />
              </label>
              <label>Descrição
                <textarea required minLength={10} maxLength={20000} rows={3}
                          value={draft.description}
                          onChange={e => setDraft(v => ({ ...v, description: e.target.value }))}/>
              </label>
              <div className="instructor-input-row">
                <label>Categoria
                  <input required minLength={2} maxLength={60} value={draft.category}
                         onChange={e => setDraft(v => ({ ...v, category: e.target.value }))}/>
                </label>
                <label>Nível
                  <select value={draft.level} onChange={e => setDraft(v => ({
                    ...v, level: e.target.value as CourseDraftForm['level'],
                  }))}>
                    <option value="INICIANTE">Iniciante</option>
                    <option value="INTERMEDIARIO">Intermediário</option>
                    <option value="AVANCADO">Avançado</option>
                  </select>
                </label>
              </div>
              <label>Duração estimada (minutos)
                <input type="number" min={0} max={100000} required value={draft.estimatedMinutes}
                       onChange={e => setDraft(v => ({ ...v, estimatedMinutes: Number(e.target.value) }))}/>
              </label>
              <button className="primary-btn" disabled={busy} type="submit">Criar rascunho +</button>
            </form>
          </section>

          <section className="instructor-panel instructor-editor">
            {!content ? (
              <div className="instructor-empty">
                <div className="dashboard-empty-icon">✦</div>
                <h2>Selecione ou crie um curso</h2>
                <p>Depois organize os módulos e acrescente aulas de texto, vídeo ou PDF.</p>
              </div>
            ) : (
              <>
                <div className="instructor-course-heading">
                  <div>
                    <span className="instructor-kicker">EDITOR DE CURSO</span>
                    <h2>{content.course.title}</h2>
                    <p>{content.course.status === 'RASCUNHO'
                      ? 'Rascunho — apenas você tem acesso ao editor.'
                      : 'Publicado — conteúdo em modo somente leitura.'}</p>
                  </div>
                  {editable && <button type="button" className="primary-btn"
                        disabled={busy} onClick={publishCourse}>Publicar curso ↗</button>}
                </div>
                {editable && (
                  <>
                    <form className="instructor-inline-form" onSubmit={createModule}>
                      <label>Adicionar módulo
                        <input required minLength={3} maxLength={180}
                               placeholder="Ex.: Fundamentos da programação"
                               value={moduleTitle} onChange={e => setModuleTitle(e.target.value)}/>
                      </label>
                      <button type="submit" className="secondary-btn" disabled={busy}>Adicionar +</button>
                    </form>
                    {content.modules.length > 0 && <form className="instructor-form instructor-lesson-form"
                                                        onSubmit={createLesson}>
                      <h3>Cadastrar aula</h3>
                      <label>Módulo
                        <select required value={moduleId} onChange={e => setModuleId(e.target.value)}>
                          {content.modules.map(m => <option key={m.id} value={m.id}>{m.position}. {m.title}</option>)}
                        </select>
                      </label>
                      <label>Título da aula
                        <input required minLength={3} maxLength={180} value={lesson.title}
                               onChange={e => setLesson(v => ({ ...v, title: e.target.value }))}/>
                      </label>
                      <div className="instructor-input-row">
                        <label>Tipo
                          <select value={lesson.kind} onChange={e => setLesson(v => ({
                            ...v, kind: e.target.value as LessonForm['kind'],
                          }))}>
                            <option value="TEXT">Texto</option>
                            <option value="VIDEO">Vídeo</option>
                            <option value="PDF">PDF</option>
                          </select>
                        </label>
                        <label>Duração (segundos)
                          <input type="number" min={0} max={720000} required value={lesson.durationSeconds}
                                 onChange={e => setLesson(v => ({ ...v, durationSeconds: Number(e.target.value) }))}/>
                        </label>
                      </div>
                      {lesson.kind === 'TEXT'
                        ? <label>Texto da aula (Markdown)
                            <textarea required minLength={5} maxLength={100000} rows={8}
                                      placeholder="## Objetivo da aula — use **negrito**, listas e blocos de código com três crases."
                                      value={lesson.bodyText ?? ''}
                                      onChange={e => setLesson(v => ({ ...v, bodyText: e.target.value }))}/>
                            <span className="instructor-markdown-hint">
                              Dica: use # para título, ## para subtítulo, **texto** para negrito
                              e três crases seguidas de java para destacar código.
                              O aluno verá o conteúdo formatado.
                            </span>
                          </label>
                        : <label>URL HTTPS do material
                            <input type="url" required placeholder="https://..."
                                   value={lesson.contentUrl ?? ''}
                                   onChange={e => setLesson(v => ({ ...v, contentUrl: e.target.value }))}/>
                          </label>}
                      <label className="instructor-checkbox">
                        <input type="checkbox" checked={lesson.required}
                               onChange={e => setLesson(v => ({ ...v, required: e.target.checked }))}/>
                        Aula obrigatória para progresso
                      </label>
                      <button className="primary-btn" disabled={busy} type="submit">Salvar aula +</button>
                    </form>}
                  </>
                )}
                <div className="instructor-content-outline">
                  <h3>Estrutura do curso</h3>
                  {content.modules.length === 0 && <p>Adicione o primeiro módulo para começar.</p>}
                  {content.modules.map(module => (
                    <div className="instructor-outline-module" key={module.id}>
                      <strong>{module.position}. {module.title}</strong>
                      {module.lessons.length === 0 && <p>Sem aulas cadastradas.</p>}
                      {module.lessons.map(item => (
                        <div className="instructor-outline-lesson" key={item.id}>
                          <span>{item.kind === 'TEXT' ? '◉' : item.kind === 'VIDEO' ? '▶' : '▤'}</span>
                          <span>{item.title}</span>
                          <small>{item.required ? 'Obrigatória' : 'Extra'}</small>
                        </div>
                      ))}
                    </div>
                  ))}
                </div>
              </>
            )}
          </section>
        </div>
      </div>
    </main>
  )
}
