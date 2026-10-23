package br.com.cursohub.student.adapters.in.web;

import br.com.cursohub.student.application.model.StudentDashboard;
import br.com.cursohub.student.application.port.in.ViewStudentDashboardUseCase;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Nunca aceita studentId por query string: usa exclusivamente o JWT validado. */
@RestController
@RequestMapping("/api/v1/students/me")
public class StudentDashboardController {
    private final ViewStudentDashboardUseCase dashboard;

    public StudentDashboardController(ViewStudentDashboardUseCase dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/dashboard")
    public StudentDashboard getDashboard(@AuthenticationPrincipal Jwt jwt) {
        return dashboard.execute(UUID.fromString(jwt.getSubject()));
    }
}
