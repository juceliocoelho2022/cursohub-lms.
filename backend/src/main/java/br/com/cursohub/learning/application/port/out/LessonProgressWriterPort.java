package br.com.cursohub.learning.application.port.out;

import br.com.cursohub.learning.application.model.LessonCompletionReceipt;
import java.util.Optional;
import java.util.UUID;

public interface LessonProgressWriterPort {
    Optional<LessonCompletionReceipt> markCompleted(UUID studentId, String slug, UUID lessonId);
}
