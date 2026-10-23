package br.com.cursohub.learning.adapters.in.web;

import br.com.cursohub.learning.application.model.LessonCompletionReceipt;
import br.com.cursohub.learning.application.port.in.StudentLearningUseCase;
import br.com.cursohub.learning.domain.LearningCourse;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students/me/courses/{slug}")
public class StudentLearningController {
    private final StudentLearningUseCase useCase;

    public StudentLearningController(StudentLearningUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/lessons")
    public LearningCourse lessons(@AuthenticationPrincipal Jwt jwt, @PathVariable String slug) {
        return useCase.viewCourse(UUID.fromString(jwt.getSubject()), slug);
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public LessonCompletionReceipt complete(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable String slug,
                                            @PathVariable UUID lessonId) {
        return useCase.completeLesson(UUID.fromString(jwt.getSubject()), slug, lessonId);
    }
}
