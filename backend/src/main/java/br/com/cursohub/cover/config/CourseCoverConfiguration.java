package br.com.cursohub.cover.config;

import br.com.cursohub.cover.application.CourseCoverService;
import br.com.cursohub.cover.application.port.in.CourseCoverUseCase;
import br.com.cursohub.cover.application.port.out.CourseCoverStorePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourseCoverConfiguration {
    @Bean CourseCoverUseCase courseCoverUseCase(CourseCoverStorePort store) {
        return new CourseCoverService(store);
    }
}
