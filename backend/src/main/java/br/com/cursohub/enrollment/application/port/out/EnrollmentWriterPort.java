package br.com.cursohub.enrollment.application.port.out;

import br.com.cursohub.enrollment.application.model.EnrollmentReceipt;
import java.util.UUID;

public interface EnrollmentWriterPort {
    EnrollmentReceipt enrollIfPublished(UUID studentId, String courseSlug);
}
