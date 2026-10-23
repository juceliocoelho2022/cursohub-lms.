package br.com.cursohub.learning.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Conteúdo liberado exclusivamente para uma matrícula ativa ou concluída. */
public record LearningCourse(UUID courseId, String slug, String title, List<LearningModule> modules) {
    public LearningCourse {
        Objects.requireNonNull(courseId);
        Objects.requireNonNull(slug);
        Objects.requireNonNull(title);
        modules = List.copyOf(modules);
    }

    public int requiredLessons() {
        return (int) modules.stream().flatMap(m -> m.lessons().stream()).filter(Lesson::required).count();
    }

    public int completedRequiredLessons() {
        return (int) modules.stream().flatMap(m -> m.lessons().stream())
                .filter(lesson -> lesson.required() && lesson.completed()).count();
    }

    public int progressPercent() {
        int total = requiredLessons();
        return total == 0 ? 0 : (int) Math.round(completedRequiredLessons() * 100.0 / total);
    }
}
