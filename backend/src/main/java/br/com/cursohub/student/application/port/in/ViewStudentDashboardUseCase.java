package br.com.cursohub.student.application.port.in;

import br.com.cursohub.student.application.model.StudentDashboard;
import java.util.UUID;

public interface ViewStudentDashboardUseCase {
    StudentDashboard execute(UUID authenticatedStudentId);
}
