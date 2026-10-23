export interface MembershipPlan {
  code: 'MENSAL' | 'SEMESTRAL' | 'ANUAL'
  title: string
  durationMonths: number
  priceCents: number
  courseLimit: number | null
  discountPercent: number
  benefits: string[]
}

export const money = (cents: number) =>
  new Intl.NumberFormat('pt-BR', {style: 'currency', currency: 'BRL'}).format(cents / 100)

/** Referência exibida SOMENTE se a API local estiver indisponível.
 * Não concede acesso, não representa uma assinatura ativa nem inicia pagamento.
 * Os valores são os mesmos da proposta comercial na migração V6. */
export const membershipPreview: MembershipPlan[] = [
  {code:'MENSAL', title:'Plano Mensal', durationMonths:1, priceCents:5990,
   courseLimit:3, discountPercent:5, benefits:[
     'Até 3 cursos premium elegíveis durante a vigência',
     'Materiais complementares',
     'Certificados após cumprir os requisitos',
     '5% de desconto em compras avulsas elegíveis'
   ]},
  {code:'SEMESTRAL', title:'Plano Semestral', durationMonths:6, priceCents:26940,
   courseLimit:8, discountPercent:10, benefits:[
     'Até 8 cursos premium elegíveis durante a vigência',
     'Materiais complementares e trilhas especiais',
     'Certificados após cumprir os requisitos',
     '10% de desconto em compras avulsas elegíveis'
   ]},
  {code:'ANUAL', title:'Plano Anual', durationMonths:12, priceCents:47880,
   courseLimit:null, discountPercent:15, benefits:[
     'Acesso a todos os cursos premium elegíveis durante a vigência',
     'Materiais e trilhas especiais',
     'Certificados após cumprir os requisitos',
     '15% de desconto em compras avulsas elegíveis',
     'Acesso antecipado a novos cursos elegíveis'
   ]},
]

function isMembershipPlan(value:unknown):value is MembershipPlan {
  if(!value || typeof value !== 'object')return false
  const p=value as Partial<MembershipPlan>
  return (p.code==='MENSAL'||p.code==='SEMESTRAL'||p.code==='ANUAL') &&
    typeof p.title==='string' &&
    typeof p.durationMonths==='number' &&
    typeof p.priceCents==='number' && Number.isInteger(p.priceCents) &&
    typeof p.discountPercent==='number' &&
    (p.courseLimit===null||typeof p.courseLimit==='number') &&
    Array.isArray(p.benefits)&&p.benefits.every(x=>typeof x==='string')
}

export async function loadMembershipPlans(signal?:AbortSignal):Promise<MembershipPlan[]> {
  let response:Response
  try {
    response=await fetch('/api/v1/membership/plans',{signal,cache:'no-store'})
  }catch(error){
    if(signal?.aborted)throw error
    throw new Error('Sem conexão com o serviço de planos. Verifique se o backend está atualizado e acessível.')
  }
  if(!response.ok){
    throw new Error(`Serviço de planos indisponível (HTTP ${response.status}). Confirme a API e a migração Flyway V6.`)
  }
  let data:unknown
  try{data=await response.json()}catch{
    throw new Error('A API retornou uma resposta inválida para a lista de planos.')
  }
  if(!Array.isArray(data)||data.length===0||!data.every(isMembershipPlan))
    throw new Error('A API retornou dados de planos incompletos.')
  return data
}
