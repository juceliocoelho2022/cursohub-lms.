import {useEffect,useState} from 'react'
import {loadMembershipPlans,money} from '../services/membership'
import type {MembershipPlan} from '../services/membership'
import {loadLoyalty} from '../services/loyalty'
import type {LoyaltySummary} from '../services/loyalty'

export function MembershipPlans({onExplore,token}:{onExplore:()=>void;token?:string}){
  const [loyalty,setLoyalty]=useState<LoyaltySummary|null>(null)
  const [plans,setPlans]=useState<MembershipPlan[]>([])
  const [error,setError]=useState('')
  const [loading,setLoading]=useState(true)
  useEffect(()=>{
    const ctrl=new AbortController()
    setLoading(true)
    loadMembershipPlans(ctrl.signal)
      .then(p=>{if(!ctrl.signal.aborted)setPlans(p)})
      .catch(e=>{if(!ctrl.signal.aborted)setError(e instanceof Error?e.message:'Falha no catálogo de planos')})
      .finally(()=>{if(!ctrl.signal.aborted)setLoading(false)})
    return ()=>ctrl.abort()
  },[])
  useEffect(()=>{
    if(!token){setLoyalty(null);return}
    const controller=new AbortController()
    void loadLoyalty(token,controller.signal).then(item=>{if(!controller.signal.aborted)setLoyalty(item)})
      .catch(()=>{if(!controller.signal.aborted)setLoyalty(null)})
    return ()=>controller.abort()
  },[token])
  return <section className="membership-section" id="planos" aria-labelledby="membership-heading">
    <div className="container">
      <div className="section-topline"><span className="mini-rule"/> CLUBE CURSOHUB</div>
      <div className="membership-intro">
        <div>
          <h2 id="membership-heading">Seu conhecimento tem <em>novos benefícios.</em></h2>
          <p>Escolha o tempo de acesso mais adequado à sua jornada. Conheça os planos e os benefícios de cada duração.</p>
        </div>
        <button type="button" className="secondary-btn" onClick={onExplore}>Ver cursos disponíveis ↗</button>
      </div>
      <div className="loyalty-summary">
        <div className="loyalty-copy">
          <span className="membership-label">PROGRAMA DE FIDELIDADE GRATUITO</span>
          <h3>{loyalty ? `Seu nível: ${loyalty.tier}` : 'Clube CursoHub — cada conquista conta'}</h3>
          <p>{loyalty
            ? `${loyalty.points} pontos · ${loyalty.completedCourses} curso(s) concluído(s). Próximo nível em ${loyalty.nextTierAt} cursos concluídos.`
            : 'Bronze: início da jornada · Prata: 2 cursos concluídos · Ouro: 5 cursos concluídos.'}</p>
          <small>Pontos por curso concluído: 100. Benefícios comerciais e resgates ainda não estão disponíveis.</small>
        </div>
        <span className="loyalty-emblem" aria-hidden="true">✦</span>
      </div>
      {loading&&<p className="membership-status" role="status">Carregando os planos...</p>}
      {error&&<p className="membership-status" role="alert">{error}</p>}
      {!loading&&!error&&<div className="membership-grid">
        {plans.map(plan=><article className={`membership-card ${plan.code==='ANUAL'?'membership-featured':''}`} key={plan.code}>
          {plan.code==='ANUAL'&&<span className="membership-recommendation">Maior economia por mês</span>}
          <span className="membership-label">{plan.durationMonths===1?'PARA COMEÇAR':plan.durationMonths===6?'PARA EVOLUIR':'PARA IR MAIS LONGE'}</span>
          <h3>{plan.title}</h3>
          <div className="membership-price">{money(plan.priceCents)}</div>
          <p className="membership-duration">{plan.durationMonths===1?'1 mês de acesso':`${plan.durationMonths} meses de acesso • equivalente a ${money(Math.round(plan.priceCents/plan.durationMonths))}/mês`}</p>
          <div className="membership-benefit-heading">O que está incluído</div>
          <ul>{plan.benefits.map(item=><li key={item}><span aria-hidden="true">✓</span>{item}</li>)}</ul>
          <button type="button" className="membership-cta" disabled aria-label={`${plan.title}: contratação em breve`}>Contratação em breve</button>
        </article>)}
      </div>}
      <p className="membership-footnote">
        Planos e valores de lançamento propostos, ainda indisponíveis para contratação. Acesso a cursos premium, descontos e benefícios serão liberados somente após integração e confirmação do pagamento. Cursos gratuitos continuam disponíveis normalmente. Certificados exigem conclusão e aprovação.
      </p>
    </div>
  </section>
}
