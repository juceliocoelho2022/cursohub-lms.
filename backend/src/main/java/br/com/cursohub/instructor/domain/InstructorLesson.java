package br.com.cursohub.instructor.domain;

import br.com.cursohub.learning.domain.LessonKind;
import java.util.UUID;

public record InstructorLesson(UUID id, UUID moduleId, String title, int position,
                               LessonKind kind, String contentUrl, String bodyText,
                               int durationSeconds, boolean required) {}
