package br.com.cursohub.enrollment.adapters.in.web;

import br.com.cursohub.enrollment.application.model.EnrollmentReceipt;
import br.com.cursohub.enrollment.application.port.in.EnrollStudentUseCase;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A identidade vem exclusivamente do JWT validado; nunca do corpo HTTP. */
@RestController
@RequestMapping("/api/v1/courses")
public class EnrollmentController {
    private final EnrollStudentUseCase enroll;

    public EnrollmentController(EnrollStudentUseCase enroll) {
        this.enroll = enroll;
    }

    @PostMapping("/{slug}/enrollments")
    public ResponseEntity<EnrollmentReceipt> create(@PathVariable String slug, @AuthenticationPrincipal Jwt jwt) {
        EnrollmentReceipt result = enroll.execute(UUID.fromString(jwt.getSubject()), slug);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(result);
    }
}
