import {useEffect,useState} from 'react'
import {validateCoverFile} from '../services/courseCover'

interface Props {file:File|null; onSelect:(value:File|null)=>void; label?:string}
export function CourseCoverPicker({file,onSelect,label='Imagem de capa (opcional)'}:Props) {
  const [preview,setPreview]=useState('')
  const [error,setError]=useState('')
  useEffect(()=>{
    if(!file){setPreview('');return}
    const url=URL.createObjectURL(file)
    setPreview(url)
    return ()=>URL.revokeObjectURL(url)
  },[file])
  return <div className="course-cover-picker">
    <label>{label}
      <input type="file" accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
        aria-label={label} onChange={e=>{
          const selected=e.target.files?.[0]??null
          const message=selected?validateCoverFile(selected):null
          setError(message??'')
          onSelect(message?null:selected)
          e.currentTarget.value=''
        }}/>
    </label>
    <span className="course-cover-help">JPG, PNG ou WEBP, até 5 MB. Recomendado: imagem horizontal 16:9 (ex.: 1280 × 720).</span>
    {error&&<span className="inline-error" role="alert">{error}</span>}
    {preview&&<figure className="course-cover-preview">
      <img src={preview} alt="Prévia da imagem escolhida para o curso"/>
      <figcaption>{file?.name}</figcaption>
      <button type="button" className="secondary-btn" onClick={()=>{setError('');onSelect(null)}}>
        Remover seleção
      </button>
    </figure>}
  </div>
}
