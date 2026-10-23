package br.com.cursohub.identity.application;

import br.com.cursohub.identity.application.exception.*;
import br.com.cursohub.identity.application.model.*;
import br.com.cursohub.identity.application.port.in.IdentityUseCase;
import br.com.cursohub.identity.application.port.out.*;
import br.com.cursohub.identity.domain.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/** Caso de uso: não importa Spring, JPA, BCrypt, Nimbus ou HTTP. */
public final class IdentityService implements IdentityUseCase {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserStorePort users;
    private final PasswordHasherPort passwords;
    private final AccessTokenPort tokens;

    public IdentityService(UserStorePort users, PasswordHasherPort passwords, AccessTokenPort tokens) {
        this.users = Objects.requireNonNull(users);
        this.passwords = Objects.requireNonNull(passwords);
        this.tokens = Objects.requireNonNull(tokens);
    }

    @Override
    public AuthenticatedSession registerStudent(RegisterStudentCommand command) {
        if (command == null) throw new InvalidRegistrationException("Cadastro inválido");
        String name = command.name() == null ? "" : command.name().strip();
        String email = normalizeEmail(command.email());
        String password = command.password();

        if (name.length() < 3 || name.length() > 150)
            throw new InvalidRegistrationException("Nome deve ter de 3 a 150 caracteres");
        if (email.length() > 254 || !EMAIL.matcher(email).matches())
            throw new InvalidRegistrationException("E-mail inválido");
        if (password == null || password.length() < 12 || password.length() > 72 ||
            password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new InvalidRegistrationException("Senha deve ter de 12 a 72 bytes");
        if (users.findByEmail(email).isPresent()) throw new DuplicateEmailException();

        UserAccount user = new UserAccount(UUID.randomUUID(), name, email, passwords.hash(password),
                                           UserRole.STUDENT, true);
        return session(users.save(user));
    }

    @Override
    public AuthenticatedSession login(LoginCommand command) {
        if (command == null) throw new InvalidCredentialsException();
        String email = normalizeEmail(command.email());
        // Mesmo hash de senha fictício para reduzir diferenças grosseiras de tempo.
        UserAccount user = users.findByEmail(email).orElse(null);
        if (user == null || !user.active() || !passwords.matches(command.password() == null ? "" : command.password(), user.passwordHash()))
            throw new InvalidCredentialsException();
        return session(user);
    }

    @Override
    public UserAccount findCurrentUser(UUID id) {
        return users.findById(id).filter(UserAccount::active).orElseThrow(AccountUnavailableException::new);
    }

    private AuthenticatedSession session(UserAccount user) {
        return new AuthenticatedSession(tokens.issue(user), "Bearer", tokens.expiresInSeconds(),
                                        user.id(), user.fullName(), user.email(), user.role());
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
