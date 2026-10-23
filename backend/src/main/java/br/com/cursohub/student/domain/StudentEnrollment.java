package br.com.cursohub.student.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Leitura de matrícula isolada do Spring e do SQL. */
public record StudentEnrollment(
        UUID enrollmentId, UUID courseId, String courseSlug, String courseTitle,
        String category, EnrollmentStatus status, int requiredLessons,
        int completedLessons, Instant enrolledAt) {
    public StudentEnrollment {
        Objects.requireNonNull(enrollmentId);
        Objects.requireNonNull(courseId);
        Objects.requireNonNull(courseSlug);
        Objects.requireNonNull(courseTitle);
        Objects.requireNonNull(status);
        Objects.requireNonNull(enrolledAt);
        if (requiredLessons < 0 || completedLessons < 0 || completedLessons > requiredLessons)
            throw new IllegalArgumentException("Contagens de aulas inconsistentes");
    }

    public int progressPercent() {
        if (requiredLessons == 0) return 0;
        return (int) Math.round(completedLessons * 100.0 / requiredLessons);
    }
}
