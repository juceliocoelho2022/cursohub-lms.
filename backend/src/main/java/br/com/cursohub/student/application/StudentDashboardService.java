package br.com.cursohub.student.application;

import br.com.cursohub.student.application.model.StudentCourseOverview;
import br.com.cursohub.student.application.model.StudentDashboard;
import br.com.cursohub.student.application.port.in.ViewStudentDashboardUseCase;
import br.com.cursohub.student.application.port.out.StudentEnrollmentReaderPort;
import br.com.cursohub.student.domain.EnrollmentStatus;
import java.util.Objects;
import java.util.UUID;

/** Caso de uso puro. A consulta filtra pelo estudante identificado pelo JWT. */
public final class StudentDashboardService implements ViewStudentDashboardUseCase {
    private final StudentEnrollmentReaderPort enrollments;

    public StudentDashboardService(StudentEnrollmentReaderPort enrollments) {
        this.enrollments = Objects.requireNonNull(enrollments);
    }

    @Override
    public StudentDashboard execute(UUID authenticatedStudentId) {
        Objects.requireNonNull(authenticatedStudentId, "Identificação do aluno obrigatória");
        var entries = enrollments.findActiveAndCompletedByStudent(authenticatedStudentId);
        var courses = entries.stream().map(StudentCourseOverview::from).toList();
        long completed = entries.stream().filter(e -> e.status() == EnrollmentStatus.COMPLETED).count();
        long active = entries.stream().filter(e -> e.status() == EnrollmentStatus.ACTIVE).count();
        return new StudentDashboard(courses.size(), Math.toIntExact(active), Math.toIntExact(completed), courses);
    }
}
