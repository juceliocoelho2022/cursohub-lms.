-- SOMENTE no ambiente demonstrativo. Nunca habilitar este local em produção.
INSERT INTO app_users (id, full_name, email, password_hash, role, is_active)
VALUES ('11111111-1111-4111-8111-111111111111', 'Instrutor de Demonstração',
        'demo-instrutor@cursohub.invalid', '!LOGIN_DESABILITADO!', 'INSTRUCTOR', false);

INSERT INTO courses
    (id, instructor_id, slug, title, description, category, level, status, estimated_minutes, published_at)
VALUES
    ('22222222-2222-4222-8222-222222222221', '11111111-1111-4111-8111-111111111111',
     'java-backend-do-zero', 'Java Backend: do Zero às APIs',
     'Aprenda Java 21, orientação a objetos e os fundamentos para criar APIs REST com Spring Boot.',
     'Programação', 'INICIANTE', 'PUBLICADO', 720, now()),
    ('22222222-2222-4222-8222-222222222222', '11111111-1111-4111-8111-111111111111',
     'python-para-analise-de-dados', 'Python para Análise de Dados',
     'Trabalhe com pandas, limpeza de dados, estatística descritiva e visualização de resultados.',
     'Dados', 'INICIANTE', 'PUBLICADO', 540, now()),
    ('22222222-2222-4222-8222-222222222223', '11111111-1111-4111-8111-111111111111',
     'engenharia-de-dados-na-pratica', 'Engenharia de Dados na Prática',
     'Explore pipelines, modelagem, integração e fundamentos de arquitetura de dados escalável.',
     'Dados', 'INTERMEDIARIO', 'PUBLICADO', 960, now()),
    ('22222222-2222-4222-8222-222222222224', '11111111-1111-4111-8111-111111111111',
     'matematica-para-programadores', 'Matemática para Programadores',
     'Desenvolva raciocínio lógico e domine funções, proporções e aplicações de matemática em algoritmos.',
     'Matemática', 'INICIANTE', 'PUBLICADO', 360, now());

INSERT INTO course_modules (id, course_id, title, position) VALUES
    ('33333333-3333-4333-8333-333333333331','22222222-2222-4222-8222-222222222221','Começando com Java',1),
    ('33333333-3333-4333-8333-333333333332','22222222-2222-4222-8222-222222222222','Primeiros passos com pandas',1),
    ('33333333-3333-4333-8333-333333333333','22222222-2222-4222-8222-222222222223','Pipelines de dados',1),
    ('33333333-3333-4333-8333-333333333334','22222222-2222-4222-8222-222222222224','Fundamentos de lógica',1);

INSERT INTO lessons (module_id, title, position, kind, body_text, duration_seconds, is_preview) VALUES
    ('33333333-3333-4333-8333-333333333331', 'Boas-vindas ao Java 21', 1, 'TEXT',
     'Nesta aula conheceremos o plano de estudos de Java.', 600, true),
    ('33333333-3333-4333-8333-333333333332', 'Por que aprender pandas?', 1, 'TEXT',
     'Uma introdução ao ecossistema de análise de dados.', 480, true),
    ('33333333-3333-4333-8333-333333333333', 'O que é um pipeline?', 1, 'TEXT',
     'Conheça os componentes de uma arquitetura de dados.', 720, true),
    ('33333333-3333-4333-8333-333333333334', 'Lógica e resolução de problemas', 1, 'TEXT',
     'Entenda a relação entre matemática e programação.', 540, true);
