# CursoHub LMS — Home Premium 2.0

## O que mudou
- Hero redesenhado com mensagem clara, terminal visual em CSS e ações funcionais para **Explorar cursos** e **Conhecer planos**.
- Atalhos por área: Programação, Dados e Matemática vinculados aos filtros do catálogo (não são apenas cartões decorativos).
- Cards de cursos continuam apresentando imagem, preço, nível e duração já retornados pelo backend.
- Clube CursoHub com planos **Mensal, Semestral, Anual**, duração, preço total, equivalente mensal, economia ilustrativa frente à proposta mensal, benefícios e status **Contratação em breve**.
- Fidelidade com Bronze/Prata/Ouro e painel de pontos de usuário quando autenticado como estudante. Pontos e cursos concluídos vêm do backend existente; nenhuma pontuação é incrementada artificialmente no browser.
- Contratação, assinatura ativa, renovação automática, descontos reais e pagamento não são anunciados como funcionais.

## Se aparecer "prévia dos planos" em vez de dados reais
A home agora mostra uma prévia **explicitamente rotulada**, com preços e benefícios de referência iguais à migração V6, e um botão **Tentar novamente**. Isso impede uma área vazia quando há falha local de API, mas **não substitui a correção do backend**.

### Diagnóstico local — PowerShell
```powershell
cd C:\Projetos\cursohub-lms-repo
git status
git fetch origin
git switch --track origin/feature/home-premium-redesign

docker compose -p cursohub-lms up -d --build
docker compose -p cursohub-lms ps

curl.exe -i http://localhost:8080/actuator/health
curl.exe -i http://localhost:8080/api/v1/membership/plans

docker compose -p cursohub-lms exec -T db psql -U cursohub -d cursohub -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```
Se já houver branch local, use `git switch feature/home-premium-redesign` e `git pull --ff-only origin feature/home-premium-redesign`. Preserve modificações locais.

### Resultado esperado
- API health: HTTP 200, `status=UP`.
- API de planos: HTTP 200, array JSON com **3 planos**.
- Flyway: V1, V2, V3, V4, V5 e **V6** com `success=t`.
- Se receber **404**, backend pode estar em branch/imagem desatualizada.
- Se receber **500**, verificar `docker compose -p cursohub-lms logs --tail=120 backend`; observar Flyway, JDBC e consultas dos planos. Não excluir banco nem volume.
- Se houver **connection refused**, checar status de backend e porta 8080 antes de reconstruir.
- Se API retornar 200 mas frontend mostrar a prévia, atualizar com `Ctrl+Shift+R` e verificar no DevTools > Network a requisição `/api/v1/membership/plans`.
- O botão de retentativa permite consultar novamente sem recarregar a página.

## Homologação
1. Abrir `http://localhost:3000` com o tema Black, depois Claro e Moderado.
2. Conferir o hero, links **Explorar cursos** e **Conhecer planos**.
3. Selecionar Programação, Dados e Matemática nos atalhos de área; ver alteração do filtro e rolagem ao catálogo.
4. Conferir cartões e preços de cursos. Cursos pagos continuam sem matrícula gratuita.
5. Conferir plano Mensal de R$ 59,90, Semestral de R$ 269,40 e Anual de R$ 478,80; todos **sem contratar**.
6. Testar a página nas larguras 1440, 1024 e 390 px.
7. Com a API indisponível, confirmar exibição de **prévia comercial**, erro legível e botão **Tentar novamente**.
8. Confirmar que cursos, matrículas, videoaulas e certificados pré-existentes continuam disponíveis.

**Segurança de dados:** nunca executar `docker compose down -v`. A branch não inclui migração nova: usa V1–V6 já existentes no PR #9.

**Integração Git:** PR #10 é empilhado sobre PR #9, que depende do PR #8 de capas. A `main` permanece estável e não deve receber PR #10 isoladamente sem resolver a cadeia.
