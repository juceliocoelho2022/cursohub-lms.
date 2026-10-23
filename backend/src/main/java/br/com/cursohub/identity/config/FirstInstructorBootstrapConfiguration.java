package br.com.cursohub.identity.config;

import br.com.cursohub.identity.application.port.out.PasswordHasherPort;
import br.com.cursohub.identity.application.port.out.UserStorePort;
import br.com.cursohub.identity.domain.UserAccount;
import br.com.cursohub.identity.domain.UserRole;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** Opt-in and local-only bootstrap. No public instructor registration endpoint. */
@Configuration
public class FirstInstructorBootstrapConfiguration {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Bean
    @ConditionalOnProperty(name = "cursohub.bootstrap.instructor.enabled", havingValue = "true")
    CommandLineRunner firstInstructorBootstrap(
            UserStorePort users, PasswordHasherPort passwords, JdbcTemplate jdbc,
            TransactionTemplate transactions,
            @Value("${cursohub.bootstrap.instructor.name:}") String name,
            @Value("${cursohub.bootstrap.instructor.email:}") String configuredEmail,
            @Value("${cursohub.bootstrap.instructor.password:}") String password) {
        return args -> {
            String fullName = name.strip();
            String email = configuredEmail.strip().toLowerCase(Locale.ROOT);
            if (fullName.length() < 3 || fullName.length() > 150
                    || email.length() > 254 || !EMAIL.matcher(email).matches()
                    || password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalStateException("Configuracao invalida do bootstrap de instrutor.");
            }
            transactions.executeWithoutResult(status -> {
                // Lock the users table so concurrent bootstraps cannot create multiple instructors.
                jdbc.execute("LOCK TABLE app_users IN SHARE ROW EXCLUSIVE MODE");
                Integer existing = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM app_users WHERE role = 'INSTRUCTOR' AND is_active = true",
                        Integer.class);
                if (existing != null && existing > 0) {
                    throw new IllegalStateException("Bootstrap recusado: ja existe instrutor ativo.");
                }
                if (users.findByEmail(email).isPresent()) {
                    throw new IllegalStateException("Bootstrap recusado: email ja cadastrado.");
                }
                users.save(new UserAccount(UUID.randomUUID(), fullName, email,
                        passwords.hash(password), UserRole.INSTRUCTOR, true));
            });
            System.out.println("Bootstrap do primeiro instrutor concluido. Desative a configuracao de bootstrap.");
        };
    }
}
