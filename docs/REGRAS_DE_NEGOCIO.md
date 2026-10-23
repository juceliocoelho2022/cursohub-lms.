# CursoHub LMS — Regras de negócio

**Legenda:** [BD] regra aplicada no PostgreSQL; [APP] caso de uso a implementar futuramente.

## Identidade

- RN-001 [BD] E-mail único, sem distinção de maiúsculas/minúsculas. Perfil: ADMIN, INSTRUCTOR ou STUDENT.
- RN-002 [APP] Somente usuário ativo pode autenticar; senhas serão hashes robustos com salt.
- RN-003 [APP] Instrutor modifica apenas seus próprios cursos; administrador pode moderar os demais.

## Cursos

- RN-004 [BD] Curso tem slug único minúsculo com hífens e instrutor responsável.
- RN-005 [BD] Estados: RASCUNHO, PUBLICADO, ARQUIVADO; publicado requer data.
- RN-006 [APP] Catálogo público exibe somente cursos publicados. **Implementado na Etapa 1.**
- RN-007 [APP] Publicar exige ao menos um módulo, uma aula válida e revisão de conteúdo.
- RN-008 [BD] Posições de módulos e de aulas são únicas dentro de cada pai.
- RN-009 [APP] Aulas privadas exigem matrícula ativa e mídia protegida.

## Matrículas e progresso

- RN-010 [BD] Aluno não pode ter mais de uma matrícula no mesmo curso.
- RN-011 [APP] Matrícula exige usuário ativo do perfil estudante e curso publicado.
- RN-012 [APP] Repetir matrícula não cria duplicidade (idempotência).
- RN-013 [BD] Conclusão de aula é única por matrícula.
- RN-014 [APP] Aula concluída deve pertencer ao curso; operação idempotente.
- RN-015 [APP] Progresso = conclusões obrigatórias / total obrigatório × 100. Zero aulas não significa 100%.

## Avaliações e certificados

- RN-016 [BD] Nota mínima entre 0 e 100 e tentativas máximas ≥ 1.
- RN-017 [BD] Número de tentativa único por matrícula e avaliação.
- RN-018 [APP] Avaliação pertence ao curso da matrícula e deve respeitar limite de tentativas.
- RN-019 [BD/APP] No máximo uma resposta correta no banco; serviço exigirá exatamente uma e no mínimo duas opções para publicar.
- RN-020 [APP] Certificado exige aulas obrigatórias concluídas e avaliações obrigatórias aprovadas.
- RN-021 [BD] Apenas um certificado por matrícula, com código único.
- RN-022 [APP] Emissão idempotente, validação pública por código sem expor dados indevidos.

## Privacidade e auditoria

- RN-023 [APP] Não expor gabarito antes da submissão.
- RN-024 [APP] Impedir acesso de aluno aos registros de outro aluno.
- RN-025 [APP] Auditar alterações de conteúdo e certificados.

## Critérios de aceite — Etapa 1

- AC-01 Compose inicia PostgreSQL, Spring Boot e React.
- AC-02 Flyway cria schema; Hibernate só valida mapeamento.
- AC-03 Catálogo somente de publicados, com filtros, paginação e detalhes.
- AC-04 Não há botões de matrícula e login fictícios.
- AC-05 Testes unitários de catálogo, paginação e não-encontrado.
- AC-06 Seed demo separado e habilitado apenas no Compose local.

A integração ponta a ponta ainda requer validação em ambiente com Docker.
