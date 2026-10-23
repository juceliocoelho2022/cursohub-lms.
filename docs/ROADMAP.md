# Roadmap do CursoHub LMS

## Etapa 1 — Fundação + catálogo
- Estrutura modular para Java 21 / Spring Boot
- Domínio e casos de uso independentes de frameworks
- Portas de entrada/saída e adaptadores REST/JPA
- PostgreSQL + migrações Flyway
- Frontend React + TypeScript
- Ambiente Docker Compose
- Testes unitários do núcleo

## Etapa 2 — Identidade e acesso
- Cadastro e login
- Senhas com hash seguro
- JWT e refresh token
- Autorização por papéis: ADMIN, INSTRUTOR, ALUNO

## Etapa 3 — Cursos e matrículas
- Gestão de cursos, módulos, aulas e materiais
- Publicação de cursos
- Matrículas com prevenção de duplicidade
- Acompanhamento de conclusão de aulas

## Etapa 4 — Avaliações e certificados
- Questionários e tentativas
- Regras configuráveis de aprovação
- Certificado com código verificável
- Dashboard e relatórios

## Qualidade e entrega
- Testes JUnit e integração
- CI no GitHub Actions
- Validação E2E
- Observabilidade e deploy

A arquitetura deve manter dependências voltadas para o núcleo da aplicação.
