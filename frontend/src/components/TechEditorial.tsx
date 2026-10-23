interface TechEditorialProps {
  onExplore: () => void
  onPlans: () => void
  onCategory: (value: string) => void
}

const tracks = [
  {
    number: '01',
    label: 'DESENVOLVIMENTO',
    title: 'DO PRIMEIRO CÓDIGO ÀS APIS.',
    description: 'Java, React, Spring Boot e práticas de desenvolvimento.',
    icon: '</>',
    category: 'Programação',
    symbol: 'code',
  },
  {
    number: '02',
    label: 'DADOS & ANALYTICS',
    title: 'TRANSFORME DADOS EM DECISÕES.',
    description: 'SQL, Python, Big Data e fundamentos de engenharia de dados.',
    icon: '{ }',
    category: 'Dados',
    symbol: 'data',
  },
  {
    number: '03',
    label: 'MATEMÁTICA & LÓGICA',
    title: 'ENTENDA A LÓGICA POR TRÁS DA TECH.',
    description: 'Raciocínio, algoritmos e matemática aplicada.',
    icon: '∑',
    category: 'Matemática',
    symbol: 'math',
  },
]

export function TechEditorial({onExplore,onPlans,onCategory}:TechEditorialProps) {
  return <>
    <section className="ch-fiap-hero" aria-labelledby="ch-hero-heading">
      <div className="ch-hero-mesh" aria-hidden="true"/>
      <div className="container ch-hero-container">
        <div className="ch-hero-eyebrow">
          <span className="ch-glow-dot"/> CURSOHUB ACADEMY
          <span className="ch-hero-separator">//</span> SEU ESPAÇO PARA APRENDER TECH
        </div>
        <div className="ch-hero-layout">
          <div className="ch-hero-message">
            <h1 id="ch-hero-heading">
              <span>APRENDA.</span>
              <span className="ch-hero-accent">PRATIQUE.</span>
              <span>EVOLUA<span className="ch-hero-dot">.</span></span>
            </h1>
            <p>Conhecimento para sair da teoria e construir. Cursos online de programação,
              dados e matemática, com aulas organizadas e evolução no seu ritmo.</p>
            <div className="ch-hero-actions">
              <button type="button" className="ch-btn-main" onClick={onExplore}>
                EXPLORAR CURSOS <span aria-hidden="true">↗</span>
              </button>
              <button type="button" className="ch-btn-ghost" onClick={onPlans}>
                CONHECER PLANOS <span aria-hidden="true">→</span>
              </button>
            </div>
            <div className="ch-hero-highlights">
              <span><i aria-hidden="true"/> Conteúdos no seu ritmo</span>
              <span><i aria-hidden="true"/> Prática e projetos</span>
              <span><i aria-hidden="true"/> Progresso por aula</span>
            </div>
          </div>
          <div className="ch-tech-stage" aria-hidden="true">
            <div className="ch-stage-scan"/>
            <div className="ch-stage-ring one"/>
            <div className="ch-stage-ring two"/>
            <div className="ch-stage-orbit-text">LEARN · BUILD · GROW</div>
            <div className="ch-stage-terminal">
              <div className="ch-stage-termbar"><span>● ● ●</span><span>cursohub/tech-lab</span></div>
              <div className="ch-stage-terminal-content">
                <span className="ch-stage-pretitle">// SEU PRÓXIMO PASSO</span>
                <p><span>const</span> jornada = &#123;</p>
                <p className="ch-code-indented">curiosidade: <b>true</b>,</p>
                <p className="ch-code-indented">prática: <b>'sempre'</b>,</p>
                <p className="ch-code-indented">futuro: <b>'em evolução'</b></p>
                <p>&#125;;</p>
                <div className="ch-stage-command">▸ executar minha jornada_</div>
              </div>
            </div>
            <div className="ch-stage-badge top">01 / DESENVOLVA</div>
            <div className="ch-stage-badge bottom">✳ CONHECIMENTO EM MOVIMENTO</div>
            <span className="ch-stage-spark">✳</span>
          </div>
        </div>
        <div className="ch-hero-foot">
          <span>DESENVOLVIMENTO <i/> DADOS <i/> MATEMÁTICA</span>
          <span>SCROLL PARA EXPLORAR <strong aria-hidden="true">↓</strong></span>
        </div>
      </div>
    </section>

    <div className="ch-tech-marquee" aria-label="Temas de aprendizagem">
      <div className="container ch-marquee-track">
        <span>JAVA</span><b aria-hidden="true">✳</b>
        <span>REACT</span><b aria-hidden="true">✳</b>
        <span>SPRING BOOT</span><b aria-hidden="true">✳</b>
        <span>BIG DATA</span><b aria-hidden="true">✳</b>
        <span>SQL</span><b aria-hidden="true">✳</b>
        <span>CLOUD</span>
      </div>
    </div>

    <section className="ch-learning-tracks" aria-labelledby="ch-learning-heading">
      <div className="container">
        <div className="ch-section-kicker"><span>01</span> / ÁREAS DO CONHECIMENTO</div>
        <div className="ch-tracks-title">
          <h2 id="ch-learning-heading">ESCOLHA SUA <em>PRÓXIMA FRONTEIRA.</em></h2>
          <p>Encontre seu caminho e comece pelo que faz sentido para você. As áreas abaixo levam diretamente ao catálogo de cursos.</p>
        </div>
        <div className="ch-track-grid">
          {tracks.map(track=><button
            type="button"
            className={`ch-track-card ch-track-${track.symbol}`}
            key={track.category}
            onClick={()=>onCategory(track.category)}
            aria-label={`Explorar cursos de ${track.category}`}
          >
            <div className="ch-track-top"><span>{track.number} / {track.label}</span><span aria-hidden="true">↗</span></div>
            <div className="ch-track-symbol" aria-hidden="true">{track.icon}</div>
            <h3>{track.title}</h3>
            <p>{track.description}</p>
            <div className="ch-track-bottom"><span>VER CURSOS</span><span aria-hidden="true">⟶</span></div>
          </button>)}
        </div>
      </div>
    </section>

    <section className="ch-statement" aria-labelledby="ch-statement-heading">
      <div className="container ch-statement-layout">
        <div className="ch-statement-index">02 / A EXPERIÊNCIA CURSOHUB</div>
        <h2 id="ch-statement-heading">AQUI, <em>APRENDER</em> VIRA <span>PRÁTICA.</span></h2>
        <div className="ch-statement-bottom">
          <p>Crie sua conta, escolha um curso, acompanhe suas aulas e evolua em cada etapa.
            Uma plataforma feita para transformar interesse em conhecimento aplicado.</p>
          <button type="button" className="ch-statement-cta" onClick={onExplore}>COMEÇAR A APRENDER <span aria-hidden="true">↗</span></button>
        </div>
      </div>
    </section>
  </>
}
