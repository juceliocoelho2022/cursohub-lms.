package br.com.cursohub.assessment.adapters.in;

import br.com.cursohub.assessment.application.AssessmentNotFoundException;
import br.com.cursohub.assessment.application.model.AssessmentModels.*;
import br.com.cursohub.assessment.application.port.in.AssessmentUseCase;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
public class AssessmentController {
    private final AssessmentUseCase useCase;
    public AssessmentController(AssessmentUseCase useCase) { this.useCase=useCase; }

    @PostMapping("/api/v1/instructor/courses/{courseId}/assessment")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentQuiz create(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID courseId,
                              @RequestBody CreateQuiz request) {
        return useCase.create(subject(jwt),courseId,request);
    }

    @GetMapping("/api/v1/instructor/courses/{courseId}/assessment")
    public Optional<StudentQuiz> instructor(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID courseId) {
        return useCase.findForInstructor(subject(jwt),courseId);
    }

    @GetMapping("/api/v1/students/me/courses/{slug}/assessment")
    public Optional<StudentQuiz> student(@AuthenticationPrincipal Jwt jwt,@PathVariable String slug) {
        return useCase.findForStudent(subject(jwt),slug);
    }

    @PostMapping("/api/v1/students/me/courses/{slug}/assessment/{assessmentId}/attempts")
    public AttemptResult submit(@AuthenticationPrincipal Jwt jwt,@PathVariable String slug,
                               @PathVariable UUID assessmentId,@RequestBody SubmitAnswers answers) {
        return useCase.submit(subject(jwt),slug,assessmentId,answers);
    }

    @PostMapping("/api/v1/students/me/courses/{slug}/certificate")
    public Certificate issue(@AuthenticationPrincipal Jwt jwt,@PathVariable String slug) {
        return useCase.issue(subject(jwt),slug);
    }

    @GetMapping("/api/v1/students/me/courses/{slug}/certificate")
    public Optional<Certificate> certificate(@AuthenticationPrincipal Jwt jwt,@PathVariable String slug) {
        return useCase.findCertificate(subject(jwt),slug);
    }

    /** Public certificate validation uses an unguessable UUID verification code. */
    @GetMapping("/api/v1/certificates/verify/{code}")
    public Certificate verify(@PathVariable UUID code) {
        return useCase.verify(code).orElseThrow(AssessmentNotFoundException::new);
    }

    private static UUID subject(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
