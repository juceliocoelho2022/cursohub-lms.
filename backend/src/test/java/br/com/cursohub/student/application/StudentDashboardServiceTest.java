package br.com.cursohub.student.application;

import static org.junit.jupiter.api.Assertions.*;

import br.com.cursohub.student.application.port.out.StudentEnrollmentReaderPort;
import br.com.cursohub.student.domain.EnrollmentStatus;
import br.com.cursohub.student.domain.StudentEnrollment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StudentDashboardServiceTest {
    private final UUID student = UUID.randomUUID();
    private final UUID otherStudent = UUID.randomUUID();

    @Test
    void alunoSemMatriculasRecebeEstadoVazio() {
        var service = new StudentDashboardService(id -> List.of());
        var dashboard = service.execute(student);
        assertEquals(0, dashboard.enrolledCourses());
        assertEquals(0, dashboard.inProgressCourses());
        assertEquals(0, dashboard.completedCourses());
        assertTrue(dashboard.courses().isEmpty());
    }

    @Test
    void resumoContabilizaMatriculasEProgressoDeAulasObrigatorias() {
        StudentEnrollment active = enrollment(EnrollmentStatus.ACTIVE, 7, 3);
        StudentEnrollment completed = enrollment(EnrollmentStatus.COMPLETED, 4, 4);
        var service = new StudentDashboardService(id -> List.of(active, completed));
        var dashboard = service.execute(student);
        assertEquals(2, dashboard.enrolledCourses());
        assertEquals(1, dashboard.inProgressCourses());
        assertEquals(1, dashboard.completedCourses());
        assertEquals(43, dashboard.courses().getFirst().progressPercent());
        assertEquals(100, dashboard.courses().get(1).progressPercent());
    }

    @Test
    void semAulasObrigatoriasProgressoEhZero() {
        var enrollment = enrollment(EnrollmentStatus.ACTIVE, 0, 0);
        assertEquals(0, enrollment.progressPercent());
    }

    @Test
    void rejeitaContagensInconsistentes() {
        assertThrows(IllegalArgumentException.class, () ->
            enrollment(EnrollmentStatus.ACTIVE, 2, 3));
    }

    @Test
    void usaIdAutenticadoParaFiltrarDados() {
        StudentEnrollment expected = enrollment(EnrollmentStatus.ACTIVE, 2, 1);
        StudentEnrollmentReaderPort fake = studentId ->
            student.equals(studentId) ? List.of(expected) : List.of();
        var service = new StudentDashboardService(fake);
        assertEquals(1, service.execute(student).enrolledCourses());
        assertEquals(0, service.execute(otherStudent).enrolledCourses());
    }

    private static StudentEnrollment enrollment(EnrollmentStatus status, int required, int completed) {
        return new StudentEnrollment(UUID.randomUUID(), UUID.randomUUID(), "java-21",
                "Java 21", "Programação", status, required, completed,
                Instant.parse("2026-10-01T12:00:00Z"));
    }
}
