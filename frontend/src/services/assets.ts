export interface CourseAsset {
  id: string
  courseId: string
  lessonId: string | null
  filename: string
  mediaType: string
  sizeBytes: number
}

async function check(response: Response): Promise<Response> {
  if(response.ok) return response
  let message = `Falha ao processar arquivo (HTTP ${response.status})`
  try {
    const body = await response.json() as {message?: string}
    if(body.message) message = body.message
  } catch { /* Erro sem JSON */ }
  throw new Error(message)
}
const auth=(token:string)=>({Authorization: `Bearer ${token}`})
export async function uploadCourseAsset(token:string, courseId:string, file:File, lessonId?:string): Promise<CourseAsset> {
  if(file.size===0 || file.size>30*1024*1024) throw new Error('Arquivo vazio ou maior que 30 MB')
  const data = new FormData()
  data.append('file',file)
  const query = lessonId ? `?lessonId=${encodeURIComponent(lessonId)}` : ''
  const result = await check(await fetch(`/api/v1/instructor/courses/${courseId}/assets${query}`,{
    method:'POST',headers:auth(token),body:data,credentials:'omit',
  }))
  return await result.json() as CourseAsset
}
export async function listInstructorAssets(token:string,courseId:string): Promise<CourseAsset[]> {
  const response=await check(await fetch(`/api/v1/instructor/courses/${courseId}/assets`,{headers:auth(token)}))
  return await response.json() as CourseAsset[]
}
export async function listStudentAssets(token:string,slug:string): Promise<CourseAsset[]> {
  const response=await check(await fetch(`/api/v1/students/me/courses/${encodeURIComponent(slug)}/assets`,{headers:auth(token)}))
  return await response.json() as CourseAsset[]
}
export async function fetchStudentAsset(token:string,slug:string,id:string): Promise<Blob> {
  const response=await check(await fetch(`/api/v1/students/me/courses/${encodeURIComponent(slug)}/assets/${id}`,{headers:auth(token),cache:'no-store'}))
  return await response.blob()
}
export function saveBlob(blob:Blob,filename:string) {
  const url=URL.createObjectURL(blob)
  const a=document.createElement('a')
  a.href=url;a.download=filename;a.click()
  window.setTimeout(()=>URL.revokeObjectURL(url),60000)
}
export async function reportVideoWatch(token:string,slug:string,lessonId:string,seconds:number) {
  const response=await check(await fetch(`/api/v1/students/me/courses/${encodeURIComponent(slug)}/lessons/${lessonId}/video-progress`,{
    method:'POST',headers:{...auth(token),'Content-Type':'application/json'},
    body:JSON.stringify({seconds}),credentials:'omit',
  }))
  return await response.json() as {watchedSeconds:number,totalSeconds:number,percent:number,completed:boolean}
}
