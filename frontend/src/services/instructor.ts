import type { CourseLevel } from './catalog'
import type { LessonKind } from './learning'

export type InstructorStatus = 'RASCUNHO' | 'PUBLICADO' | 'ARQUIVADO'

export interface InstructorCourse {
  id: string
  slug: string
  title: string
  description: string
  category: string
  level: CourseLevel
  status: InstructorStatus
  estimatedMinutes: number
  priceCents: number
}

export interface InstructorLesson {
  id: string
  moduleId: string
  title: string
  position: number
  kind: LessonKind
  contentUrl: string | null
  bodyText: string | null
  durationSeconds: number
  required: boolean
}

export interface InstructorModule {
  id: string
  title: string
  position: number
  lessons: InstructorLesson[]
}

export interface InstructorCourseContent {
  course: InstructorCourse
  modules: InstructorModule[]
}

export interface CourseDraftForm {
  slug: string
  title: string
  description: string
  category: string
  level: CourseLevel
  estimatedMinutes: number
}

export interface LessonForm {
  title: string
  kind: LessonKind
  contentUrl: string | null
  bodyText: string | null
  durationSeconds: number
  required: boolean
}

async function request<T>(url: string, token: string, method: 'GET' | 'POST' = 'GET', body?: unknown): Promise<T> {
  const response = await fetch(url, {
    method,
    headers: {
      Authorization: `Bearer ${token}`,
      ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
    credentials: 'omit',
  })
  if (!response.ok) {
    if (response.status === 401) throw new Error('Sessão expirada. Entre novamente.')
    if (response.status === 403) throw new Error('Acesso restrito ao instrutor.')
    let message = `Erro HTTP ${response.status} ao salvar/consultar dados.`
    try {
      const problem = await response.json() as { message?: string }
      if (problem.message) message = problem.message
    } catch { /* Resposta sem JSON. */ }
    throw new Error(message)
  }
  return await response.json() as T
}

const base = '/api/v1/instructor/courses'
export const instructorApi = {
  list(token: string) {
    return request<InstructorCourse[]>(base, token)
  },
  content(token: string, courseId: string) {
    return request<InstructorCourseContent>(`${base}/${courseId}/content`, token)
  },
  create(token: string, course: CourseDraftForm) {
    return request<InstructorCourse>(base, token, 'POST', course)
  },
  module(token: string, courseId: string, title: string) {
    return request<InstructorModule>(`${base}/${courseId}/modules`, token, 'POST', { title })
  },
  lesson(token: string, courseId: string, moduleId: string, data: LessonForm) {
    return request<InstructorLesson>(`${base}/${courseId}/modules/${moduleId}/lessons`, token, 'POST', data)
  },
  publish(token: string, courseId: string) {
    return request<InstructorCourse>(`${base}/${courseId}/publish`, token, 'POST')
  },
}
