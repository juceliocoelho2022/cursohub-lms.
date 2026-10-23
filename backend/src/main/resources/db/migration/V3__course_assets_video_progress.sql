-- Materiais binários pequenos do curso, preservados no volume PostgreSQL atual.
CREATE TABLE course_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    lesson_id UUID REFERENCES lessons(id) ON DELETE RESTRICT,
    filename VARCHAR(180) NOT NULL,
    media_type VARCHAR(60) NOT NULL
      CHECK (media_type IN ('application/pdf','image/png','image/jpeg','image/webp','application/zip','video/mp4','video/webm')),
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0 AND size_bytes <= 31457280),
    contents BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_course_assets_course ON course_assets(course_id);
CREATE UNIQUE INDEX ux_course_assets_lesson_video ON course_assets(lesson_id)
  WHERE lesson_id IS NOT NULL;
CREATE TABLE video_playback_progress (
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE RESTRICT,
    watched_seconds INTEGER NOT NULL DEFAULT 0 CHECK (watched_seconds >= 0),
    last_reported_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY(enrollment_id, lesson_id)
);
