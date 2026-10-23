# Homologacao E2E - jornada do aluno

## Estado conhecido

O smoke test publico da Home ja verificou HTTP 200 para frontend, healthcheck, catalogo e busca. Isso **nao** prova matricula, autenticacao ou progresso.

O catalogo local observado em 23/10/2026 retornou quatro cursos pagos e nenhum curso com capa cadastrada. Esses dados podem mudar; confirme o ambiente ativo antes de testar. Nao execute comandos que apaguem volumes.

## Rotas confirmadas no backend

| Fluxo | Metodo | Rota |
| --- | --- | --- |
| Catalogo | GET | `/api/v1/courses` |
| Detalhes | GET | `/api/v1/courses/{slug}` |
| Registro do aluno | POST | `/api/v1/auth/register` |
| Login | POST | `/api/v1/auth/login` |
| Usuario atual | GET | `/api/v1/auth/me` |
| Matricula | POST | `/api/v1/courses/{slug}/enrollments` |
| Aulas do aluno | GET | `/api/v1/students/me/courses/{slug}/lessons` |
| Concluir aula | POST | `/api/v1/students/me/courses/{slug}/lessons/{lessonId}/complete` |

O identificador do aluno vem do JWT validado, nao do corpo da requisicao.

## Preparacao segura

1. Confirme `docker compose ps` e qual banco contem os cursos de interesse.
2. No Estudio do Instrutor, crie um **curso de teste gratuito**, com slug exclusivo `homologacao-jornada-aluno`, preco **R$ 0,00** (`priceCents=0`), modulo e aula obrigatoria, e publique-o. Nao modifique cursos existentes para esta homologacao.
3. Crie uma conta de **aluno de teste**, com email exclusivo, no formulario normal da aplicacao. Nao coloque senhas ou JWTs em logs, commits ou capturas.
4. Consulte o catalogo e confirme que o curso publicado aparece com preco zero antes de matricular.

## Execucao manual e resultados esperados

- [ ] Visitante: Home e catalogo carregam; busca pelo titulo encontra o curso gratuito.
- [ ] Visitante: detalhes apresentam conteudo correto; a matricula requer autenticacao.
- [ ] Aluno: login cria sessao; `GET /api/v1/auth/me` retorna o proprio usuario.
- [ ] Aluno: primeira matricula gratuita retorna sucesso, tipicamente `201`; o curso aparece no dashboard.
- [ ] Aluno: matricula repetida e tratada de forma consistente, sem duplicar registro; consultar o recibo.
- [ ] Aluno: `GET /api/v1/students/me/courses/{slug}/lessons` retorna os modulos e aulas acessiveis.
- [ ] Aluno: concluir uma aula atualiza progresso; recarregar o dashboard preserva o estado.
- [ ] Outro aluno: nao consegue alterar progresso do primeiro aluno.
- [ ] Curso pago: tentativa de matricula direta nao libera acesso sem checkout confirmado.
- [ ] Mobile: repetir descoberta, login, matricula e reproducao/leitura com viewport de 375px; testar teclado.

## Evidencias

Registrar URL local, data, branch/commit, resultado e capturas sem dados pessoais ou tokens. Marcar cada item como passou/falhou/bloqueado. Um HTTP 200 isolado nao comprova a jornada.

## Falhas / encaminhamento

Se falhar, consultar `docker compose logs --tail=100 backend` e o Network do navegador. Nao publicar credenciais, JWTs ou dados pessoais em issues. Se nao houver conta de instrutor, registrar a criacao do curso como **bloqueada**, sem inventar sucesso.
