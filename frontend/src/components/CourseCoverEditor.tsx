import {useEffect,useState} from 'react'
import {CourseCoverPicker} from './CourseCoverPicker'
import {fetchOwnedCover,uploadCourseCover} from '../services/courseCover'

interface Props {token:string;courseId:string}
export function CourseCoverEditor({token,courseId}:Props) {
  const [file,setFile]=useState<File|null>(null)
  const [current,setCurrent]=useState('')
  const [busy,setBusy]=useState(false)
  const [message,setMessage]=useState('')
  const [error,setError]=useState('')

  async function load(signal?:AbortSignal){
    const blob=await fetchOwnedCover(token,courseId,signal)
    if(signal?.aborted) return null
    if(!blob){setCurrent('');return null}
    const url=URL.createObjectURL(blob)
    setCurrent(url)
    return url
  }
  useEffect(()=>{
    const controller=new AbortController()
    let objectUrl=''
    setFile(null);setMessage('');setError('');setCurrent('')
    void load(controller.signal).then(url=>{objectUrl=url??''})
      .catch(err=>{if(!controller.signal.aborted)setError(err instanceof Error?err.message:'Falha ao carregar a imagem')})
    return ()=>{controller.abort();if(objectUrl)URL.revokeObjectURL(objectUrl)}
  },[token,courseId])

  async function upload(){
    if(!file||busy)return
    setBusy(true);setError('');setMessage('')
    try{
      await uploadCourseCover(token,courseId,file)
      const blob=await fetchOwnedCover(token,courseId)
      if(blob){
        const url=URL.createObjectURL(blob)
        setCurrent(prev=>{if(prev)URL.revokeObjectURL(prev);return url})
      }
      setFile(null)
      setMessage('Capa salva no curso. Se ele já estiver publicado, a imagem estará disponível no catálogo.')
    }catch(err){setError(err instanceof Error?err.message:'Não foi possível enviar a capa')}
    finally{setBusy(false)}
  }
  return <section className="course-cover-editor">
    <h3>Imagem de capa do curso</h3>
    <p>Envie ou substitua a capa sem perder aulas nem matrículas, inclusive em cursos já publicados.</p>
    {current&&<img className="course-cover-current" src={current} alt="Capa atual do curso"/>}
    <CourseCoverPicker file={file} onSelect={setFile} label={current?'Trocar imagem de capa':'Adicionar imagem de capa'}/>
    <button type="button" className="secondary-btn" disabled={!file||busy} onClick={()=>void upload()}>
      {busy?'Enviando...':'Salvar imagem de capa ↑'}
    </button>
    {error&&<p className="inline-error" role="alert">{error}</p>}
    {message&&<p className="learning-feedback" role="status">{message}</p>}
  </section>
}
