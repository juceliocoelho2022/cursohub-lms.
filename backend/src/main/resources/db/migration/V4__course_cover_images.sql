-- Upload de imagem de capa dos cursos. V1, V2 e V3 permanecem imutáveis.
CREATE TABLE course_cover_images (
    course_id UUID PRIMARY KEY REFERENCES courses(id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL CHECK (media_type IN ('image/jpeg','image/png','image/webp')),
    size_bytes INTEGER NOT NULL CHECK (size_bytes > 0 AND size_bytes <= 5242880),
    content BYTEA NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
