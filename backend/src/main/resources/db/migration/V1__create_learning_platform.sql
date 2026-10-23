-- CursoHub: esquema principal do LMS. Sem dados de demonstração neste arquivo.
CREATE TABLE app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'INSTRUCTOR', 'STUDENT')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_user_email_nonempty CHECK (length(trim(email)) > 3)
);
CREATE UNIQUE INDEX ux_app_users_email_ci ON app_users (lower(email));

CREATE TABLE courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instructor_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    slug VARCHAR(160) NOT NULL UNIQUE,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(60) NOT NULL,
    level VARCHAR(20) NOT NULL CHECK (level IN ('INICIANTE', 'INTERMEDIARIO', 'AVANCADO')),
    status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO'
        CHECK (status IN ('RASCUNHO', 'PUBLICADO', 'ARQUIVADO')),
    estimated_minutes INTEGER NOT NULL DEFAULT 0 CHECK (estimated_minutes >= 0),
    thumbnail_url VARCHAR(500),
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_course_published_at CHECK (status <> 'PUBLICADO' OR published_at IS NOT NULL),
    CONSTRAINT ck_course_slug CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);
CREATE INDEX ix_courses_status_category_level ON courses(status, category, level);
CREATE INDEX ix_courses_instructor ON courses(instructor_id);

CREATE TABLE course_modules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_module_course_position UNIQUE (course_id, position)
);

CREATE TABLE lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id UUID NOT NULL REFERENCES course_modules(id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    kind VARCHAR(15) NOT NULL CHECK (kind IN ('VIDEO', 'TEXT', 'PDF')),
    content_url VARCHAR(1000),
    body_text TEXT,
    duration_seconds INTEGER NOT NULL DEFAULT 0 CHECK (duration_seconds >= 0),
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    is_preview BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_lesson_module_position UNIQUE (module_id, position)
);
CREATE INDEX ix_lessons_module ON lessons(module_id);

CREATE TABLE enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
    status VARCHAR(15) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT ux_enrollment_student_course UNIQUE (student_id, course_id),
    CONSTRAINT ck_enrollment_completed_at CHECK (status <> 'COMPLETED' OR completed_at IS NOT NULL)
);
CREATE INDEX ix_enrollments_course ON enrollments(course_id);

CREATE TABLE lesson_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE RESTRICT,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_progress_enrollment_lesson UNIQUE (enrollment_id, lesson_id)
);
CREATE INDEX ix_progress_enrollment ON lesson_progress(enrollment_id);

CREATE TABLE assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    passing_score NUMERIC(5,2) NOT NULL DEFAULT 70
        CHECK (passing_score BETWEEN 0 AND 100),
    max_attempts INTEGER NOT NULL DEFAULT 3 CHECK (max_attempts >= 1),
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_assessments_course ON assessments(course_id);

CREATE TABLE assessment_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    statement TEXT NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    points NUMERIC(8,2) NOT NULL DEFAULT 1 CHECK (points > 0),
    CONSTRAINT ux_question_assessment_position UNIQUE (assessment_id, position)
);

CREATE TABLE question_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ux_option_question_position UNIQUE (question_id, position)
);
-- O modelo inicial usa uma única alternativa correta por questão.
CREATE UNIQUE INDEX ux_one_correct_option_per_question
    ON question_options(question_id) WHERE is_correct;

CREATE TABLE assessment_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID NOT NULL REFERENCES assessments(id) ON DELETE RESTRICT,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE RESTRICT,
    attempt_number INTEGER NOT NULL CHECK (attempt_number > 0),
    score NUMERIC(5,2) CHECK (score BETWEEN 0 AND 100),
    passed BOOLEAN,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    submitted_at TIMESTAMPTZ,
    CONSTRAINT ux_attempt_enrollment_assessment_number
        UNIQUE (enrollment_id, assessment_id, attempt_number),
    CONSTRAINT ck_attempt_result_complete
        CHECK ((submitted_at IS NULL AND score IS NULL AND passed IS NULL)
            OR (submitted_at IS NOT NULL AND score IS NOT NULL AND passed IS NOT NULL))
);
CREATE INDEX ix_attempts_enrollment ON assessment_attempts(enrollment_id);

CREATE TABLE assessment_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL REFERENCES assessment_attempts(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES assessment_questions(id) ON DELETE RESTRICT,
    selected_option_id UUID NOT NULL REFERENCES question_options(id) ON DELETE RESTRICT,
    CONSTRAINT ux_answer_attempt_question UNIQUE (attempt_id, question_id)
);

CREATE TABLE certificates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id UUID NOT NULL UNIQUE REFERENCES enrollments(id) ON DELETE RESTRICT,
    verification_code UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    issued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at TIMESTAMPTZ
);
