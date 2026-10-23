package br.com.cursohub.learning.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record LearningModule(UUID id, String title, int position, List<Lesson> lessons) {
    public LearningModule {
        Objects.requireNonNull(id);
        Objects.requireNonNull(title);
        if (title.isBlank() || position < 1) throw new IllegalArgumentException("Módulo inválido");
        lessons = List.copyOf(lessons);
    }
}
