package br.com.cursohub.enrollment.config;

import br.com.cursohub.enrollment.application.EnrollStudentService;
import br.com.cursohub.enrollment.application.port.in.EnrollStudentUseCase;
import br.com.cursohub.enrollment.application.port.out.EnrollmentWriterPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnrollmentConfiguration {
    @Bean
    EnrollStudentUseCase enrollStudentUseCase(EnrollmentWriterPort writer) {
        return new EnrollStudentService(writer);
    }
}
