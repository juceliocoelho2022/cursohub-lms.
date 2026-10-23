package br.com.cursohub.learning.domain;

import java.util.Objects;
import java.util.UUID;

/** Aula de domínio, sem referências ao Spring ou ao JPA. */
public record Lesson(UUID id, String title, int position, LessonKind kind,
                     String contentUrl, String bodyText, int durationSeconds,
                     boolean required, boolean completed, boolean locked) {
    public Lesson(UUID id, String title, int position, LessonKind kind, String contentUrl,
                  String bodyText, int durationSeconds, boolean required, boolean completed) {
        this(id, title, position, kind, contentUrl, bodyText, durationSeconds, required, completed, false);
    }

    public Lesson {
        Objects.requireNonNull(id);
        Objects.requireNonNull(title);
        Objects.requireNonNull(kind);
        if (title.isBlank() || position < 1 || durationSeconds < 0)
            throw new IllegalArgumentException("Dados da aula inválidos");
    }
}
