package br.com.cursohub.learning.application.port.out;

import br.com.cursohub.learning.domain.LearningCourse;
import java.util.Optional;
import java.util.UUID;

public interface LearningContentReaderPort {
    Optional<LearningCourse> findEnrolledCourse(UUID studentId, String slug);
}
