export interface EnrollmentReceipt {
  enrollmentId: string
  courseId: string
  courseSlug: string
  status: 'ACTIVE' | 'COMPLETED'
  created: boolean
}

/** O identificador do aluno nunca é enviado: o backend usa o JWT. */
export async function enrollInCourse(slug: string, token: string): Promise<EnrollmentReceipt> {
  const response = await fetch(`/api/v1/courses/${encodeURIComponent(slug)}/enrollments`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    credentials: 'omit',
  })
  if (response.ok) return (await response.json()) as EnrollmentReceipt

  if (response.status === 401) throw new Error('Sua sessão expirou. Entre novamente para realizar a matrícula.')
  if (response.status === 403) throw new Error('Somente contas de alunos podem realizar matrículas.')
  let message = 'Não foi possível realizar a matrícula. Tente novamente.'
  try {
    const body = (await response.json()) as { message?: string }
    if (body.message) message = body.message
  } catch { /* Erros de rede e respostas não JSON recebem mensagem padrão. */ }
  throw new Error(message)
}
