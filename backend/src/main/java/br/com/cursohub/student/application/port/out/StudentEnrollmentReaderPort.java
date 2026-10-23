package br.com.cursohub.student.application.port.out;

import br.com.cursohub.student.domain.StudentEnrollment;
import java.util.List;
import java.util.UUID;

public interface StudentEnrollmentReaderPort {
    List<StudentEnrollment> findActiveAndCompletedByStudent(UUID authenticatedStudentId);
}
