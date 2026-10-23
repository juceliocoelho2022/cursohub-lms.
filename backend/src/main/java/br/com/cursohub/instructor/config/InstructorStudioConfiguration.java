package br.com.cursohub.instructor.config;

import br.com.cursohub.instructor.application.InstructorStudioService;
import br.com.cursohub.instructor.application.port.in.InstructorStudioUseCase;
import br.com.cursohub.instructor.application.port.out.InstructorStudioPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InstructorStudioConfiguration {
    @Bean
    InstructorStudioUseCase instructorStudioUseCase(InstructorStudioPort port) {
        return new InstructorStudioService(port);
    }
}
