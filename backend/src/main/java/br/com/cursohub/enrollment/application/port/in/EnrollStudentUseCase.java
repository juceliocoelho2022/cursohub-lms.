package br.com.cursohub.enrollment.application.port.in;

import br.com.cursohub.enrollment.application.model.EnrollmentReceipt;
import java.util.UUID;

public interface EnrollStudentUseCase {
    EnrollmentReceipt execute(UUID authenticatedStudentId, String courseSlug);
}
