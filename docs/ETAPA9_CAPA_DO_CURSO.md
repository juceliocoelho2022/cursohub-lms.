# CursoHub LMS — Etapa 9: Imagem de capa do curso

**Branch:** `feature/course-cover-upload` | **PR:** #8 (rascunho), baseada em `main`. Implementada separadamente da Etapa 8, que ainda está no PR #7. **Não faça merge até testar no navegador.**

## Uso pelo instrutor

1. Entre no **Estúdio do Instrutor** e procure a seção **Criar curso**.
2. Preencha nome, slug, descrição, categoria, nível e duração estimada.
3. Em **Imagem de capa (opcional)**, selecione uma imagem de seu computador.
4. A prévia aparece em proporção **16:9**. Use o banner **Java Básico — Primeiros passos na programação** como exemplo.
5. Clique em **Criar rascunho +**. O CursoHub cria o curso e envia a imagem automaticamente.
6. Para trocar a capa depois, selecione o curso em **Meus cursos**, vá a **Imagem de capa do curso** e clique em **Salvar imagem de capa** após escolher o novo arquivo. A troca está disponível para rascunhos e publicados; conteúdos e matrículas não são alterados.
7. Quando o curso for publicado, a imagem aparece nos cartões do catálogo **Explorar cursos** e nos detalhes do curso.

**A capa é opcional.** Se não houver imagem, o catálogo continua mostrando a arte ilustrativa de acordo com a categoria.

## Regras e segurança

- **JPG/JPEG, PNG e WEBP** (não aceita SVG).
- **Limite 5 MiB** por imagem. A extensão e a assinatura binária são validadas pelo backend; o navegador faz validação básica prévia.
- Apenas o **instrutor dono** do curso pode enviar ou consultar a imagem privada.
- A capa do rascunho não fica pública. `GET /api/v1/courses/{slug}/cover` responde HTTP 404 até que o curso seja `PUBLICADO`.
- `GET /api/v1/instructor/courses/{id}/cover` exige JWT com role INSTRUCTOR e propriedade do curso.
- A imagem é servida como MIME validado e `X-Content-Type-Options: nosniff`, sem cache.
- Curso/slug permanecem independentes do nome do arquivo; as capas não são arquivos anexos para alunos, mas identidade visual pública do curso publicado.
- O navegador **não** coloca o JWT em URL e não precisa autenticar para exibir capas já publicadas.
- A gravação usa a tabela `course_cover_images` (BYTEA, uma capa por curso) adicionada pela migração **V4**; `courses.thumbnail_url` aponta para URL estável de leitura pública.
- O método de atualização usa `INSERT ... ON CONFLICT DO UPDATE` sob bloqueio da linha do curso no PostgreSQL.
- **Não modifique as migrações V1, V2 ou V3 já aplicadas nem execute `docker compose down -v`.**
- Imagem corrompida ou conteúdo potencialmente enganoso além da assinatura inicial exige validação avançada/antivírus e transcodificação antes de servir em produção. O armazenamento BYTEA é uma solução MVP; para escala, usar S3/MinIO, CDN e processamento de thumbnails.

## Endpoints

| Método | Endpoint | Quem pode |
|---|---|---|
| POST multipart | `/api/v1/instructor/courses/{courseId}/cover` | INSTRUCTOR dono |
| GET | `/api/v1/instructor/courses/{courseId}/cover` | INSTRUCTOR dono |
| GET | `/api/v1/courses/{slug}/cover` | Público, somente se publicado |

### Homologação local em Windows PowerShell

```powershell
cd C:\Projetos\cursohub-lms-repo
git status
git fetch origin
git switch --track origin/feature/course-cover-upload
docker compose -p cursohub-lms up -d --build
docker compose -p cursohub-lms ps
curl.exe -i http://localhost:8080/actuator/health
```

Se já houver branch local, use `git switch feature/course-cover-upload` e `git pull --ff-only origin feature/course-cover-upload`. Não sobrescreva alterações locais.

Confirme Flyway:

```powershell
docker compose -p cursohub-lms exec -T db psql -U cursohub -d cursohub -c "SELECT version,description,success FROM flyway_schema_history ORDER BY installed_rank;"
```

Esperadas: V1, V2 demo, V3 media e V4 course cover images, todas com `success=t`.

### Teste em um curso novo

Crie `Java Básico — Primeiros passos` com slug `java-basico-primeiros-passos-2026`, anexe a capa enviada e clique **Criar rascunho +**. Confirme a prévia no editor. Em **Estrutura do curso**, cadastre ao menos uma aula obrigatória; publique. Abra **Explorar cursos** e veja a capa no cartão e na janela de detalhes. Faça um segundo upload no editor, depois confirme a nova imagem no catálogo. Teste também um arquivo maior que 5 MB e um arquivo que não seja uma imagem real; o sistema deve rejeitá-los. Verifique que cursos antigos sem capa continuam normais.

## CI

GitHub Actions executa `mvn verify`, build React, testes com PostgreSQL 17 e JWT, validação de assinatura PNG, HTTP 400 para upload inválido, autorização de rascunhos, disponibilização de imagem após publicação e compatibilidade Flyway V4.
