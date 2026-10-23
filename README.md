# CursoHub LMS — Plataforma de Cursos Online

Plataforma **Learning Management System (LMS)** com **Clean Architecture + Arquitetura Hexagonal (Ports & Adapters)**, construída com **Java 21**, **Spring Boot 4.1.2**, **PostgreSQL 17**, **Flyway**, **React 19**, **TypeScript**, **Vite 7** e **Docker Compose**.

> **Status: Etapa 1 — Fundação + Catálogo público.** O projeto contém código-fonte de backend, frontend, schema SQL, dados demonstrativos, testes e documentação. Login, matrículas e aulas privadas ainda não estão implementados.

## Estrutura

```text
cursohub-lms/
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/br/com/cursohub/
│       │   ├── CursoHubApplication.java
│       │   ├── catalog/
│       │   │   ├── domain/             # Regras Java puras
│       │   │   ├── application/        # Casos de uso e portas
│       │   │   ├── adapters/in/web/    # Controller REST
│       │   │   ├── adapters/out/       # JPA / PostgreSQL
│       │   │   └── config/            # Injeção de dependências
│       │   └── shared/api/            # Tratamento de erros HTTP
│       ├── main/resources/db/
│       │   ├── migration/             # Flyway: schema
│       │   └── demo/                  # Seed local, não produção
│       └── test/                      # JUnit / fake repository
├── frontend/
│   ├── src/                           # React e TypeScript
│   ├── nginx/default.conf
│   └── Dockerfile
├── docs/
│   ├── ARQUITETURA.md
│   ├── GUIA_PRATICO_VIDEO.md
│   ├── REGRAS_DE_NEGOCIO.md
│   └── ROADMAP.md
├── scripts/check_architecture.py
├── compose.yaml
├── .env.example
└── .gitignore
```

## Fluxo Clean + Hexagonal

`React → CatalogController → CatalogQueryUseCase → CatalogService → CourseReaderPort → CoursePostgresAdapter → JPA → PostgreSQL`

O domínio `Course` não utiliza `@Entity`. O mapeamento do banco fica isolado em `JpaCourseEntity`. Testes unitários exercitam o caso de uso sem conectar ao PostgreSQL.

## Como executar com Docker

Pré-requisitos: **Docker Desktop** com Compose e portas **3000**, **8080** e **5432** disponíveis. Na pasta clonada:

```powershell
Copy-Item .env.example .env
# Substitua a senha de demonstração por uma senha local segura no .env
docker compose up --build -d
docker compose ps
```

- **Frontend**: http://localhost:3000
- **API de cursos**: http://localhost:8080/api/v1/courses
- **Healthcheck**: http://localhost:8080/actuator/health

O Compose utiliza volume de dados PostgreSQL e ativa um **seed local** de quatro cursos fictícios. Não habilite `db/demo` em produção.

```powershell
docker compose logs -f backend
docker compose down
```

**Cuidado:** `docker compose down -v` também apaga o volume de dados.

## Desenvolvimento local

Pré-requisitos: **Java 21**, **Maven**, **Node.js 22**, **npm** e PostgreSQL.

```powershell
Copy-Item .env.example .env
docker compose up -d db
$env:DB_URL = "jdbc:postgresql://localhost:5432/cursohub"
$env:DB_USER = "cursohub"
$env:DB_PASSWORD = "<defina-a-mesma-senha-do-arquivo-env>"
$env:FLYWAY_LOCATIONS = "classpath:db/migration,classpath:db/demo"
cd backend
mvn spring-boot:run
```

Em outro terminal:

```powershell
cd frontend
npm install
npm run dev
```

O Vite abre em `http://localhost:5173` e encaminha `/api` para o Spring Boot.

## Endpoints

| Método | Endpoint | Finalidade |
|---|---|---|
| GET | `/api/v1/courses` | Busca paginada de cursos publicados |
| GET | `/api/v1/courses/{slug}` | Detalhes de curso publicado |
| GET | `/actuator/health` | Estado da API |

Filtros: `q`, `categoria`, `nivel` (`INICIANTE`, `INTERMEDIARIO`, `AVANCADO`), `page` e `size` (1–48).

## Testes e qualidade

```powershell
python scripts/check_architecture.py
cd backend
mvn test
cd ../frontend
npm install
npm run build
```

A verificação de fronteiras arquiteturais passou no ambiente de geração. A compilação integral e o funcionamento integrado exigem uma execução com Maven, dependências npm e Docker no ambiente local ou no CI.

## Segurança

- **Não versionar** `.env`, tokens, chaves ou senhas de produção.
- A identidade fictícia no seed está inativa, com senha inutilizável.
- A porta PostgreSQL está restrita a `127.0.0.1` no Compose.
- **Não existem endpoints de escrita sem autenticação** na primeira etapa.
- Antes de produção: Spring Security, JWT, RBAC, proteção de mídia, LGPD, backups, logs e monitoramento.

## Próximos passos

1. Executar a stack, validar banco, endpoints, interface e testes.
2. Desenvolver cadastro de usuários, autenticação e autorização.
3. Implementar cursos, módulos, aulas e matrículas.
4. Construir avaliações, progresso e certificados.

**Documentação principal:** [Arquitetura](docs/ARQUITETURA.md) · [Regras de negócio](docs/REGRAS_DE_NEGOCIO.md) · [Guia prático do vídeo](docs/GUIA_PRATICO_VIDEO.md) · [Roadmap](docs/ROADMAP.md).
