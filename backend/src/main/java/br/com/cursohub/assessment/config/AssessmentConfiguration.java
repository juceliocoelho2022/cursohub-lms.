package br.com.cursohub.assessment.config;

import br.com.cursohub.assessment.application.AssessmentService;
import br.com.cursohub.assessment.application.port.in.AssessmentUseCase;
import br.com.cursohub.assessment.application.port.out.AssessmentStorePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssessmentConfiguration {
    @Bean AssessmentUseCase assessmentUseCase(AssessmentStorePort store) {
        return new AssessmentService(store);
    }
}
