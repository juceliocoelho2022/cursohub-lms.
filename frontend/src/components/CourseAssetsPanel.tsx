import {useEffect,useState} from 'react'
import type {CourseAsset} from '../services/assets'
import {listInstructorAssets,uploadCourseAsset} from '../services/assets'
import type {InstructorModule} from '../services/instructor'

interface Props { token:string; courseId:string; modules:InstructorModule[]; }
export function CourseAssetsPanel({token,courseId,modules}:Props) {
  const [items,setItems]=useState<CourseAsset[]>([])
  const [file,setFile]=useState<File|null>(null)
  const [videoFile,setVideoFile]=useState<File|null>(null)
  const [videoLesson,setVideoLesson]=useState('')
  const [busy,setBusy]=useState(false)
  const [error,setError]=useState('')
  const [message,setMessage]=useState('')
  useEffect(()=>{
    let active=true
    setItems([]);setError('');setFile(null);setVideoFile(null)
    listInstructorAssets(token,courseId).then(rows=>{if(active)setItems(rows)})
      .catch(e=>{if(active)setError(e instanceof Error?e.message:'Falha ao carregar anexos')})
    return ()=>{active=false}
  },[token,courseId])
  const videos=modules.flatMap(m=>m.lessons).filter(l=>l.kind==='VIDEO')
  async function send(selected:File|null,lessonId?:string) {
    if(!selected || busy) return
    setBusy(true);setError('');setMessage('')
    try{
      await uploadCourseAsset(token,courseId,selected,lessonId)
      setItems(await listInstructorAssets(token,courseId))
      setFile(null);setVideoFile(null)
      setMessage(lessonId?'Vídeo associado à aula com sucesso.':'Arquivo disponibilizado aos alunos matriculados.')
    }catch(e){setError(e instanceof Error?e.message:'Falha ao enviar arquivo')}
    finally{setBusy(false)}
  }
  return <section className="course-assets-panel">
    <h3>Materiais do curso</h3>
    <p>Adicione PDF, imagens e ZIP. O acesso é restrito aos alunos matriculados. Limite: 30 MB por arquivo.</p>
    <div className="course-upload-row">
      <input type="file" aria-label="Escolher material PDF, imagem ou ZIP"
        accept=".pdf,.png,.jpg,.jpeg,.webp,.zip"
        onChange={e=>setFile(e.target.files?.[0]??null)}/>
      <button type="button" className="secondary-btn" disabled={!file||busy} onClick={()=>void send(file)}>
        {busy?'Enviando...':'Enviar material ↑'}
      </button>
    </div>
    {items.filter(i=>!i.lessonId).length>0 && <ul className="course-asset-list">
      {items.filter(i=>!i.lessonId).map(a=><li key={a.id}>▤ {a.filename} <small>{(a.sizeBytes/1024/1024).toFixed(1)} MB</small></li>)}
    </ul>}
    <div className="instructor-rule"/>
    <h3>Vídeo para uma aula</h3>
    <p>Cadastre uma aula do tipo Vídeo, informe sua duração real em segundos e envie um MP4 ou WEBM. O aluno deverá assistir até 100% para avançar.</p>
    {videos.length===0?<p>Crie primeiro uma aula do tipo Vídeo.</p>:<>
      <label className="asset-select-label">Aula de destino
        <select value={videoLesson} onChange={e=>setVideoLesson(e.target.value)}>
          <option value="">Selecione uma videoaula</option>
          {videos.map(l=><option key={l.id} value={l.id}>{l.title}</option>)}
        </select>
      </label>
      <div className="course-upload-row">
        <input type="file" aria-label="Escolher vídeo MP4 ou WEBM" accept=".mp4,.webm,video/mp4,video/webm"
          onChange={e=>setVideoFile(e.target.files?.[0]??null)}/>
        <button type="button" className="secondary-btn" disabled={!videoLesson||!videoFile||busy}
          onClick={()=>void send(videoFile,videoLesson)}>Enviar vídeo ↑</button>
      </div>
      {items.filter(i=>i.lessonId).map(a=><p className="course-linked-video" key={a.id}>
        ✓ Vídeo anexado: {a.filename} — {videos.find(v=>v.id===a.lessonId)?.title??'Aula'}
      </p>)}
    </>}
    {error&&<p className="inline-error" role="alert">{error}</p>}
    {message&&<p className="learning-feedback" role="status">{message}</p>}
  </section>
}
