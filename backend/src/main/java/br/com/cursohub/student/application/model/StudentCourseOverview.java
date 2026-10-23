package br.com.cursohub.student.application.model;

import br.com.cursohub.student.domain.EnrollmentStatus;
import br.com.cursohub.student.domain.StudentEnrollment;
import java.time.Instant;
import java.util.UUID;

public record StudentCourseOverview(UUID enrollmentId, UUID courseId, String slug,
                                    String title, String category, EnrollmentStatus status,
                                    int requiredLessons, int completedLessons, int progressPercent,
                                    Instant enrolledAt) {
    public static StudentCourseOverview from(StudentEnrollment enrollment) {
        return new StudentCourseOverview(
                enrollment.enrollmentId(), enrollment.courseId(), enrollment.courseSlug(),
                enrollment.courseTitle(), enrollment.category(), enrollment.status(),
                enrollment.requiredLessons(), enrollment.completedLessons(),
                enrollment.progressPercent(), enrollment.enrolledAt());
    }
}
