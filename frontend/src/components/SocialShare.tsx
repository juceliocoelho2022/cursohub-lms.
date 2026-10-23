import {useEffect,useState} from 'react'
import {buildShareUrl,isPublicShareUrl,loadSocialLinks,shareLinkedIn,shareWhatsApp} from '../services/social'

interface Props { title:string; slug?:string; compact?:boolean }

export function SocialShare({title,slug,compact=false}:Props){
  const [publicSiteUrl,setPublicSiteUrl]=useState<string|undefined>()
  const [message,setMessage]=useState('')
  const [busy,setBusy]=useState(false)
  useEffect(()=>{
    const controller=new AbortController()
    void loadSocialLinks(controller.signal).then(config=>{
      if(!controller.signal.aborted)setPublicSiteUrl(config.siteUrl)
    }).catch(()=>{})
    return ()=>controller.abort()
  },[])
  const url=buildShareUrl(slug,publicSiteUrl)
  const canShare=isPublicShareUrl(url)

  async function copy(){
    setMessage('');setBusy(true)
    try{
      await navigator.clipboard.writeText(url)
      setMessage(canShare?'Link copiado! Agora você pode enviar para seus contatos.':
        'Link local copiado. Ele só funciona no seu computador; publique o CursoHub para compartilhar externamente.')
    }catch{
      setMessage('Não foi possível copiar automaticamente. Use a barra de endereço para copiar o link.')
    }finally{setBusy(false)}
  }
  function openSocial(destination:'whatsapp'|'linkedin'){
    if(!canShare){
      setMessage('Links locais (localhost) não podem ser acessados por outras pessoas. Configure o endereço público do CursoHub.')
      return
    }
    const address=destination==='whatsapp'?shareWhatsApp(url,title):shareLinkedIn(url)
    window.open(address,'_blank','noopener,noreferrer')
  }
  async function systemShare(){
    if(!canShare){setMessage('Publique o CursoHub e configure seu endereço público antes de compartilhar.');return}
    if(!navigator.share){await copy();return}
    try{
      await navigator.share({title,url})
      setMessage('')
    }catch(error){
      if(error instanceof DOMException&&error.name==='AbortError')return
      setMessage('Não foi possível abrir o compartilhamento. Tente copiar o link.')
    }
  }
  return <div className={`social-share ${compact?'social-share-compact':''}`}>
    <div className="social-share-heading">{compact?'COMPARTILHAR ESTE CURSO':'COMPARTILHE O CURSOHUB'}</div>
    <div className="social-share-actions">
      <button type="button" onClick={()=>void copy()} disabled={busy}>↗ Copiar link</button>
      <button type="button" onClick={()=>openSocial('whatsapp')} disabled={!canShare}
        title={!canShare?'Disponível quando a plataforma tiver endereço público':undefined}>◉ WhatsApp</button>
      <button type="button" onClick={()=>openSocial('linkedin')} disabled={!canShare}
        title={!canShare?'Disponível quando a plataforma tiver endereço público':undefined}>in LinkedIn</button>
      {typeof navigator.share==='function'&&
        <button type="button" onClick={()=>void systemShare()} disabled={!canShare}>Compartilhar ↗</button>}
    </div>
    {!canShare&&<p className="social-share-hint">Esta versão usa localhost. Compartilhamento externo será ativado após configurar o domínio público.</p>}
    {message&&<p className="social-share-feedback" role="status">{message}</p>}
  </div>
}
