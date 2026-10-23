package br.com.cursohub.learning.application;

import br.com.cursohub.learning.application.exception.*;
import br.com.cursohub.learning.application.model.LessonCompletionReceipt;
import br.com.cursohub.learning.application.port.in.StudentLearningUseCase;
import br.com.cursohub.learning.application.port.out.LearningContentReaderPort;
import br.com.cursohub.learning.application.port.out.LessonProgressWriterPort;
import br.com.cursohub.learning.domain.LearningCourse;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/** Caso de uso Java 21 puro. Nenhum dado de identidade vem do cliente. */
public final class StudentLearningService implements StudentLearningUseCase {
    private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    private final LearningContentReaderPort reader;
    private final LessonProgressWriterPort progress;

    public StudentLearningService(LearningContentReaderPort reader, LessonProgressWriterPort progress) {
        this.reader = Objects.requireNonNull(reader);
        this.progress = Objects.requireNonNull(progress);
    }

    @Override
    public LearningCourse viewCourse(UUID studentId, String slug) {
        Objects.requireNonNull(studentId, "Identidade autenticada obrigatória");
        validateSlug(slug);
        return reader.findEnrolledCourse(studentId, slug)
                .orElseThrow(LearningCourseUnavailableException::new);
    }

    @Override
    public LessonCompletionReceipt completeLesson(UUID studentId, String slug, UUID lessonId) {
        Objects.requireNonNull(studentId, "Identidade autenticada obrigatória");
        Objects.requireNonNull(lessonId, "ID da aula obrigatório");
        validateSlug(slug);
        return progress.markCompleted(studentId, slug, lessonId)
                .orElseThrow(LessonUnavailableException::new);
    }

    private static void validateSlug(String slug) {
        if (slug == null || slug.length() > 160 || !SLUG.matcher(slug).matches())
            throw new InvalidLearningRequestException();
    }
}
