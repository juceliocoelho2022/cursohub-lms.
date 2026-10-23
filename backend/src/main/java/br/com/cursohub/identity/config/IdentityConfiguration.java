package br.com.cursohub.identity.config;

import br.com.cursohub.identity.application.IdentityService;
import br.com.cursohub.identity.application.port.in.IdentityUseCase;
import br.com.cursohub.identity.application.port.out.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityConfiguration {
    @Bean
    IdentityUseCase identityUseCase(UserStorePort users, PasswordHasherPort passwords, AccessTokenPort tokens) {
        return new IdentityService(users, passwords, tokens);
    }
}
