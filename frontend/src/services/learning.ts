export type LessonKind = 'TEXT' | 'VIDEO' | 'PDF'

export interface LearningLesson {
  id: string
  title: string
  position: number
  kind: LessonKind
  contentUrl: string | null
  bodyText: string | null
  durationSeconds: number
  required: boolean
  completed: boolean
  locked: boolean
}

export interface LearningModule {
  id: string
  title: string
  position: number
  lessons: LearningLesson[]
}

export interface LearningCourse {
  courseId: string
  slug: string
  title: string
  modules: LearningModule[]
  requiredLessons: number
  completedRequiredLessons: number
  progressPercent: number
}

export interface LessonCompletionReceipt {
  lessonId: string
  newlyCompleted: boolean
}

function authorization(token: string): HeadersInit {
  return { Authorization: `Bearer ${token}` }
}

export async function getCourseLessons(slug: string, token: string, signal?: AbortSignal): Promise<LearningCourse> {
  const response = await fetch(`/api/v1/students/me/courses/${encodeURIComponent(slug)}/lessons`, {
    headers: authorization(token),
    credentials: 'omit',
    signal,
  })
  if (response.status === 401) throw new Error('Sua sessão expirou. Entre novamente.')
  if (response.status === 403) throw new Error('Acesso disponível somente para alunos.')
  if (response.status === 404) throw new Error('Este curso não está disponível nas suas matrículas.')
  if (!response.ok) throw new Error('Não foi possível carregar as aulas.')
  return (await response.json()) as LearningCourse
}

export async function completeLesson(slug: string, lessonId: string, token: string): Promise<LessonCompletionReceipt> {
  const response = await fetch(
    `/api/v1/students/me/courses/${encodeURIComponent(slug)}/lessons/${encodeURIComponent(lessonId)}/complete`,
    { method: 'POST', headers: authorization(token), credentials: 'omit' },
  )
  if (response.status === 401) throw new Error('Sua sessão expirou. Entre novamente.')
  if (response.status === 404) throw new Error('A aula não está disponível para esta matrícula.')
  if (!response.ok) throw new Error('Não foi possível registrar a conclusão da aula.')
  return (await response.json()) as LessonCompletionReceipt
}

/** Só permite endereços HTTPS para abrir materiais externos. */
export function safeMaterialUrl(value: string | null): string | null {
  if (!value) return null
  try {
    const url = new URL(value)
    return url.protocol === 'https:' ? url.toString() : null
  } catch {
    return null
  }
}

export function isDirectVideoUrl(value: string): boolean {
  try {
    const url = new URL(value)
    return /\.(mp4|webm)$/i.test(url.pathname)
  } catch {
    return false
  }
}
