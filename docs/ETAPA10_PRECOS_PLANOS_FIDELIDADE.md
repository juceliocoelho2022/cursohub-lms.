# CursoHub LMS — Preços dos cursos e Clube CursoHub

**Situação:** PR #9 em rascunho, branch `feature/course-pricing-loyalty`, baseado no PR #8 (capas de curso). A `main` permanece inalterada. A Etapa 8 (avaliação e certificados) é um PR independente.

## 1. Planos propostos

**Todos os valores em reais e cobrados como valor total do período**, não como mensalidades recorrentes nesta versão. Não há renovação automática implementada.

| Plano | Prazo | Total | Equiv./mês | Limite de cursos premium elegíveis | Desconto sugerido em compras avulsas |
|---|---:|---:|---:|---:|---:|
| Mensal | 1 mês | R$ 59,90 | R$ 59,90 | 3 | 5% |
| Semestral | 6 meses | R$ 269,40 | R$ 44,90 | 8 | 10% |
| Anual | 12 meses | R$ 478,80 | R$ 39,90 | Todos os elegíveis | 15% |

**Esses preços são uma proposta inicial e não correspondem a vendas ativas.**

Benefícios comuns a todos: acesso aos materiais dos cursos incluídos, acompanhamento do progresso, certificados **após concluir as aulas e satisfazer as avaliações obrigatórias**. Benefícios do semestral: trilhas especiais; do anual: trilhas especiais e acesso antecipado a novos cursos elegíveis. O número de cursos é uma proposta de limite para matrículas elegíveis durante a vigência, não equivale à compra vitalícia de um curso.

Todos os descontos e vantagens estão **catalogados, mas não são aplicados em transações reais** até o sistema receber pagamentos confirmados. O status do plano na home é **Contratação em breve**.

## 2. Precificação individual do catálogo

- `courses.price_cents` em centavos de BRL, `0` indica gratuito.
- Cursos existentes mantêm `0` por padrão. Alguns cursos demonstrativos recebem valores ilustrativos pela migração V5.
- O Estúdio do Instrutor permite atribuir preço ao criar um curso.
- Cartões e detalhes exibem os preços da API, nunca valores estáticos no frontend.
- Uma matrícula nova em curso pago é **bloqueada na API** enquanto não existe checkout confiável. Matrículas anteriores continuam associadas aos seus alunos; não são excluídas por mudança no preço do catálogo.

## 3. Programa gratuito de fidelidade por aprendizagem

Além dos planos pagos *futuros*, foi implementado o **Clube CursoHub de conquistas**, sem contratação:
- **100 pontos** por curso com todas as aulas obrigatórias concluídas (incluindo vídeos), calculados pelo PostgreSQL e vinculados à matrícula do aluno autenticado.
- **Bronze:** 0 ou 1 curso concluído. **Prata:** 2 a 4 cursos. **Ouro:** 5 ou mais cursos.
- `GET /api/v1/students/me/loyalty` exige JWT com papel STUDENT e retorna `tier`, `completedCourses`, `points`, `nextTierAt` e `notice`.
- O painel de pontos aparece na seção Clube CursoHub da home quando o aluno entra.
- Os pontos **não têm valor monetário, não são transferíveis e não podem ser resgatados ou convertidos em descontos** nesta versão. Benefícios comerciais exigem futura implementação de checkout e regras contratuais.

## 4. Estrutura de dados

- `V5__course_pricing.sql`: `courses.price_cents`.
- `V6__membership_plans.sql`: catálogo de planos e tabela de futuras assinaturas `student_memberships`.
- `GET /api/v1/membership/plans`: lista pública de ofertas, incluindo `code`, `durationMonths`, `priceCents`, `courseLimit`, `discountPercent` e `benefits`.
- **Nenhum endpoint de ativação ou pagamento existe nesta entrega.** A tabela de assinaturas permanece sem registros até a próxima etapa de integração com gateway.
- `student_memberships.status` prevê `PENDING_PAYMENT`, `ACTIVE`, `EXPIRED` e `CANCELLED`, mas **não é um sistema de cobrança funcionando**.

**Não executar `docker compose down -v`** nem alterar migrações aplicadas (V1–V4). O Flyway acrescenta V5 e V6 e preserva o PostgreSQL.

## 5. Passo a passo Windows

```powershell
cd C:\Projetos\cursohub-lms-repo
git status
git fetch origin
git switch --track origin/feature/course-pricing-loyalty
docker compose -p cursohub-lms up -d --build
docker compose -p cursohub-lms ps
curl.exe -i http://localhost:8080/actuator/health
curl.exe -i http://localhost:8080/api/v1/membership/plans
docker compose -p cursohub-lms exec -T db psql -U cursohub -d cursohub -c "SELECT version,description,success FROM flyway_schema_history ORDER BY installed_rank;"
```

Caso a branch já exista: `git switch feature/course-pricing-loyalty` + `git pull --ff-only origin feature/course-pricing-loyalty`. **Guarde mudanças locais antes do pull.**

## 6. Testes manuais

1. Abra `http://localhost:3000`, role até **Clube CursoHub — Planos e benefícios**; confira Mensal/Semestral/Anual, período total e equivalentes por mês.
2. Confira os botões **Contratação em breve** desativados (nenhuma ação de pagamento simulada).
3. Abra **Explorar cursos**; compare pelo menos um curso gratuito e um curso de demonstração pago, com preço em reais.
4. Entre como instrutor e crie um rascunho com preço em reais; publique após cadastrar uma aula obrigatória e confira a precificação no cartão público.
5. Entre com conta de aluno e confirme que o curso pago **não permite matrícula gratuita**; cursos gratuitos continuam matriculáveis.
6. Confira que certificados e material de aulas já liberados de outros cursos não foram perdidos.
7. Com uma conta de aluno, confira no Clube CursoHub o nível e os pontos por cursos efetivamente concluídos. A API deve rejeitar acesso sem autenticação.\n8. Confirme Flyway V1–V6, todas com `success=t`.

## 7. Próximos requisitos antes de vender assinaturas

- Checkout por provedor de pagamentos, cobrança com valor/planCode imutáveis no servidor e comunicação de confirmação por webhook assinado e idempotente.
- Provisão da assinatura exclusivamente após verificação de pagamento liquidado; controles de prazo de vigência, estorno, expiração, cancelamento e nova assinatura.
- Aplicação real de elegibilidade dos cursos premium e limite por plano na **API do aluno e no acesso às aulas**, inclusive depois que o plano expirar.
- Cálculo de desconto antes da cobrança, trilhas antecipadas e benefícios fiéis à oferta apresentada.
- Termos de serviço, privacidade, política de cancelamento/reembolso e informação clara sobre cobrança e renovação.
- Regras para preservar compras avulsas, progresso e certificados já emitidos, sem suspensão indevida.

**Não apresentar como ativos:** pagamento online, resgate de pontos, renovação, desconto aplicado no checkout ou ativação de assinatura. O saldo de pontos e as faixas Bronze/Prata/Ouro são funcionalidades reais, mas sem benefício monetário nesta etapa.
