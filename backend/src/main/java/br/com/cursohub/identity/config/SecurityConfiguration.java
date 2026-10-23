package br.com.cursohub.identity.config;

import br.com.cursohub.identity.application.port.out.UserStorePort;
import br.com.cursohub.identity.domain.UserRole;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/** API REST com JWT HS256 assinado; a chave é obrigatória no ambiente. */
@Configuration
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/v1/courses", "/api/v1/courses/**",
                                 "/api/v1/membership/plans",
                                 "/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/instructor/**").hasRole("INSTRUCTOR")
                .requestMatchers(HttpMethod.GET, "/api/v1/students/me/loyalty").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/v1/students/me/dashboard").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/v1/students/me/courses/*/lessons").hasRole("STUDENT")
                .requestMatchers(HttpMethod.POST, "/api/v1/students/me/courses/*/lessons/*/complete").hasRole("STUDENT")
                .requestMatchers(HttpMethod.POST, "/api/v1/courses/*/enrollments").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/v1/students/me/courses/*/assets", "/api/v1/students/me/courses/*/assets/*").hasRole("STUDENT")
                .requestMatchers(HttpMethod.POST, "/api/v1/students/me/courses/*/lessons/*/video-progress").hasRole("STUDENT")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    SecretKey jwtKey(@Value("${security.jwt.secret-base64:}") String base64) {
        if (base64 == null || base64.isBlank())
            throw new IllegalStateException("JWT_SECRET_BASE64 ausente: configure chave aleatória Base64 de 32 bytes.");
        byte[] bytes;
        try { bytes = Base64.getDecoder().decode(base64); }
        catch (IllegalArgumentException e) { throw new IllegalStateException("JWT_SECRET_BASE64 inválida.", e); }
        if (bytes.length < 32)
            throw new IllegalStateException("JWT_SECRET_BASE64 deve representar pelo menos 32 bytes.");
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) { return new NimbusJwtEncoder(new ImmutableSecret<>(key)); }

    @Bean
    JwtDecoder jwtDecoder(SecretKey key, UserStorePort users,
                          @Value("${security.jwt.issuer}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> jwtValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> userValidator = jwt -> {
            try {
                UUID id = UUID.fromString(jwt.getSubject());
                UserRole role = UserRole.valueOf(jwt.getClaimAsString("role"));
                if (users.findById(id).filter(u -> u.active() && u.role() == role).isPresent())
                    return OAuth2TokenValidatorResult.success();
            } catch (RuntimeException ignored) {
                // Token inválido, sem perfil ou com usuário inexistente.
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Conta não disponível", null));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(jwtValidator, userValidator));
        return decoder;
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt ->
                List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("role"))));
        return converter;
    }
}
