export type EnrollmentStatus = 'ACTIVE' | 'COMPLETED'

export interface CourseOverview {
  enrollmentId: string
  courseId: string
  slug: string
  title: string
  category: string
  status: EnrollmentStatus
  requiredLessons: number
  completedLessons: number
  progressPercent: number
  enrolledAt: string
}

export interface DashboardData {
  enrolledCourses: number
  inProgressCourses: number
  completedCourses: number
  courses: CourseOverview[]
}

export async function getStudentDashboard(token: string, signal: AbortSignal): Promise<DashboardData> {
  const response = await fetch('/api/v1/students/me/dashboard', {
    headers: { Authorization: `Bearer ${token}` },
    credentials: 'omit',
    signal,
  })
  if (response.status === 401 || response.status === 403) {
    throw new Error('Sessão expirada ou acesso não autorizado. Entre novamente.')
  }
  if (!response.ok) {
    throw new Error('Não foi possível carregar seu painel. Tente novamente.')
  }
  return (await response.json()) as DashboardData
}
