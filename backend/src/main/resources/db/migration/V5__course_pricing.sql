-- Preço nominal em centavos de BRL; cursos existentes permanecem GRATUITOS por padrão.
-- Pagamento/checkout NÃO está implementado: matrícula de curso pago será bloqueada.
ALTER TABLE courses
  ADD COLUMN price_cents INTEGER NOT NULL DEFAULT 0
  CONSTRAINT ck_course_price_cents CHECK (price_cents BETWEEN 0 AND 2000000);

-- Somente catálogo de demonstração recebe preços ilustrativos para a home.
-- Contas, matrículas, aulas e progresso dos estudantes não são alterados.
UPDATE courses SET price_cents=7990
 WHERE id='22222222-2222-4222-8222-222222222221'
   AND instructor_id='11111111-1111-4111-8111-111111111111';
UPDATE courses SET price_cents=6990
 WHERE id='22222222-2222-4222-8222-222222222222'
   AND instructor_id='11111111-1111-4111-8111-111111111111';
UPDATE courses SET price_cents=12990
 WHERE id='22222222-2222-4222-8222-222222222223'
   AND instructor_id='11111111-1111-4111-8111-111111111111';
-- Matemática para Programadores: oferta de entrada com preco moderado.
UPDATE courses SET price_cents=4990
 WHERE id='22222222-2222-4222-8222-222222222224'
   AND instructor_id='11111111-1111-4111-8111-111111111111';
-- Cursos criados pelo instrutor mantem 0 centavos ate que ele configure um preco.

