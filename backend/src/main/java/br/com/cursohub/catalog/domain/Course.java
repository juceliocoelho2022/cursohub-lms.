package br.com.cursohub.catalog.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Entidade de domínio: não depende de Spring, Hibernate ou HTTP. */
public record Course(
        UUID id, String slug, String title, String description,
        String category, CourseLevel level, CourseStatus status,
        UUID instructorId, int estimatedMinutes, String thumbnailUrl,
        Instant publishedAt) {
    public Course {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(slug, "slug");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(status, "status");
        if (estimatedMinutes < 0) throw new IllegalArgumentException("Duração não pode ser negativa");
        if (status == CourseStatus.PUBLICADO && publishedAt == null)
            throw new IllegalArgumentException("Curso publicado exige data de publicação");
    }

    public boolean isPublished() {
        return status == CourseStatus.PUBLICADO;
    }
}
