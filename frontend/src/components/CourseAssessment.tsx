import {useEffect,useState} from 'react'
import {assessmentApi} from '../services/assessments'
import type {AttemptResult,CourseCertificate,StudentQuiz} from '../services/assessments'

interface Props {token:string;slug:string;courseProgress:number}
export function CourseAssessment({token,slug,courseProgress}:Props) {
  const [quiz,setQuiz]=useState<StudentQuiz|null>(null)
  const [certificate,setCertificate]=useState<CourseCertificate|null>(null)
  const [answers,setAnswers]=useState<Record<string,string>>({})
  const [result,setResult]=useState<AttemptResult|null>(null)
  const [loading,setLoading]=useState(true)
  const [busy,setBusy]=useState(false)
  const [error,setError]=useState('')
  const [notice,setNotice]=useState('')
  useEffect(()=>{
    let active=true
    setLoading(true);setError('');setResult(null);setAnswers({});setCertificate(null)
    Promise.all([assessmentApi.quiz(token,slug),assessmentApi.certificate(token,slug)])
      .then(([q,c])=>{if(active){setQuiz(q);setCertificate(c)}})
      .catch(e=>{if(active)setError(e instanceof Error?e.message:'Falha ao carregar avaliação')})
      .finally(()=>{if(active)setLoading(false)})
    return ()=>{active=false}
  },[token,slug])

  async function submit(){
    if(!quiz||busy)return
    setBusy(true);setError('');setNotice('')
    try{
      const body=quiz.questions.map(q=>({questionId:q.id,optionId:answers[q.id]}))
      if(body.some(x=>!x.optionId))throw new Error('Responda todas as questões antes de enviar.')
      const response=await assessmentApi.submit(token,slug,quiz.id,body)
      setResult(response)
      const updated=await assessmentApi.quiz(token,slug)
      setQuiz(updated)
      setNotice(response.passed?'Avaliação aprovada! Agora você pode emitir seu certificado.':
        response.attemptsRemaining>0?'Nota insuficiente. Você ainda pode tentar novamente.':'Nota insuficiente. Limite de tentativas atingido.')
      setAnswers({})
    }catch(e){setError(e instanceof Error?e.message:'Falha no envio das respostas')}
    finally{setBusy(false)}
  }
  async function issue(){
    if(busy)return
    setBusy(true);setError('');setNotice('')
    try{
      const item=await assessmentApi.issue(token,slug)
      setCertificate(item)
      setNotice('Certificado emitido e registrado. Use Imprimir / Salvar como PDF.')
    }catch(e){setError(e instanceof Error?e.message:'Ainda não é possível emitir o certificado')}
    finally{setBusy(false)}
  }

  const canAttempt=quiz && !quiz.passed && quiz.attemptsUsed<quiz.maxAttempts
  const canIssue=courseProgress===100 && (!quiz||quiz.passed)
  const verifyUrl=certificate ? `${window.location.origin}/api/v1/certificates/verify/${certificate.verificationCode}` : ''

  return <section className="course-assessment" aria-label="Avaliação e certificação do curso">
    <h2>Avaliação e certificado</h2>
    {loading&&<p>Consultando avaliação e certificado...</p>}
    {!loading && !quiz && <p>Este curso não exige avaliação final. Complete as aulas obrigatórias para solicitar seu certificado.</p>}
    {quiz&&<div className="course-quiz">
      <div className="course-quiz-meta">
        <h3>{quiz.title}</h3>
        <p>Nota mínima: {quiz.passingScore}% · Tentativas: {quiz.attemptsUsed}/{quiz.maxAttempts}</p>
      </div>
      {quiz.passed&&<p className="learning-complete-label">✓ Avaliação aprovada</p>}
      {!quiz.passed&&courseProgress<100&&<p>Conclua todas as aulas obrigatórias antes de responder à avaliação.</p>}
      {canAttempt && courseProgress===100&&<form onSubmit={e=>{e.preventDefault();void submit()}}>
        {quiz.questions.map((q,i)=><fieldset className="course-quiz-question" key={q.id}>
          <legend>{i+1}. {q.statement}</legend>
          {q.options.map((o,j)=><label key={o.id} className="course-quiz-option">
            <input type="radio" name={`quiz-${q.id}`} value={o.id} checked={answers[q.id]===o.id}
              onChange={()=>setAnswers(prev=>({...prev,[q.id]:o.id}))}/>
            <span>{String.fromCharCode(65+j)}. {o.text}</span>
          </label>)}
        </fieldset>)}
        <button type="submit" className="primary-btn" disabled={busy||quiz.questions.some(q=>!answers[q.id])}>
          {busy?'Corrigindo...':'Enviar respostas para correção'}
        </button>
      </form>}
      {!canAttempt&&!quiz.passed&&<p>Limite de tentativas atingido. Procure o instrutor para orientação.</p>}
      {result&&<p role="status" className={result.passed?'learning-complete-label':'course-quiz-result'}>
        Sua nota: {result.score}% · {result.passed?'Aprovado':'Não aprovado'}.
      </p>}
    </div>}
    {courseProgress<100&&<p className="course-certificate-hint">O certificado exige a conclusão de todas as aulas obrigatórias.</p>}
    {courseProgress===100&&quiz&&!quiz.passed&&<p className="course-certificate-hint">A aprovação na avaliação final é necessária para emitir o certificado.</p>}
    {canIssue&&!certificate&&<button type="button" className="primary-btn" disabled={busy} onClick={()=>void issue()}>
      {busy?'Verificando requisitos...':'Emitir meu certificado'}
    </button>}
    {certificate&&<div className="course-certificate" id="cursohub-certificado">
      <div className="course-certificate-sheet">
        <div className="course-certificate-brand">✳ CursoHub.</div>
        <h3>CERTIFICADO DE CONCLUSÃO</h3>
        <p>Certificamos que</p>
        <strong>{certificate.studentName}</strong>
        <p>concluiu o curso</p>
        <h4>{certificate.courseTitle}</h4>
        <p>Emissão: {new Date(certificate.issuedAt).toLocaleDateString('pt-BR')}</p>
        <div className="course-certificate-code">Código de verificação: {certificate.verificationCode}</div>
        <p><a href={verifyUrl} target="_blank" rel="noopener noreferrer">Validar autenticidade ↗</a></p>
      </div>
      <button type="button" className="secondary-btn certificate-print-button"
        onClick={()=>window.print()}>Imprimir / Salvar como PDF</button>
    </div>}
    {error&&<p role="alert" className="inline-error">{error}</p>}
    {notice&&<p role="status" className="learning-feedback">{notice}</p>}
  </section>
}
