-- Clube CursoHub: planos e assinaturas futuras. Nenhuma matrícula/assinatura é ativada por esta migração.
-- Preços comerciais propostos em centavos de BRL. Sem gateway, os planos são informativos.
CREATE TABLE membership_plans (
    code VARCHAR(20) PRIMARY KEY,
    title VARCHAR(80) NOT NULL,
    duration_months SMALLINT NOT NULL CHECK (duration_months IN (1,6,12)),
    price_cents INTEGER NOT NULL CHECK (price_cents > 0),
    course_limit SMALLINT CHECK (course_limit IS NULL OR course_limit > 0),
    discount_percent SMALLINT NOT NULL CHECK (discount_percent BETWEEN 0 AND 30),
    benefits TEXT[] NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order SMALLINT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO membership_plans(code,title,duration_months,price_cents,course_limit,
                              discount_percent,benefits,sort_order)
VALUES
('MENSAL','Plano Mensal',1,5990,3,5,
 ARRAY['Até 3 cursos premium elegíveis durante a vigência',
       'Materiais complementares',
       'Certificados após cumprir os requisitos',
       '5% de desconto em compras avulsas elegíveis'],1),
('SEMESTRAL','Plano Semestral',6,26940,8,10,
 ARRAY['Até 8 cursos premium elegíveis durante a vigência',
       'Materiais complementares e trilhas especiais',
       'Certificados após cumprir os requisitos',
       '10% de desconto em compras avulsas elegíveis'],2),
('ANUAL','Plano Anual',12,47880,NULL,15,
 ARRAY['Acesso a todos os cursos premium elegíveis durante a vigência',
       'Materiais e trilhas especiais',
       'Certificados após cumprir os requisitos',
       '15% de desconto em compras avulsas elegíveis',
       'Acesso antecipado a novos cursos elegíveis'],3);

-- Estrutura reservada a pagamentos verificados por backend: sem endpoint de autoativação.
CREATE TABLE student_memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    plan_code VARCHAR(20) NOT NULL REFERENCES membership_plans(code),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT'
      CHECK(status IN ('PENDING_PAYMENT','ACTIVE','EXPIRED','CANCELLED')),
    charged_price_cents INTEGER NOT NULL CHECK(charged_price_cents > 0),
    provider_payment_id VARCHAR(150) UNIQUE,
    started_at TIMESTAMPTZ,
    ends_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_membership_dates CHECK (
      (status = 'PENDING_PAYMENT' AND started_at IS NULL AND ends_at IS NULL)
      OR (status <> 'PENDING_PAYMENT' AND started_at IS NOT NULL AND ends_at > started_at)
    )
);
CREATE INDEX ix_memberships_student_status ON student_memberships(student_id,status);
CREATE UNIQUE INDEX ux_memberships_one_active_per_student
    ON student_memberships(student_id) WHERE status='ACTIVE';
