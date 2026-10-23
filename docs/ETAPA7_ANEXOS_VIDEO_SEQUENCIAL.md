# CursoHub LMS — Etapa 7: PDFs, imagens, ZIP e videoaulas sequenciais

**Implementação em avaliação:** `feature/course-assets-video-gating`, baseada em `feature/instructor-studio`. Não foi integrada à `main`.

## O que o instrutor faz

No **Estúdio do Instrutor**, selecione um curso e abra **Materiais do curso**. Envie arquivos PDF, PNG, JPG/JPEG, WEBP ou ZIP (máximo 30 MiB). Esses arquivos ficam associados ao curso e poderão ser baixados por alunos matriculados, inclusive em cursos já publicados.

Para uma **videoaula controlada**, crie uma aula do tipo `VIDEO`, informe a duração real do vídeo em segundos, selecione essa aula na seção **Vídeo para uma aula** e envie o MP4 ou WEBM (máximo 30 MiB). O conteúdo fica no servidor, não é um link público.

Não é permitido publicar um curso que tenha videoaulas sem o arquivo associado. Não há upload de vídeos de terceiros via URL: um URL externo não permite registrar com confiança o progresso no player integrado.

## O que o aluno faz

No painel, acesse o curso matriculado:

1. A seção **Materiais complementares** exibe os PDFs, imagens e ZIPs disponíveis para download.
2. O player de videoaulas carrega o vídeo por um endpoint autenticado, sem expor o JWT em URL.
3. A reprodução envia relatórios sequenciais ao backend, que contabiliza apenas pequenos avanços compatíveis com o tempo decorrido no servidor.
4. Quando o progresso confirmado alcança **100%**, o backend registra a conclusão da videoaula e desbloqueia as próximas aulas.
5. A tentativa de completar manualmente uma aula do tipo `VIDEO` é recusada. O backend também recusa concluir aulas posteriores antes das obrigatórias precedentes.

**Importante:** não existe mecanismo infalível para determinar se uma pessoa realmente prestou atenção ao vídeo. O mecanismo mede reprodução sequencial reportada pelo navegador e limitada pelo relógio do servidor. A verificação não equivale a DRM/proctoring ou prevenção absoluta de fraude.

## Segurança e persistência

- Usuário instrutor: endpoints `/api/v1/instructor/**` (perfil `INSTRUCTOR`) com verificação de propriedade de curso.
- Usuário estudante: endpoints `/api/v1/students/me/courses/{slug}/assets/**` (perfil `STUDENT`), limitados a matrícula `ACTIVE`/`COMPLETED`.
- Download de vídeo de aula bloqueada também é impedido no backend.
- Os downloads exigem `Authorization: Bearer`, sem token na URL ou no `localStorage`.
- Os arquivos ficam na coluna `BYTEA` de `course_assets` da migração Flyway V3. Essa estratégia é adequada **somente para pequenos arquivos em ambiente MVP**. Arquivos maiores e produção exigem armazenamento de objetos (S3/MinIO), antivírus, cota e URLs temporárias.
- A assinatura inicial do arquivo é conferida (não apenas a extensão). Os arquivos jamais são extraídos ou executados pelo backend; são enviados como **attachment**, com `nosniff`.
- Não execute `docker compose down -v`. O volume `cursohub_pgdata` mantém os dados.
- A substituição de videoaula precisa preservar histórico; em produção deve usar versionamento e migração de progresso.

## APIs

| Método | Endpoint | Papel |
|---|---|---|
| POST multipart | `/api/v1/instructor/courses/{id}/assets` | INSTRUCTOR dono |
| POST multipart | `/api/v1/instructor/courses/{id}/assets?lessonId={id}` | INSTRUCTOR dono; vídeo |
| GET | `/api/v1/instructor/courses/{id}/assets` | INSTRUCTOR dono |
| GET | `/api/v1/students/me/courses/{slug}/assets` | STUDENT matriculado |
| GET | `/api/v1/students/me/courses/{slug}/assets/{id}` | STUDENT matriculado e aula liberada |
| POST | `/api/v1/students/me/courses/{slug}/lessons/{id}/video-progress` | STUDENT matriculado e aula liberada |

Resposta do progresso de vídeo: `watchedSeconds`, `totalSeconds`, `percent` e `completed`.

## Teste local

```powershell
cd C:\Projetos\cursohub-lms-repo
git status
git fetch origin
git switch --track origin/feature/course-assets-video-gating
docker compose -p cursohub-lms up -d --build
docker compose -p cursohub-lms ps
```

Se já existir a branch local, use `git switch feature/course-assets-video-gating` e `git pull --ff-only`.

Teste primeiro um PDF e uma imagem pequenos; confirme download como estudante matriculado. Em um curso de teste em **RASCUNHO**, crie 3 aulas obrigatórias: TEXT, VIDEO (duração real) e TEXT. Anexe o vídeo, publique, matricule aluno e confira que a terceira aula permanece bloqueada até conclusão da primeira e 100% do vídeo.

Para evitar grandes arquivos no PostgreSQL, respeite **30 MiB por arquivo** e não faça uploads de vídeos grandes nesta versão.

## Compatibilidade com o catálogo demonstrativo existente

O ambiente `compose.yaml` utiliza `FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/demo`. O catálogo demonstrativo ocupa **V2** (`db/demo/V2__demo_catalog.sql`); o upload de materiais utiliza **V3** (`db/migration/V3__course_assets_video_progress.sql`). Mantenha ambas as versões: não renomeie nem remova a V2 já aplicada, e não execute `flyway repair` ou apague o volume para resolver conflitos de versão.

Em um banco já existente, o resultado após iniciar deve ser:

```text
V1 create learning platform  success
V2 demo catalog             success
V3 course assets video progress success
```

Validação segura no PowerShell:

```powershell
git pull --ff-only origin feature/course-assets-video-gating
docker compose -p cursohub-lms up -d --build backend frontend
docker compose -p cursohub-lms ps
curl.exe -i http://localhost:8080/actuator/health
docker compose -p cursohub-lms exec -T db psql -U cursohub -d cursohub -c "SELECT version,description,success FROM flyway_schema_history ORDER BY installed_rank;"
```
