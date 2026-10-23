package br.com.cursohub.learning.application.port.in;

import br.com.cursohub.learning.application.model.LessonCompletionReceipt;
import br.com.cursohub.learning.domain.LearningCourse;
import java.util.UUID;

public interface StudentLearningUseCase {
    LearningCourse viewCourse(UUID authenticatedStudentId, String slug);
    LessonCompletionReceipt completeLesson(UUID authenticatedStudentId, String slug, UUID lessonId);
}
