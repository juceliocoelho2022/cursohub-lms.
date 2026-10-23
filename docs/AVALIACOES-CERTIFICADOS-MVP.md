# CursoHub — Avaliacoes e certificados (MVP)

## Estado atual verificado

O banco ja contem `assessments`, `assessment_questions`, `question_options`, `assessment_attempts`, `assessment_answers` e `certificates` na migracao inicial. Nao foram encontrados controllers ou servicos de avaliacao/certificados na arvore atual do backend. **Esquema existente nao significa funcionalidade implementada.**

A jornada de cadastro, matricula e conclusao de aula foi validada manualmente pelo projeto, mas a emissao de certificados nao foi testada.

## Objetivo

Permitir ao instrutor criar um questionario de multipla escolha para um curso e ao estudante matriculado responder, receber nota e, quando cumprir os requisitos, obter certificado verificavel.

## Regras de negocio

1. Instrutor so gerencia avaliacoes de cursos de sua titularidade; todas as acoes requerem JWT com permissao adequada.
2. Avaliacao pertence a um unico curso. Questao tem enunciado, peso positivo e alternativas com **uma** resposta correta.
3. Antes de publicar uma avaliacao, exigir pelo menos uma questao valida e alternativas suficientes. Nao expor `is_correct` ao estudante antes do envio.
4. Somente estudante matriculado e ativo acessa a avaliacao do seu curso.
5. Toda tentativa tem numeracao unica por aluno/matricula e avaliacao; limite de tentativas respeita `max_attempts`. Prevenir envios repetidos e corridas de concorrencia.
6. Nota calculada no backend: soma dos pontos corretos dividida pelos pontos possiveis, vezes 100, com arredondamento consistente.
7. Aprovacao depende de `passing_score` definido no banco; nao confiar em pontuacao enviada pelo cliente.
8. A conclusao do curso deve considerar aulas obrigatorias e todas as avaliacoes obrigatorias aprovadas; definir explicitamente transicao de `ACTIVE` para `COMPLETED`.
9. Certificado so pode ser emitido para matricula concluida; emissao deve ser idempotente para evitar certificados duplicados.
10. Verificacao publica usa identificador aleatorio e mostra apenas dados necessarios para validar autenticidade, sem expor e-mail nem dados privados.
11. Certificados emitidos preservam dados historicos (nome do aluno, curso e data) de forma auditavel, mesmo que titulo do curso mude.
12. Falhas retornam mensagens de dominio claras (403, 404, 409 ou 422, conforme o caso). Registrar logs sem respostas de prova antes da submissao nem tokens.

## Etapas de implementacao

### Fase A — Avaliacoes
- Casos de uso Java puros, adaptadores JPA e endpoints REST para instrutor e aluno.
- DTOs separados para o instrutor e estudante, omitindo gabarito no acesso do estudante.
- Criacao/edicao/publicacao de avaliacoes no Estudio do Instrutor.
- Execucao do quiz na sala de aprendizagem com resultado e tentativas restantes.
- Testes de ownership, autorizacao, nota ponderada, limite de tentativas, duplicidade e concorrencia.

### Fase B — Certificados
- Servico de elegibilidade e conclusao de matricula.
- Emissao unica, codigo verificavel e consulta publica com dados minimizados.
- Renderizacao de certificado PDF e botao no painel do aluno.
- Testes de elegibilidade, idempotencia, vazamento de dados e validacao por codigo.

## Criterios de aceite

- [ ] Instrutor configura um quiz e publica sem vazar gabarito.
- [ ] Aluno nao matriculado nao consegue enviar respostas.
- [ ] Nota e aprovacao calculadas pelo backend e persistidas.
- [ ] Limite de tentativas e concorrencia verificados por testes automatizados.
- [ ] Aulas e avaliacoes obrigatorias governam a conclusao.
- [ ] Certificado nao pode ser emitido antecipadamente.
- [ ] Certificado final pode ser validado por codigo sem revelar informacoes privadas.
- [ ] Build React, Maven e integracao PostgreSQL passam no CI.
- [ ] Homologacao manual no CursoHub Docker local com aluno e instrutor reais de teste.

## Nota de seguranca

Nao habilitar pagamentos nem certificados validos comercialmente sem a politica pedagogica, LGPD, autenticidade e requisitos institucionais revisados.
