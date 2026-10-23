# CursoHub LMS — Etapa 8: Avaliações e Certificados

**Situação:** implementação na branch `feature/assessments-certificates`; PR #7 em rascunho até validação em Docker. **Ainda não integrada à main.**

## Objetivo pedagógico

Permitir que o instrutor configure uma avaliação final objetiva de múltipla escolha antes de publicar o curso. O aluno responde após concluir as aulas obrigatórias; recebe uma nota numérica e, se aprovado, pode emitir um certificado verificável. Em cursos sem avaliação, o certificado exige apenas as aulas obrigatórias concluídas.

Este certificado **não é um diploma, título acadêmico, reconhecimento pelo MEC nem documento com certificação governamental**. É um comprovante de conclusão emitido pelo CursoHub.

## Regras de negócio implementadas

1. Apenas usuários `INSTRUCTOR` podem cadastrar a avaliação final de seus próprios cursos.
2. A avaliação só pode ser cadastrada quando o curso está em `RASCUNHO`; nesta versão existe **uma avaliação obrigatória por curso**. Após publicar, a avaliação não é alterável, preservando os requisitos dos alunos já matriculados.
3. Cada avaliação contém **1 a 20 questões**, cada questão com **2 a 5 opções** e **exatamente uma correta**.
4. A **nota mínima** configurável vai de **1 a 100%**, e o número máximo de tentativas de **1 a 10**.
5. Toda questão possui o mesmo peso (`1`); a nota é a porcentagem de acertos, arredondada para duas casas decimais (HALF_UP). A correção ocorre no **backend**, sem enviar o gabarito para o navegador.
6. A API valida que as respostas cobrem todas as questões sem repetições e que as alternativas pertencem realmente à prova. Envios inválidos retornam HTTP 400 e não consomem tentativas.
7. O estudante só pode submeter uma prova quando todas as aulas obrigatórias (incluindo vídeos) estiverem concluídas no banco de dados.
8. A submissão de cada tentativa é transacional e seriada por bloqueio da matrícula. Após a aprovação ou o esgotamento das tentativas, novas tentativas são bloqueadas com HTTP 409.
9. **Certificado:** é emitido somente para matrícula ativa/concluída, todas as aulas obrigatórias concluídas, **ao menos uma aula obrigatória no curso**, e todas as avaliações obrigatórias aprovadas. Se não houver avaliação, basta o requisito de aulas.
10. A emissão é idempotente, com um certificado por matrícula, código de verificação UUID aleatório único, data de emissão e vínculo com o aluno/curso. A matrícula muda para `COMPLETED` no mesmo fluxo transacional.
11. Certificados revogados não são retornados pela verificação pública nem reemitidos automaticamente. A interface administrativa de revogação ainda não faz parte desta etapa.
12. A consulta por código é pública e revela o nome do estudante e do curso **apenas a quem possui o código de verificação**. Não há pesquisa pública por e-mail ou nome.

## Esquema SQL

**Nenhuma nova migração:** as tabelas `assessments`, `assessment_questions`, `question_options`, `assessment_attempts`, `assessment_answers` e `certificates` já estão presentes em `V1__create_learning_platform.sql`. **Não modificar V1, V2 ou V3 já aplicadas.**

## Endpoints

| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/api/v1/instructor/courses/{courseId}/assessment` | INSTRUCTOR dono, curso RASCUNHO |
| GET | `/api/v1/instructor/courses/{courseId}/assessment` | INSTRUCTOR dono |
| GET | `/api/v1/students/me/courses/{slug}/assessment` | STUDENT matriculado |
| POST | `/api/v1/students/me/courses/{slug}/assessment/{assessmentId}/attempts` | STUDENT matriculado, aulas concluídas |
| POST | `/api/v1/students/me/courses/{slug}/certificate` | STUDENT qualificado |
| GET | `/api/v1/students/me/courses/{slug}/certificate` | STUDENT dono |
| GET | `/api/v1/certificates/verify/{verificationCode}` | Público, certificado não revogado |

Os métodos GET de avaliação/certificado do estudante retornam JSON `null` quando não existem. Não retornam gabarito. A identidade vem do JWT.

## Passo a passo local

```powershell
cd C:\Projetos\cursohub-lms-repo
git status
git fetch origin
git switch --track origin/feature/assessments-certificates
docker compose -p cursohub-lms up -d --build
docker compose -p cursohub-lms ps
curl.exe -i http://localhost:8080/actuator/health
```

Se a branch já existir, use `git switch feature/assessments-certificates` e `git pull --ff-only`. **Nunca execute `docker compose down -v`**. Seu banco e cursos atuais serão preservados.

### Teste de ponta a ponta

1. Entre como instrutor e **crie um curso NOVO em RASCUNHO**, por exemplo `cursohub-avaliacao-teste-2026`.
2. Cadastre um módulo e uma aula obrigatória de texto.
3. Em **Avaliação final do curso**, crie uma prova de 2 questões, nota mínima de 70%, máximo de 2 tentativas.
4. Marque uma alternativa correta para cada questão. Clique em **Salvar avaliação final**.
5. Publique o curso.
6. Entre com Roberta ou outra conta de estudante e matricule-se.
7. Abra o curso e confirme que ainda não é possível fazer a prova nem emitir certificado antes de concluir a aula.
8. Conclua a aula de texto. Responda a prova. Se obtiver menos de 70%, teste a segunda tentativa.
9. Após aprovação, clique em **Emitir meu certificado**, confira nome do aluno e curso. Clique em **Imprimir / Salvar como PDF** para exportar pelo navegador.
10. Abra o link **Validar autenticidade**: deve retornar os dados da emissão. Atualize a página do aluno e confirme que o certificado permanece disponível.

**Importante:** o certificado é renderizado em HTML/CSS e impresso/salvo como PDF pelo navegador. Ainda não existe geração, assinatura digital ou download de arquivo PDF pelo servidor.

## Observações de arquitetura

- `assessment/application` e `assessment/application/port`: contratos e validações sem dependências do Spring.
- `assessment/adapters/out`: consultas SQL parametrizadas e transações no PostgreSQL.
- `assessment/adapters/in`: controllers JWT e representação JSON.
- `InstructorAssessmentEditor.tsx`: autoria do questionário.
- `CourseAssessment.tsx`: tentativa, correção, certificado e layout A4 imprimível.
- Fluxo de autenticação, uploads e videoaulas das Etapas 2–7 inalterado.

## Testes automatizados

O GitHub Actions verifica compilação Maven/React, autorização JWT e cenários de PostgreSQL com avaliação inválida, duplicidade, prova bloqueada antes das aulas, nota insuficiente, segunda tentativa aprovada, bloqueio de tentativas posteriores, certificado bloqueado até aprovação, emissão idempotente e verificação pública.

## Melhorias futuras

Editor de avaliações com versão e aprovação administrativa; banco de questões; feedback pedagógico por questão; justificativas; embaralhamento; notas de exercícios dissertativos; analytics do instrutor; bloqueios antifraude; assinatura digital e geração de PDF no servidor; permissões de revogação; acessibilidade ampliada de provas.
