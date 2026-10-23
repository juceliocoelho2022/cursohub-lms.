export type SocialNetwork = 'youtube' | 'instagram' | 'linkedin' | 'github' | 'facebook' | 'tiktok' | 'whatsapp'
export type SocialLinks = Partial<Record<SocialNetwork,string>> & { siteUrl?: string }

export const networks: {key:SocialNetwork; label:string; icon:string; description:string}[] = [
  {key:'youtube',label:'YouTube',icon:'▶',description:'Aulas e conteúdo em vídeo'},
  {key:'instagram',label:'Instagram',icon:'◎',description:'Dicas e bastidores'},
  {key:'linkedin',label:'LinkedIn',icon:'in',description:'Carreira e tecnologia'},
  {key:'github',label:'GitHub',icon:'</>',description:'Projetos e código'},
  {key:'facebook',label:'Facebook',icon:'f',description:'Comunidade e novidades'},
  {key:'tiktok',label:'TikTok',icon:'♪',description:'Conteúdos rápidos'},
]

const allowedHosts:Record<SocialNetwork,string[]> = {
  youtube:['youtube.com','youtu.be'],
  instagram:['instagram.com'],
  linkedin:['linkedin.com'],
  github:['github.com'],
  facebook:['facebook.com','fb.com'],
  tiktok:['tiktok.com'],
  whatsapp:['wa.me','api.whatsapp.com','chat.whatsapp.com'],
}
const knownKeys:SocialNetwork[]=['youtube','instagram','linkedin','github','facebook','tiktok','whatsapp']

function allowedHttpsUrl(input:unknown, hosts:string[]):string|undefined{
  if(typeof input!=='string'||!input.trim()||input.length>2048)return undefined
  try{
    const url=new URL(input)
    if(url.protocol!=='https:'||url.username||url.password)return undefined
    if(!hosts.some(host=>url.hostname===host||url.hostname.endsWith('.'+host)))return undefined
    return url.href
  }catch{return undefined}
}

export function normalizeSocialLinks(data:unknown):SocialLinks{
  const obj=data&&typeof data==='object'&&!Array.isArray(data)?data as Record<string,unknown>:{}
  const links:SocialLinks={}
  for(const network of knownKeys){
    const url=allowedHttpsUrl(obj[network],allowedHosts[network])
    if(url)links[network]=url
  }
  if(typeof obj.siteUrl==='string'&&obj.siteUrl.trim()){
    try{
      const url=new URL(obj.siteUrl)
      if(url.protocol==='https:'&&!url.username&&!url.password){
        links.siteUrl=url.origin
      }
    }catch{/* Invalid public site URL: use current origin. */}
  }
  return links
}

export async function loadSocialLinks(signal?:AbortSignal):Promise<SocialLinks>{
  const response=await fetch('/social-links.json',{cache:'no-store',signal})
  if(!response.ok)throw new Error('Configuração de redes sociais não encontrada')
  return normalizeSocialLinks(await response.json() as unknown)
}

export function buildShareUrl(slug?:string, publicSiteUrl?:string):string{
  const origin=publicSiteUrl||window.location.origin
  const link=new URL('/',origin)
  if(slug)link.searchParams.set('curso',slug)
  return link.href
}

export function isPublicShareUrl(url:string):boolean{
  try{
    const parsed=new URL(url)
    return parsed.protocol==='https:'&&
      !['localhost','127.0.0.1','[::1]'].includes(parsed.hostname)&&
      !parsed.hostname.endsWith('.local')
  }catch{return false}
}

export function shareWhatsApp(url:string,title:string):string{
  return 'https://api.whatsapp.com/send?'+new URLSearchParams({text:title+' — '+url})
}
export function shareLinkedIn(url:string):string{
  return 'https://www.linkedin.com/sharing/share-offsite/?'+new URLSearchParams({url})
}
