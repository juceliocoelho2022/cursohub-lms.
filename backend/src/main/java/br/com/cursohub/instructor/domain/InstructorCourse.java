package br.com.cursohub.instructor.domain;

import br.com.cursohub.catalog.domain.CourseLevel;
import br.com.cursohub.catalog.domain.CourseStatus;
import java.util.Objects;
import java.util.UUID;

public record InstructorCourse(UUID id, String slug, String title, String description,
                               String category, CourseLevel level, CourseStatus status,
                               int estimatedMinutes) {
    public InstructorCourse {
        Objects.requireNonNull(id);
        Objects.requireNonNull(slug);
        Objects.requireNonNull(title);
        Objects.requireNonNull(level);
        Objects.requireNonNull(status);
    }
}
