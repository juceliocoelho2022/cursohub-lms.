export interface MembershipPlan {
  code:'MENSAL'|'SEMESTRAL'|'ANUAL'
  title:string
  durationMonths:number
  priceCents:number
  courseLimit:number|null
  discountPercent:number
  benefits:string[]
}
export const money=(cents:number)=>new Intl.NumberFormat('pt-BR',{
  style:'currency',currency:'BRL'
}).format(cents/100)
export async function loadMembershipPlans(signal?:AbortSignal):Promise<MembershipPlan[]>{
  const response=await fetch('/api/v1/membership/plans',{signal,cache:'no-store'})
  if(!response.ok)throw new Error('Não foi possível carregar os planos.')
  return await response.json() as MembershipPlan[]
}
