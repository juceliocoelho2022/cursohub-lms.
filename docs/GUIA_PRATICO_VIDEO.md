# Guia prático — Clean Architecture + Arquitetura Hexagonal no CursoHub

Referência compartilhada: https://www.youtube.com/watch?v=HTrLgUKQLvs — Arquitetura Limpa x Arquitetura Hexagonal.

> Aplicação prática dos padrões ao CursoHub, não uma transcrição do vídeo.

## Conceitos aplicados

| Conceito | Exemplo no código | Responsabilidade |
|---|---|---|
| Entidade de domínio | `catalog/domain/Course.java` | Regras de publicação e duração |
| Caso de uso | `catalog/application/CatalogService.java` | Consulta e valida filtros |
| Porta de entrada | `CatalogQueryUseCase.java` | Contrato independente de HTTP |
| Porta de saída | `CourseReaderPort.java` | Contrato independente do banco |
| Adaptador de entrada | `CatalogController.java` | Traduz requisição HTTP |
| Adaptador de saída | `CoursePostgresAdapter.java` | Usa JPA e PostgreSQL |
| ORM | `JpaCourseEntity.java` | Entidade técnica da tabela |
| Composição | `CatalogConfiguration.java` | Injeção de dependências |
| Teste isolado | `CatalogServiceTest.java` | Substitui banco por fake |

## Exercício 1 — Subir localmente

No PowerShell, na raiz:

```powershell
Copy-Item .env.example .env
docker compose up --build -d
docker compose ps
```

No banco novo de demonstração, o catálogo deverá exibir quatro cursos fictícios.

```powershell
Invoke-RestMethod 'http://localhost:8080/api/v1/courses?q=java'
Invoke-RestMethod 'http://localhost:8080/api/v1/courses/java-backend-do-zero'
```

## Exercício 2 — Identificar as dependências

1. Abra `CatalogController.java`: ele depende de `CatalogQueryUseCase`, não de JPA.
2. Abra `CatalogService.java`: ele depende de `CourseReaderPort`, sem Spring.
3. Abra `CoursePostgresAdapter.java`: implementa `CourseReaderPort` usando `JpaCourseEntity`.
4. Abra `Course.java`: a regra de publicação está no domínio.
5. Abra `CatalogConfiguration.java`: observe o ponto onde as dependências são conectadas.

## Exercício 3 — Provar independência

```powershell
cd backend
mvn test
cd ..
python scripts/check_architecture.py
```

O teste usa `FakeCourseRepository`. Tente consultar com tamanho 1000 e confira a rejeição antes da leitura no banco.

## Exercício 4 — Criar curso na arquitetura hexagonal

1. Validar título, slug, duração e instrutor no domínio.
2. Criar `CreateCourseUseCase` e `CreateCourseCommand`.
3. Criar `CourseWriterPort` para salvar e consultar unicidade.
4. Implementar `CreateCourseService` puro.
5. Implementar `CourseWriterPort` no adaptador JPA com transação.
6. Criar controller POST que retorna 201 Created.
7. Testar domínio, caso de uso, persistência e HTTP.

**Não exponha rotas de escrita antes de implementar autenticação e autorização.**

## Desafio

Curso só pode ser publicado se tiver pelo menos um módulo e uma aula obrigatória. O caso de uso deve usar portas e nunca chamar JPA diretamente. Testar cenário válido e inválido.

## Critérios

- [ ] Catálogo de ponta a ponta React → Spring → PostgreSQL.
- [ ] Controller depende da porta de entrada.
- [ ] Serviço puro, sem `@Service`, `@Transactional` ou `Pageable`.
- [ ] Entidade de domínio sem `@Entity`.
- [ ] Testes unitários sem inicializar banco de dados.
