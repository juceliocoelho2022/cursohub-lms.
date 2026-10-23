package br.com.cursohub.student.config;

import br.com.cursohub.student.application.StudentDashboardService;
import br.com.cursohub.student.application.port.in.ViewStudentDashboardUseCase;
import br.com.cursohub.student.application.port.out.StudentEnrollmentReaderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StudentDashboardConfiguration {
    @Bean
    ViewStudentDashboardUseCase viewStudentDashboardUseCase(StudentEnrollmentReaderPort reader) {
        return new StudentDashboardService(reader);
    }
}
