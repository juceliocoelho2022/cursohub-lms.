import {useEffect,useState} from 'react'
import {assessmentApi} from '../services/assessments'
import type {QuizDraft,StudentQuiz} from '../services/assessments'

interface Props {token:string;courseId:string;editable:boolean}
const makeQuestion=()=>({statement:'',options:[
  {text:'',correct:true},{text:'',correct:false},
  {text:'',correct:false},{text:'',correct:false},
]})
export function InstructorAssessmentEditor({token,courseId,editable}:Props){
  const [current,setCurrent]=useState<StudentQuiz|null>(null)
  const [loading,setLoading]=useState(true)
  const [busy,setBusy]=useState(false)
  const [error,setError]=useState('')
  const [message,setMessage]=useState('')
  const [draft,setDraft]=useState<QuizDraft>({
    title:'Avaliação final',passingScore:70,maxAttempts:3,questions:[makeQuestion()],
  })
  useEffect(()=>{
    let active=true
    setLoading(true);setError('');setCurrent(null)
    assessmentApi.instructor(token,courseId).then(q=>{if(active)setCurrent(q)})
      .catch(e=>{if(active)setError(e instanceof Error?e.message:'Não foi possível consultar a avaliação')})
      .finally(()=>{if(active)setLoading(false)})
    return ()=>{active=false}
  },[token,courseId])
  function editQuestion(index:number,fn:(q:QuizDraft['questions'][number])=>QuizDraft['questions'][number]){
    setDraft(d=>({...d,questions:d.questions.map((q,i)=>i===index?fn(q):q)}))
  }
  async function save(){
    if(busy)return
    setBusy(true);setError('');setMessage('')
    try{
      const q=await assessmentApi.create(token,courseId,draft)
      setCurrent(q);setMessage('Avaliação final cadastrada! Publique o curso quando as aulas estiverem prontas.')
    }catch(e){setError(e instanceof Error?e.message:'Não foi possível salvar a avaliação')}
    finally{setBusy(false)}
  }
  return <section className="assessment-editor course-assets-panel">
    <h3>Avaliação final do curso</h3>
    <p>Questões de múltipla escolha com uma alternativa correta, correção automática e nota mínima. Somente antes da publicação.</p>
    {loading && <p>Consultando avaliação...</p>}
    {current && <div className="assessment-summary">
      <strong>✓ {current.title}</strong>
      <p>{current.questions.length} questões · Aprovação: {current.passingScore}% · {current.maxAttempts} tentativas.</p>
      <p>O gabarito não é enviado aos alunos.</p>
    </div>}
    {!loading && !current && editable && <form className="instructor-form" onSubmit={e=>{e.preventDefault();void save()}}>
      <label>Título da avaliação
        <input value={draft.title} required minLength={5} maxLength={180}
          onChange={e=>setDraft(d=>({...d,title:e.target.value}))}/>
      </label>
      <div className="instructor-input-row">
        <label>Nota mínima (%)
          <input type="number" min={1} max={100} required value={draft.passingScore}
            onChange={e=>setDraft(d=>({...d,passingScore:Number(e.target.value)}))}/>
        </label>
        <label>Limite de tentativas
          <input type="number" min={1} max={10} required value={draft.maxAttempts}
            onChange={e=>setDraft(d=>({...d,maxAttempts:Number(e.target.value)}))}/>
        </label>
      </div>
      {draft.questions.map((q,i)=><fieldset className="assessment-question-edit" key={i}>
        <legend>Questão {i+1}</legend>
        <label>Enunciado
          <textarea rows={2} required minLength={5} maxLength={2000} value={q.statement}
            onChange={e=>editQuestion(i,x=>({...x,statement:e.target.value}))}/>
        </label>
        {q.options.map((opt,j)=><div className="assessment-option-edit" key={j}>
          <input type="radio" name={`answer-${courseId}-${i}`} checked={opt.correct}
            aria-label={`Marcar alternativa ${j+1} como correta`}
            onChange={()=>editQuestion(i,x=>({...x,options:x.options.map((o,k)=>({...o,correct:k===j}))}))}/>
          <input aria-label={`Questão ${i+1} alternativa ${j+1}`} value={opt.text}
            required maxLength={500} placeholder={`Alternativa ${j+1}`}
            onChange={e=>editQuestion(i,x=>({...x,options:x.options.map((o,k)=>k===j?{...o,text:e.target.value}:o)}))}/>
        </div>)}
      </fieldset>)}
      {draft.questions.length<20 && <button type="button" className="secondary-btn"
        onClick={()=>setDraft(d=>({...d,questions:[...d.questions,makeQuestion()]}))}>+ Adicionar questão</button>}
      {draft.questions.length>1 && <button type="button" className="secondary-btn"
        onClick={()=>setDraft(d=>({...d,questions:d.questions.slice(0,-1)}))}>Remover última questão</button>}
      <button type="submit" className="primary-btn" disabled={busy}>{busy?'Salvando...':'Salvar avaliação final'}</button>
    </form>}
    {!loading && !current && !editable && <p>Este curso foi publicado sem avaliação final. Para preservar as regras dos alunos matriculados, apenas novos cursos em rascunho podem receber avaliações nesta versão.</p>}
    {error && <p className="inline-error" role="alert">{error}</p>}
    {message && <p className="learning-feedback" role="status">{message}</p>}
  </section>
}
