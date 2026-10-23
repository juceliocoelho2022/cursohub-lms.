import { useEffect, useRef, useState } from 'react'
import { fetchStudentAsset, reportVideoWatch } from '../services/assets'

interface Props {
  token: string
  slug: string
  lessonId: string
  assetId: string
  onCompleted: () => Promise<void>
}

export function ProtectedVideoPlayer({token,slug,lessonId,assetId,onCompleted}:Props) {
  const [source,setSource]=useState('')
  const [error,setError]=useState('')
  const [watched,setWatched]=useState(0)
  const [total,setTotal]=useState(0)
  const [finished,setFinished]=useState(false)
  const element=useRef<HTMLVideoElement|null>(null)
  const reporting=useRef(false)
  const authorized=useRef(0)
  const lastSent=useRef(0)

  useEffect(()=>{
    let live=true
    let objectUrl=''
    setSource('');setError('');setWatched(0);setTotal(0);setFinished(false)
    reporting.current=false;authorized.current=0;lastSent.current=0
    Promise.all([fetchStudentAsset(token,slug,assetId),reportVideoWatch(token,slug,lessonId,0)])
      .then(([blob,progress])=>{
        if(!live) return
        objectUrl=URL.createObjectURL(blob)
        authorized.current=progress.watchedSeconds
        lastSent.current=progress.watchedSeconds
        setWatched(progress.watchedSeconds)
        setTotal(progress.totalSeconds)
        setFinished(progress.completed)
        setSource(objectUrl)
      })
      .catch(e=>{if(live)setError(e instanceof Error?e.message:'Não foi possível carregar o vídeo')})
    return ()=>{live=false;if(objectUrl)URL.revokeObjectURL(objectUrl)}
  },[token,slug,lessonId,assetId])

  async function updateProgress(target:number) {
    if(reporting.current || finished || target<=lastSent.current) return
    if(target>authorized.current+3) return
    reporting.current=true
    try {
      const progress=await reportVideoWatch(token,slug,lessonId,target)
      authorized.current=progress.watchedSeconds
      lastSent.current=progress.watchedSeconds
      setWatched(progress.watchedSeconds)
      setTotal(progress.totalSeconds)
      if(progress.completed) {
        setFinished(true)
        await onCompleted()
      }
    } catch(e){setError(e instanceof Error?e.message:'Erro ao registrar o vídeo')}
    finally{reporting.current=false}
  }

  return <div className="learning-video-watched">
    {error && <p role="alert" className="inline-error">{error}</p>}
    {!source && !error && <p>Carregando vídeo protegido...</p>}
    {source && <video ref={element} className="learning-video" src={source} controls playsInline
      controlsList="nodownload noplaybackrate"
      onRateChange={e=>{if(e.currentTarget.playbackRate!==1)e.currentTarget.playbackRate=1}}
      onSeeking={e=>{
        const video=e.currentTarget
        if(video.currentTime>authorized.current+2.5) video.currentTime=authorized.current
      }}
      onTimeUpdate={e=>{
        if(e.currentTarget.paused || reporting.current || finished) return
        const seconds=Math.floor(e.currentTarget.currentTime)
        if(seconds>=lastSent.current+2) void updateProgress(Math.min(seconds,authorized.current+3))
      }}
      onEnded={()=>{
        if(!finished) void updateProgress(total)
      }}
    />}
    <div className="learning-video-watch-summary">
      <span>Progresso da videoaula: <strong>{total?Math.round(100*watched/total):0}%</strong></span>
      <span>{watched} de {total} segundos validados pelo servidor</span>
    </div>
    <p>{finished?'Vídeo concluído. A próxima aula foi liberada.':'Assista em sequência até 100%. Avançar ou pular trechos não libera a próxima aula.'}</p>
  </div>
}
