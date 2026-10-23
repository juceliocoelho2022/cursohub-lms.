import {useState} from 'react'
import type {CourseAsset} from '../services/assets'
import {fetchStudentAsset,saveBlob} from '../services/assets'

interface Props {token:string;slug:string;assets:CourseAsset[]}
export function CourseDownloadMaterials({token,slug,assets}:Props) {
  const [error,setError]=useState('')
  const [busy,setBusy]=useState<string|null>(null)
  const files=assets.filter(a=>a.lessonId===null)
  if(files.length===0) return null
  async function download(item:CourseAsset){
    setBusy(item.id);setError('')
    try{ const blob=await fetchStudentAsset(token,slug,item.id);saveBlob(blob,item.filename) }
    catch(e){setError(e instanceof Error?e.message:'Falha ao baixar material')}
    finally{setBusy(null)}
  }
  return <section className="learning-materials">
    <h2>Materiais complementares</h2>
    <p>Arquivos publicados pelo instrutor para alunos matriculados.</p>
    <div className="learning-material-grid">
      {files.map(item=><button type="button" key={item.id} disabled={busy!==null}
        className="learning-material-item" onClick={()=>void download(item)}>
        <span>{item.mediaType.startsWith('image/')?'▧':item.mediaType.includes('zip')?'▣':'▤'}</span>
        <strong>{item.filename}</strong>
        <small>{(item.sizeBytes/1024/1024).toFixed(2)} MB · {busy===item.id?'Carregando...':'Baixar ↧'}</small>
      </button>)}
    </div>
    {error&&<p className="inline-error" role="alert">{error}</p>}
  </section>
}
