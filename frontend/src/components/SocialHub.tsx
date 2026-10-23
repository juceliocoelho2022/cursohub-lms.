import {useEffect,useState} from 'react'
import {loadSocialLinks,networks} from '../services/social'
import type {SocialLinks} from '../services/social'
import {SocialShare} from './SocialShare'

export function SocialHub(){
  const [links,setLinks]=useState<SocialLinks>({})
  const [notice,setNotice]=useState('')
  useEffect(()=>{
    const controller=new AbortController()
    void loadSocialLinks(controller.signal).then(config=>{
      if(!controller.signal.aborted)setLinks(config)
    }).catch(()=>{
      if(!controller.signal.aborted)setNotice('Os perfis ainda não estão configurados. Consulte o arquivo social-links.json.')
    })
    return ()=>controller.abort()
  },[])
  return <section id="redes-sociais" className="ch-social-hub" aria-labelledby="ch-social-title">
    <div className="container ch-social-container">
      <div className="ch-social-copy">
        <span className="ch-social-eyebrow">04 / COMUNIDADE & CONEXÕES</span>
        <h2 id="ch-social-title">O APRENDIZADO <em>NÃO PARA</em> AQUI.</h2>
        <p>Explore projetos, acompanhe conteúdos e compartilhe sua jornada.
          Conecte-se aos canais que já estão disponíveis.</p>
        <SocialShare title="CursoHub — Aprenda, pratique e evolua"/>
      </div>
      <div className="ch-social-panel">
        <div className="ch-social-panel-top">
          <span>SIGA O CURSOHUB</span>
          <span aria-hidden="true">↗</span>
        </div>
        <div className="ch-social-networks">
          {networks.map(network=>{
            const url=links[network.key]
            return url?<a href={url} key={network.key} target="_blank" rel="noopener noreferrer"
              aria-label={`Abrir ${network.label} em nova aba`} className="ch-social-network">
              <span className={`ch-social-network-symbol ch-social-network-${network.key}`} aria-hidden="true">{network.icon}</span>
              <span className="ch-social-network-text"><strong>{network.label}</strong><small>{network.description}</small></span>
              <span className="ch-social-network-arrow" aria-hidden="true">↗</span>
            </a>:<div className="ch-social-network ch-social-network-soon" key={network.key}
              aria-label={`${network.label} — canal ainda não configurado`}>
              <span className={`ch-social-network-symbol ch-social-network-${network.key}`} aria-hidden="true">{network.icon}</span>
              <span className="ch-social-network-text"><strong>{network.label}</strong><small>Canal em breve</small></span>
              <span className="ch-social-soon">EM BREVE</span>
            </div>
          })}
        </div>
        {notice&&<p className="ch-social-notice" role="status">{notice}</p>}
      </div>
    </div>
  </section>
}
