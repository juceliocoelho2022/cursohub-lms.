export interface LoyaltySummary {
  tier: 'BRONZE'|'PRATA'|'OURO'
  completedCourses:number
  points:number
  nextTierAt:number
  notice:string
}
export async function loadLoyalty(token:string,signal?:AbortSignal):Promise<LoyaltySummary>{
  const response=await fetch('/api/v1/students/me/loyalty',{
    headers:{Authorization:`Bearer ${token}`},credentials:'omit',signal,cache:'no-store'
  })
  if(!response.ok)throw new Error('Não foi possível carregar seus pontos de fidelidade.')
  return await response.json() as LoyaltySummary
}
