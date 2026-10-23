export const MAX_COVER_BYTES = 5 * 1024 * 1024

export function validateCoverFile(file: File): string | null {
  if (!/\.(jpg|jpeg|png|webp)$/i.test(file.name))
    return 'Escolha uma imagem JPG, PNG ou WEBP.'
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type))
    return 'Formato de imagem não reconhecido.'
  if (file.size === 0 || file.size > MAX_COVER_BYTES)
    return 'A imagem deve ter até 5 MB.'
  return null
}

export async function uploadCourseCover(token: string, courseId: string, file: File) {
  const error = validateCoverFile(file)
  if (error) throw new Error(error)
  const data = new FormData()
  data.append('file', file)
  const response = await fetch(`/api/v1/instructor/courses/${courseId}/cover`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: data, credentials: 'omit',
  })
  if (!response.ok) {
    let message = `Não foi possível enviar a capa (HTTP ${response.status}).`
    try { const problem = await response.json() as {message?: string}
      if(problem.message) message = problem.message
    } catch { /* Sem resposta JSON */ }
    throw new Error(message)
  }
  return (await response.json()) as { thumbnailUrl: string }
}

export async function fetchOwnedCover(token: string, courseId: string, signal?: AbortSignal) {
  const response=await fetch(`/api/v1/instructor/courses/${courseId}/cover`,{
    headers:{ Authorization:`Bearer ${token}` },credentials:'omit',signal,cache:'no-store',
  })
  if(response.status===404) return null
  if(!response.ok) throw new Error(`Não foi possível carregar a capa (HTTP ${response.status}).`)
  return response.blob()
}

/** Only same-origin URLs issued by CursoHub, never arbitrary external URLs. */
export function publicCoverUrl(url: string | null | undefined) {
  return url && /^\/api\/v1\/courses\/[a-z0-9]+(?:-[a-z0-9]+)*\/cover$/.test(url) ? url : null
}
