package br.com.cursohub.instructor.adapters.in.web;

import br.com.cursohub.catalog.domain.CourseLevel;
import br.com.cursohub.instructor.application.model.*;
import br.com.cursohub.instructor.application.port.in.InstructorStudioUseCase;
import br.com.cursohub.instructor.domain.*;
import br.com.cursohub.learning.domain.LessonKind;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** HTTP/JSON; instructorId é SEMPRE extraído do JWT validado, nunca do corpo. */
@RestController
@RequestMapping("/api/v1/instructor/courses")
public class InstructorStudioController {
    private final InstructorStudioUseCase useCase;

    public InstructorStudioController(InstructorStudioUseCase useCase) { this.useCase = useCase; }

    @GetMapping
    public List<InstructorCourse> list(@AuthenticationPrincipal Jwt jwt) {
        return useCase.listCourses(userId(jwt));
    }

    @GetMapping("/{courseId}/content")
    public InstructorCourseContent content(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID courseId) {
        return useCase.getCourse(userId(jwt), courseId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstructorCourse create(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateCourseRequest request) {
        return useCase.createCourse(userId(jwt), new CreateCourseCommand(
                request.slug(), request.title(), request.description(), request.category(),
                request.level(), request.estimatedMinutes()));
    }

    @PostMapping("/{courseId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public InstructorModule module(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID courseId,
                                   @RequestBody CreateModuleRequest request) {
        return useCase.addModule(userId(jwt), courseId, request.title());
    }

    @PostMapping("/{courseId}/modules/{moduleId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public InstructorLesson lesson(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID courseId,
                                   @PathVariable UUID moduleId, @RequestBody CreateLessonRequest request) {
        return useCase.addLesson(userId(jwt), courseId, moduleId,
                new CreateLessonCommand(request.title(), request.kind(), request.contentUrl(),
                        request.bodyText(), request.durationSeconds(), request.required()));
    }

    @PostMapping("/{courseId}/publish")
    public InstructorCourse publish(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID courseId) {
        return useCase.publishCourse(userId(jwt), courseId);
    }

    private static UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }

    public record CreateCourseRequest(String slug, String title, String description, String category,
                                      CourseLevel level, int estimatedMinutes) {}
    public record CreateModuleRequest(String title) {}
    public record CreateLessonRequest(String title, LessonKind kind, String contentUrl,
                                      String bodyText, int durationSeconds, boolean required) {}
}
