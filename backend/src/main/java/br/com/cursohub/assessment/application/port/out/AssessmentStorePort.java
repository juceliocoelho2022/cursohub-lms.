package br.com.cursohub.assessment.application.port.out;

import br.com.cursohub.assessment.application.model.AssessmentModels.*;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentStorePort {
    StudentQuiz save(UUID instructorId, UUID courseId, CreateQuiz request);
    Optional<StudentQuiz> findForInstructor(UUID instructorId, UUID courseId);
    Optional<StudentQuiz> findForStudent(UUID studentId, String slug);
    AttemptResult submit(UUID studentId, String slug, UUID assessmentId, SubmitAnswers answers);
    Certificate issue(UUID studentId, String slug);
    Optional<Certificate> findCertificate(UUID studentId, String slug);
    Optional<Certificate> verify(UUID code);
}
