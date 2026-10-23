import { useCallback, useEffect, useRef, useState } from 'react'
import { CourseCard } from './components/CourseCard'
import { CourseModal } from './components/CourseModal'
import { MembershipPlans } from './components/MembershipPlans'
import { CourseThemeSelector } from './components/CourseThemeSelector'
import { loadCourseTheme, saveCourseTheme } from './services/courseTheme'
import type { CourseTheme } from './services/courseTheme'
import { AuthDialog } from './components/AuthDialog'
import { StudentDashboard } from './components/StudentDashboard'
import { InstructorStudio } from './components/InstructorStudio'
import { LearningRoom } from './components/LearningRoom'
import type { AuthMode } from './components/AuthDialog'
import { currentUser } from './services/auth'
import type { AuthSession, CurrentUser } from './services/auth'
import type { CatalogPage, Course } from './services/catalog'
import { loadCourses } from './services/catalog'
import './styles.css'

const categories = ['Programação', 'Dados', 'Matemática']
const PAGE_SIZE = 6

export default function App() {
  const [courseTheme, setCourseTheme] = useState<CourseTheme>(loadCourseTheme)
  const [search, setSearch] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [category, setCategory] = useState('')
  const [level, setLevel] = useState('')
  const [page, setPage] = useState(0)
  const [catalog, setCatalog] = useState<CatalogPage | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [retry, setRetry] = useState(0)
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null)
  const coursesSection = useRef<HTMLElement | null>(null)
  const [authMode, setAuthMode] = useState<AuthMode | null>(null)
  const [session, setSession] = useState<AuthSession | null>(null)
  const [currentProfile, setCurrentProfile] = useState<CurrentUser | null>(null)
  const [view, setView] = useState<'catalog' | 'dashboard' | 'learning' | 'instructor'>('catalog')
  const [activeLearningSlug, setActiveLearningSlug] = useState<string | null>(null)
  const [scrollTarget, setScrollTarget] = useState<'courses' | 'about' | 'plans' | null>(null)

  useEffect(() => saveCourseTheme(courseTheme), [courseTheme])

  useEffect(() => {
    const handle = window.setTimeout(() => setDebouncedSearch(search.trim()), 300)
    return () => window.clearTimeout(handle)
  }, [search])

  useEffect(() => setPage(0), [debouncedSearch, category, level])

  useEffect(() => {
    const controller = new AbortController()
    setLoading(true)
    setError('')
    loadCourses({ search: debouncedSearch, category, level, page, size: PAGE_SIZE }, controller.signal)
      .then(setCatalog)
      .catch(err => {
        if (!controller.signal.aborted) setError(err instanceof Error ? err.message : 'Erro inesperado')
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [debouncedSearch, category, level, page, retry])

  const scrollToCourses = useCallback(() => {
    setView('catalog')
    setScrollTarget('courses')
  }, [])
  const scrollToPlans = useCallback(() => {
    setView('catalog')
    setScrollTarget('plans')
  }, [])
  const scrollToAbout = useCallback(() => {
    setView('catalog')
    setScrollTarget('about')
  }, [])
  useEffect(() => {
    if (view !== 'catalog' || !scrollTarget) return
    const destination = scrollTarget === 'courses' ? coursesSection.current : document.getElementById(scrollTarget === 'plans'?'planos':'sobre')
    destination?.scrollIntoView({ behavior: 'smooth' })
    setScrollTarget(null)
  }, [view, scrollTarget])
  const closeModal = useCallback(() => setSelectedCourse(null), [])
  const resetFilters = () => { setSearch(''); setCategory(''); setLevel(''); setPage(0) }
  const closeAuth = useCallback(() => setAuthMode(null), [])
  const onAuthenticated = useCallback((authenticated: AuthSession) => {
    setSession(authenticated)
    setAuthMode(null)
    if (authenticated.role === 'STUDENT') setView('dashboard')
    if (authenticated.role === 'INSTRUCTOR') setView('instructor')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }, [])
  const logout = useCallback(() => { setSession(null); setCurrentProfile(null); setView('catalog') }, [])

  useEffect(() => {
    if (!session) { setCurrentProfile(null); return }
    let active = true
    const expires = window.setTimeout(() => {
      if (active) { setSession(null); setCurrentProfile(null) }
    }, session.expiresIn * 1000)
    currentUser(session.accessToken)
      .then(user => { if (active) setCurrentProfile(user) })
      .catch(() => { if (active) logout() })
    return () => { active = false; window.clearTimeout(expires) }
  }, [session, logout])

  return (
    <div className="app-shell" data-course-theme={courseTheme}>
      <header className="site-header">
        <div className="container nav-row">
          <a className="brand" href="#inicio" aria-label="CursoHub — início" onClick={() => setView("catalog")}>
            <span className="brand-icon" aria-hidden="true">✳</span> Curso<span>Hub</span><b>.</b>
          </a>
          <nav aria-label="Navegação principal">
            <a href="#cursos" onClick={event => { event.preventDefault(); scrollToCourses() }}>Explorar cursos</a>
            <a href="#planos" onClick={event => { event.preventDefault(); scrollToPlans() }}>Planos e benefícios</a>
            <a href="#sobre" onClick={event => { event.preventDefault(); scrollToAbout() }}>Sobre a plataforma</a>
          </nav>
          <div className="nav-account">
            {session ? (
              <>
                <span className="account-greeting">Olá, {(currentProfile?.name || session.name).split(' ')[0]}!</span>
                {session.role === 'STUDENT' && (
                  <button className="nav-cta" type="button" onClick={() => { setView('dashboard'); window.scrollTo({ top: 0, behavior: 'smooth' }) }}>
                    Meu painel
                  </button>
                )}
                {session.role === 'INSTRUCTOR' && (
                  <button className="nav-cta" type="button"
                          onClick={() => { setView('instructor'); window.scrollTo({ top: 0, behavior: 'smooth' }) }}>
                    Estúdio do Instrutor
                  </button>
                )}
                <button className="nav-cta" type="button" onClick={logout}>Sair</button>
              </>
            ) : (
              <>
                <button className="nav-cta" type="button" onClick={() => setAuthMode('login')}>Entrar</button>
                <button className="nav-signup" type="button" onClick={() => setAuthMode('register')}>Criar conta ↗</button>
              </>
            )}
          </div>
        </div>
      </header>

      {view === 'instructor' && session?.role === 'INSTRUCTOR'
        ? <InstructorStudio
            token={session.accessToken}
            name={currentProfile?.name || session.name}
            theme={courseTheme}
            onThemeChange={setCourseTheme}
          />
        : view === 'learning' && session?.role === 'STUDENT' && activeLearningSlug
        ? <LearningRoom
            slug={activeLearningSlug}
            token={session.accessToken}
            theme={courseTheme}
            onThemeChange={setCourseTheme}
            onBack={() => { setView('dashboard'); window.scrollTo({ top: 0, behavior: 'smooth' }) }}
          />
        : view === 'dashboard' && session?.role === 'STUDENT'
        ? <StudentDashboard
            token={session.accessToken}
            name={currentProfile?.name || session.name}
            onExplore={scrollToCourses}
            onLogout={logout}
            onOpenCourse={slug => {
              setActiveLearningSlug(slug)
              setView('learning')
              window.scrollTo({ top: 0, behavior: 'smooth' })
            }}
            theme={courseTheme}
            onThemeChange={setCourseTheme}
          />
        : <main id="inicio">
        <section className="hero">
          <div className="container hero-grid">
            <div className="hero-copy">
              <div className="eyebrow"><span className="eyebrow-dot" /> APRENDA NO SEU RITMO</div>
              <h1>Conhecimento que <em>transforma</em> seu próximo passo.</h1>
              <p>Explore cursos de tecnologia, dados e matemática em uma experiência de aprendizagem feita para evoluir com você.</p>
              <div className="hero-actions">
                <button className="primary-btn" type="button" onClick={scrollToCourses}>Explorar cursos <span>↗</span></button>
                <span className="hero-note">Novas possibilidades começam aqui</span>
              </div>
              <div className="hero-proof">
                <div className="proof-avatars" aria-hidden="true"><span>J</span><span>P</span><span>Σ</span></div>
                <span><b>Aprendizagem prática</b> de um jeito acessível</span>
              </div>
            </div>
            <div className="hero-art" aria-hidden="true">
              <div className="art-halo art-halo-1" />
              <div className="art-halo art-halo-2" />
              <div className="art-top-chip">✦&nbsp; Sua jornada de aprendizado</div>
              <div className="art-main-card">
                <span className="art-card-kicker">CURSOHUB / LEARNING</span>
                <div className="art-code-icon">{`{ }`}</div>
                <div className="art-card-line" />
                <div className="art-card-line short" />
                <div className="art-bottom-text">LEARN · BUILD · GROW</div>
              </div>
              <div className="art-badge"><span>↗</span><strong>Vá além</strong><small>Um curso de cada vez</small></div>
              <div className="art-sparkle">✳</div>
            </div>
          </div>
        </section>

        <section className="catalog-section" id="cursos" ref={coursesSection}>
          <div className="container">
            <div className="section-topline"><span className="mini-rule" /> CATÁLOGO DE CURSOS</div>
            <div className="section-header">
              <div><h2>Seu próximo <em>grande passo</em> começa aqui.</h2>
                <p>Encontre conteúdos alinhados com seus objetivos e interesses.</p>
              </div>
              <span className="course-total">{catalog ? `${catalog.totalElements} curso${catalog.totalElements === 1 ? '' : 's'} disponível${catalog.totalElements === 1 ? '' : 'eis'}` : 'Carregando catálogo...'}</span>
            </div>

            <div className="catalog-theme-row">
              <CourseThemeSelector value={courseTheme} onChange={setCourseTheme} />
            </div>

            <div className="filters">
              <label className="search-field">
                <span className="search-icon" aria-hidden="true">⌕</span>
                <span className="sr-only">Pesquisar cursos</span>
                <input value={search} onChange={event => setSearch(event.target.value)}
                       placeholder="Qual conhecimento você procura?" maxLength={120} />
              </label>
              <label className="filter-select">
                <span className="sr-only">Categoria</span>
                <select value={category} onChange={event => setCategory(event.target.value)}>
                  <option value="">Todas as categorias</option>
                  {categories.map(item => <option key={item} value={item}>{item}</option>)}
                </select>
              </label>
              <label className="filter-select level-select">
                <span className="sr-only">Nível</span>
                <select value={level} onChange={event => setLevel(event.target.value)}>
                  <option value="">Todos os níveis</option>
                  <option value="INICIANTE">Iniciante</option>
                  <option value="INTERMEDIARIO">Intermediário</option>
                  <option value="AVANCADO">Avançado</option>
                </select>
              </label>
            </div>

            {error ? (
              <div className="state-box" role="alert"><div className="state-icon">!</div><h3>Não conseguimos carregar os cursos</h3><p>{error} Confirme se a API está executando.</p><button className="secondary-btn" onClick={() => setRetry(n => n + 1)}>Tentar novamente</button></div>
            ) : loading ? (
              <div className="state-box" role="status"><div className="spinner" /><p>Buscando os melhores caminhos de aprendizagem...</p></div>
            ) : !catalog || catalog.content.length === 0 ? (
              <div className="state-box"><div className="state-icon">⌕</div><h3>Nenhum curso encontrado</h3><p>Tente ajustar sua busca ou limpar os filtros.</p><button className="secondary-btn" onClick={resetFilters}>Limpar filtros</button></div>
            ) : (
              <>
                <div className="course-grid">
                  {catalog.content.map(course => <CourseCard key={course.id} course={course} onView={setSelectedCourse} />)}
                </div>
                {catalog.totalPages > 1 && <div className="pagination">
                  <button className="secondary-btn" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Anterior</button>
                  <span>Página {page + 1} de {catalog.totalPages}</span>
                  <button className="secondary-btn" disabled={page + 1 >= catalog.totalPages} onClick={() => setPage(p => p + 1)}>Próxima →</button>
                </div>}
              </>
            )}
          </div>
        </section>

        <MembershipPlans onExplore={scrollToCourses}/>

        <section className="about-section" id="sobre">
          <div className="container about-row"><div><div className="section-topline light"><span className="mini-rule" /> FEITO PARA EVOLUIR</div><h2>Aprender é construir <em>novos caminhos.</em></h2></div><p>O CursoHub conecta catálogo, autenticação e matrículas ao banco de dados. Aulas, avaliações e certificados serão disponibilizados nas próximas etapas.</p></div>
        </section>
      </main>}
      <footer><div className="container footer-row"><span className="brand">✳ Curso<span>Hub</span><b>.</b></span><span>© {new Date().getFullYear()} CursoHub · Versão inicial de desenvolvimento</span></div></footer>
      {selectedCourse && <CourseModal
        key={selectedCourse.slug}
        course={selectedCourse}
        session={session}
        theme={courseTheme}
        onThemeChange={setCourseTheme}
        onClose={closeModal}
        onRequestLogin={() => { setSelectedCourse(null); setAuthMode('login') }}
        onEnrolled={() => {
          setSelectedCourse(null)
          setView('dashboard')
          window.scrollTo({ top: 0, behavior: 'smooth' })
        }}
      />}
      {authMode && <AuthDialog initialMode={authMode} onClose={closeAuth} onAuthenticated={onAuthenticated} />}
    </div>
  )
}
