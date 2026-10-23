export interface QuizOption {id:string;text:string}
export interface QuizQuestion {id:string;statement:string;options:QuizOption[]}
export interface StudentQuiz {
  id:string; title:string; passingScore:number; maxAttempts:number;
  attemptsUsed:number; passed:boolean; questions:QuizQuestion[]
}
export interface QuestionDraft {statement:string;options:{text:string;correct:boolean}[]}
export interface QuizDraft {title:string;passingScore:number;maxAttempts:number;questions:QuestionDraft[]}
export interface AttemptResult {id:string;number:number;score:number;passed:boolean;attemptsRemaining:number}
export interface CourseCertificate {id:string;verificationCode:string;studentName:string;courseTitle:string;issuedAt:string}
async function api<T>(url:string,token:string,method='GET',body?:unknown):Promise<T> {
  const r=await fetch(url,{method,credentials:'omit',headers:{
    Authorization:`Bearer ${token}`,...(body===undefined?{}:{'Content-Type':'application/json'})
  },body:body===undefined?undefined:JSON.stringify(body)})
  if(!r.ok){
    let message=`Erro HTTP ${r.status}`
    try{const data=await r.json() as {message?:string};message=data.message??message}catch{/* empty */}
    throw new Error(message)
  }
  return await r.json() as T
}
const student=(slug:string)=>`/api/v1/students/me/courses/${encodeURIComponent(slug)}`
export const assessmentApi={
  instructor(token:string,courseId:string){
    return api<StudentQuiz|null>(`/api/v1/instructor/courses/${courseId}/assessment`,token)
  },
  create(token:string,courseId:string,draft:QuizDraft){
    return api<StudentQuiz>(`/api/v1/instructor/courses/${courseId}/assessment`,token,'POST',draft)
  },
  quiz(token:string,slug:string){
    return api<StudentQuiz|null>(`${student(slug)}/assessment`,token)
  },
  submit(token:string,slug:string,id:string,answers:{questionId:string;optionId:string}[]){
    return api<AttemptResult>(`${student(slug)}/assessment/${id}/attempts`,token,'POST',{answers})
  },
  certificate(token:string,slug:string){
    return api<CourseCertificate|null>(`${student(slug)}/certificate`,token)
  },
  issue(token:string,slug:string){
    return api<CourseCertificate>(`${student(slug)}/certificate`,token,'POST')
  },
}
