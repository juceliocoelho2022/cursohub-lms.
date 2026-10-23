# CursoHub LMS — Plataforma de Cursos Online

Projeto educacional de uma plataforma **Learning Management System (LMS)**, desenvolvido com Java 21, Spring Boot 4.1.2, React, TypeScript, PostgreSQL, Flyway e Docker Compose.

## Objetivo

Construir, por etapas, uma solução com catálogo de cursos, área do aluno, instrutores, matrículas, avaliações e emissão de certificados.

## Arquitetura

**Clean Architecture + Arquitetura Hexagonal (Ports & Adapters)**.

```text
React -> REST Controller -> Porta de Entrada -> Caso de Uso -> Porta de Saída -> Adaptador JPA -> PostgreSQL
```

O domínio não deve depender de Spring, JPA, controllers ou banco de dados.

## Estado do projeto

**Etapa 1 — Fundação e catálogo público.** A primeira versão do código-fonte foi preparada localmente; sua publicação integral neste repositório ainda está pendente. Login, matrículas, avaliações e certificados são funcionalidades planejadas, não prontas.

## Stack

- Java 21, Spring Boot 4.1.2, Maven
- Spring Data JPA, PostgreSQL 17, Flyway
- React 19, TypeScript, Vite 7
- Docker Compose, JUnit

## Como executar (após a publicação integral do código)

```powershell
Copy-Item .env.example .env
docker compose up --build -d
docker compose ps
```

Acessos locais planejados: React em http://localhost:3000, API em http://localhost:8080/api/v1/courses e Actuator em http://localhost:8080/actuator/health.

## Roadmap

- [x] Definir arquitetura e modelagem inicial
- [x] Preparar os arquivos da Etapa 1 localmente
- [ ] Publicar o código-fonte completo neste repositório
- [ ] Validar backend, frontend e PostgreSQL integrados
- [ ] Autenticação, RBAC e área de gestão de cursos
- [ ] Matrículas, avaliações, progresso e certificados
- [ ] Integração contínua, testes E2E e deploy

> Atenção: nunca versione o arquivo `.env`, senhas reais ou artefatos de build.

Projeto mantido por Jucelio Coelho.
