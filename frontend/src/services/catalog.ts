export type CourseLevel = 'INICIANTE' | 'INTERMEDIARIO' | 'AVANCADO'

export interface Course {
  id: string
  slug: string
  title: string
  description: string
  category: string
  level: CourseLevel
  estimatedMinutes: number
  thumbnailUrl: string | null
}

export interface CatalogPage {
  content: Course[]
  totalElements: number
  page: number
  size: number
  totalPages: number
}

export interface CatalogFilters {
  search: string
  category: string
  level: string
  page: number
  size: number
}

export async function loadCourses(filters: CatalogFilters, signal: AbortSignal): Promise<CatalogPage> {
  const params = new URLSearchParams({
    q: filters.search,
    page: String(filters.page),
    size: String(filters.size),
  })
  if (filters.category) params.set('categoria', filters.category)
  if (filters.level) params.set('nivel', filters.level)

  const response = await fetch(`/api/v1/courses?${params}`, { signal })
  if (!response.ok) throw new Error('Não foi possível carregar o catálogo.')
  return response.json() as Promise<CatalogPage>
}

export async function loadCourse(slug: string, signal: AbortSignal): Promise<Course> {
  const response = await fetch(`/api/v1/courses/${encodeURIComponent(slug)}`, { signal })
  if (!response.ok) throw new Error('Não foi possível carregar os detalhes do curso.')
  return response.json() as Promise<Course>
}

export function formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const extraMinutes = minutes % 60
  if (hours && extraMinutes) return `${hours}h ${extraMinutes}min`
  if (hours) return `${hours}h`
  return `${minutes}min`
}

export const levels: Record<CourseLevel, string> = {
  INICIANTE: 'Iniciante',
  INTERMEDIARIO: 'Intermediário',
  AVANCADO: 'Avançado',
}
