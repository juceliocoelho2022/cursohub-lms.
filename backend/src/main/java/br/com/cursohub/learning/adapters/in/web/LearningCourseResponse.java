package br.com.cursohub.learning.adapters.in.web;

import br.com.cursohub.learning.domain.LearningCourse;
import br.com.cursohub.learning.domain.LearningModule;
import java.util.List;
import java.util.UUID;

/** DTO REST: expõe explicitamente totais que são métodos calculados no domínio. */
public record LearningCourseResponse(
        UUID courseId, String slug, String title, List<LearningModule> modules,
        int requiredLessons, int completedRequiredLessons, int progressPercent) {
    public static LearningCourseResponse from(LearningCourse course) {
        return new LearningCourseResponse(
                course.courseId(), course.slug(), course.title(), course.modules(),
                course.requiredLessons(), course.completedRequiredLessons(), course.progressPercent());
    }
}
