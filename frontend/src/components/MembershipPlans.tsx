import {useEffect,useState} from 'react'
import {loadMembershipPlans,membershipPreview,money} from '../services/membership'
import type {MembershipPlan} from '../services/membership'
import {loadLoyalty} from '../services/loyalty'
import type {LoyaltySummary} from '../services/loyalty'

function periodLabel(months:number){
  return months===1?'1 mês':`${months} meses`
}
function PlanCard({plan,reference}:{plan:MembershipPlan;reference:boolean}){
  const featured=plan.code==='ANUAL'
  const monthlyEquivalent=Math.round(plan.priceCents/plan.durationMonths)
  const savings=plan.durationMonths===1 ? 0 :
    Math.round((1-plan.priceCents/(5990*plan.durationMonths))*100)
  return <article className={`membership-card ${featured?'membership-featured':''}`}>
    {featured&&<div className="membership-featured-label">✦ Melhor valor mensal</div>}
    <div className="membership-plan-header">
      <div className="membership-icon" aria-hidden="true">
        {plan.durationMonths===1?'01':plan.durationMonths===6?'06':'12'}
      </div>
      <span className="membership-label">{plan.durationMonths===1?'COMECE AGORA':
        plan.durationMonths===6?'ACELERE SEUS ESTUDOS':'EVOLUA O ANO INTEIRO'}</span>
    </div>
    <h3>{plan.title}</h3>
    <div className="membership-price-block">
      <div className="membership-price">{money(plan.priceCents)}</div>
      <p className="membership-duration">Valor total por {periodLabel(plan.durationMonths)}</p>
      <div className="membership-monthly">Equivalente a <strong>{money(monthlyEquivalent)}/mês</strong></div>
      {savings>0&&<div className="membership-saving">Economize {savings}% em relação ao mensal</div>}
    </div>
    <div className="membership-benefit-heading">Sua experiência inclui</div>
    <ul>{plan.benefits.map(item=><li key={item}><span aria-hidden="true">✓</span><span>{item}</span></li>)}</ul>
    <button type="button" className="membership-cta" disabled>
      Contratação em breve <span aria-hidden="true">↗</span>
    </button>
    {reference&&<p className="membership-preview-hint">Prévia comercial · preços sujeitos à confirmação</p>}
  </article>
}

export function MembershipPlans({onExplore,token}:{onExplore:()=>void;token?:string}){
  const [loyalty,setLoyalty]=useState<LoyaltySummary|null>(null)
  const [plans,setPlans]=useState<MembershipPlan[]>([])
  const [error,setError]=useState('')
  const [loading,setLoading]=useState(true)
  const [attempt,setAttempt]=useState(0)
  useEffect(()=>{
    const ctrl=new AbortController()
    setLoading(true);setError('')
    loadMembershipPlans(ctrl.signal)
      .then(p=>{if(!ctrl.signal.aborted)setPlans(p)})
      .catch(e=>{if(!ctrl.signal.aborted)setError(e instanceof Error?e.message:'Falha no catálogo de planos')})
      .finally(()=>{if(!ctrl.signal.aborted)setLoading(false)})
    return ()=>ctrl.abort()
  },[attempt])
  useEffect(()=>{
    if(!token){setLoyalty(null);return}
    const controller=new AbortController()
    void loadLoyalty(token,controller.signal).then(item=>{if(!controller.signal.aborted)setLoyalty(item)})
      .catch(()=>{if(!controller.signal.aborted)setLoyalty(null)})
    return ()=>controller.abort()
  },[token])

  const visiblePlans=error?membershipPreview:plans
  const completed=loyalty?.completedCourses??0
  const nextAt=completed<2?2:completed<5?5:5
  const tier=loyalty?.tier??'Bronze'
  const percent=completed>=5?100:Math.min(100,Math.round(completed/nextAt*100))
  return <section className="membership-section" id="planos" aria-labelledby="membership-heading">
    <div className="container">
      <div className="membership-top">
        <div className="membership-headline">
          <div className="section-topline"><span className="mini-rule"/> CLUBE CURSOHUB</div>
          <h2 id="membership-heading">Invista no seu futuro. <em>Evolua no seu ritmo.</em></h2>
          <p>Escolha o período ideal para evoluir em tecnologia, programação e dados.
            Conteúdo organizado, progresso no seu ritmo e benefícios que crescem com você.</p>
        </div>
        <button type="button" className="membership-outline-action" onClick={onExplore}>
          Explorar os cursos <span aria-hidden="true">↗</span>
        </button>
      </div>

      <div className="membership-value-strip" aria-label="Diferenciais CursoHub">
        <span><b aria-hidden="true">✳</b> Aprendizagem prática</span>
        <span><b aria-hidden="true">◉</b> Evolução no seu ritmo</span>
        <span><b aria-hidden="true">♢</b> Trilhas por conhecimento</span>
        <span><b aria-hidden="true">✓</b> Progresso acompanhado</span>
      </div>

      <div className="membership-plans-heading">
        <div>
          <span className="membership-label">ESCOLHA SUA JORNADA</span>
          <h3>Um plano para cada objetivo.</h3>
          <p>Confira o valor total e o equivalente mensal antes de escolher.</p>
        </div>
        <span className="membership-no-charge">Sem cobrança online nesta fase</span>
      </div>

      {loading&&<div className="membership-skeletons" role="status" aria-live="polite">
        <div/><div/><div/><span className="sr-only">Carregando planos...</span>
      </div>}
      {!loading&&error&&<div className="membership-alert" role="status">
        <div>
          <strong>Exibindo uma prévia dos planos de lançamento.</strong>
          <p>O backend de assinaturas não respondeu: {error} Os valores abaixo são apenas referência e a contratação está desativada.</p>
        </div>
        <button type="button" onClick={()=>setAttempt(x=>x+1)}>Tentar novamente ↻</button>
      </div>}
      {!loading&&<div className="membership-grid">
        {visiblePlans.map(plan=><PlanCard key={plan.code} plan={plan} reference={Boolean(error)}/>)}
      </div>}

      <div className="loyalty-premium" aria-label="Programa de fidelidade">
        <div className="loyalty-premium-glow" aria-hidden="true"/>
        <div className="loyalty-premium-copy">
          <div className="loyalty-premium-overline">✦ PROGRAMA DE FIDELIDADE GRATUITO</div>
          <h3>Aprender vale pontos.<br/><span>E cada conquista conta.</span></h3>
          <p>Mesmo sem assinatura paga, você pode acompanhar sua evolução no Clube CursoHub.
            Ganhe 100 pontos por curso concluído e avance de nível.</p>
          <div className="loyalty-premium-tiers" aria-label="Níveis de fidelidade">
            <span className={completed<2?'is-current':''}><i>◈</i> Bronze <small>Início</small></span>
            <span className={completed>=2&&completed<5?'is-current':''}><i>✧</i> Prata <small>2 cursos</small></span>
            <span className={completed>=5?'is-current':''}><i>✦</i> Ouro <small>5 cursos</small></span>
          </div>
          <small className="loyalty-premium-disclaimer">Resgates e descontos por pontos ainda não estão disponíveis.</small>
        </div>
        <div className="loyalty-progress-card">
          <div className="loyalty-progress-icon" aria-hidden="true">✳</div>
          <div className="loyalty-progress-label">SEU CLUBE CURSOHUB</div>
          <h4>{token? `Nível ${tier}` : 'Sua jornada começa aqui'}</h4>
          <strong>{loyalty?loyalty.points:0}<small> pontos</small></strong>
          <div className="loyalty-progress-track" role="progressbar" aria-valuemin={0}
            aria-valuemax={100} aria-valuenow={percent} aria-label="Progresso de fidelidade">
            <span style={{width:`${percent}%`}}/>
          </div>
          <p>{token
            ? completed>=5?'Você alcançou o nível Ouro!':
              `${completed} de ${nextAt} cursos para o próximo nível.`
            : 'Entre como aluno para acompanhar seus pontos e evolução.'}</p>
        </div>
      </div>
      <p className="membership-footnote">
        Os planos são propostas comerciais, não assinaturas ativas. O acesso premium e os descontos
        dependem de checkout com pagamento confirmado. Cursos gratuitos seguem disponíveis; certificados
        dependem da conclusão e das regras de aprovação de cada curso.
      </p>
    </div>
  </section>
}
