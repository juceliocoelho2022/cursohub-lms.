/** Preferência visual local, sem dados de autenticação. */
export type CourseTheme = 'light' | 'moderate' | 'black'

const STORAGE_KEY = 'cursohub.course.theme'

export function isCourseTheme(value: string | null): value is CourseTheme {
  return value === 'light' || value === 'moderate' || value === 'black'
}

/** Fallback para claro quando o browser bloqueia armazenamento ou o valor é inválido. */
export function loadCourseTheme(): CourseTheme {
  try {
    const saved = window.localStorage.getItem(STORAGE_KEY)
    return isCourseTheme(saved) ? saved : 'light'
  } catch {
    return 'light'
  }
}

export function saveCourseTheme(theme: CourseTheme): void {
  try {
    window.localStorage.setItem(STORAGE_KEY, theme)
  } catch {
    // Modo privado/bloqueio de storage: a escolha continua ativa na sessão.
  }
}
