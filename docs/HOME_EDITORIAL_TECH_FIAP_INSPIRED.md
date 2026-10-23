# CursoHub — Home editorial tech (referência FIAP, identidade própria)

**Branch:** `feature/home-fiap-inspired`  
**PR:** #11 → base `feature/home-premium-redesign` (#10)  
**Não integrado à main.** Dependências acumuladas: #8 capa → #9 preços e planos → #10 home premium → #11 editorial tech.

## Objetivo

Recriar a experiência visual pública da home com o impacto de portais modernos de educação tecnológica: fundo escuro, tipografia editorial condensada, alto contraste, acentos neon, navegação por trilhas de conhecimento, imagens originais de cursos e conteúdo comercial transparente.

O site FIAP foi usado **apenas como referência de linguagem visual**. Nada neste PR reproduz a marca, o logotipo, os vídeos, a fotografia, a hierarquia exata, nem afirma qualquer parceria com a FIAP. **Não importar indicadores de empregabilidade, MEC, rankings ou parceiros da FIAP** para o CursoHub.

## O que está implementado

- Faixa de utilidades no topo e acesso ao Clube CursoHub.
- Hero: APRENDA. PRATIQUE. EVOLUA. + CTAs reais de catálogo e planos.
- Arte original ilustrativa em CSS (terminal e formas geométricas): sem download de assets externos.
- Faixa informativa de temas de estudo: Java, React, Spring Boot, Big Data, SQL e Cloud.
- Três cartões editoriais clicáveis que aplicam filtros reais do catálogo: Programação, Dados e Matemática.
- Seção de experiência pedagógica com botão funcional para o catálogo.
- Cabeçalhos do catálogo e dos planos com tipografia editorial e cards mais retos.
- Responsivo para desktop, tablet e mobile; estados de hover/foco visível e suporte a `prefers-reduced-motion`.
- Mantidos: cadastro/login, matrícula, vídeos, progresso, temas Claro/Moderado/Black, Estúdio do Instrutor, APIs, uploads de capa, preços, planos e fidelidade.

## Instalar sem perder os cursos

```powershell
cd C:\Projetos\cursohub-lms-repo
git status
git fetch origin
git switch --track origin/feature/home-fiap-inspired

docker compose -p cursohub-lms up -d --build
docker compose -p cursohub-lms ps

curl.exe -i http://localhost:8080/actuator/health
curl.exe -i http://localhost:8080/api/v1/courses
curl.exe -i http://localhost:8080/api/v1/membership/plans
```

Se a branch já existir: `git switch feature/home-fiap-inspired`; em seguida `git pull --ff-only origin feature/home-fiap-inspired`.

**Não execute `docker compose down -v`** e preserve alterações locais se `git status` mostrar arquivos modificados.

Entre em `http://localhost:3000` e use **Ctrl + Shift + R**.

## Check visual de homologação

1. No topo, faixa CursoHub Tech Education + logotipo original (não FIAP).
2. Hero preto com linhas editoriais, título grande e terminal ilustrado. Botões **EXPLORAR CURSOS** e **CONHECER PLANOS** navegam às seções.
3. Faixa magenta de temas técnicos visível sem sobreposição.
4. Três cartões Desenvolvimento, Dados e Matemática: cada clique rola ao catálogo e aplica a categoria correta; a busca continua editável.
5. Catálogo exibe preço, capa real e modal; filtros e paginação permanecem funcionais.
6. Seção de planos mostra valores de 1/6/12 meses ou uma prévia comercial explicitamente rotulada quando a API estiver indisponível; contratação ainda desabilitada.
7. Login do aluno, painel de cursos, progresso e Estúdio do Instrutor continuam acessíveis.
8. Conferir larguras 1440px, 1024px, 768px e 390px com DevTools; evitar overflow horizontal e textos cortados.
9. Conferir temas Claro, Moderado e Black; a home editorial usa base escura intencionalmente, sem substituir as preferências internas do aluno.
10. Testar foco por teclado (Tab) e modo de movimento reduzido.

## Sobre recursos

A home usa a fonte Barlow Condensed carregada pelo Google Fonts; se estiver offline, usa Arial Narrow/Impact como fallback. A arte do terminal é desenhada por CSS e HTML, sem exigir novos arquivos de imagem.

## Segurança e validação

Nenhuma migração Flyway adicional foi criada; nenhuma regra de pagamento, autenticação ou matrículas foi alterada. A oferta de planos permanece informativa, sem promessas falsas de compra funcional.

**Antes do merge para a main:** confirmar testes Java/React/PostgreSQL/JWT e capturas visuais no navegador local. Como o PR #11 é empilhado, integrar ou rebasear previamente #8, #9 e #10.
