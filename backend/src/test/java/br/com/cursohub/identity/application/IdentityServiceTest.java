package br.com.cursohub.identity.application;

import static org.junit.jupiter.api.Assertions.*;

import br.com.cursohub.identity.application.exception.*;
import br.com.cursohub.identity.application.model.*;
import br.com.cursohub.identity.application.port.out.*;
import br.com.cursohub.identity.domain.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdentityServiceTest {
    private InMemoryUsers store;
    private IdentityService service;

    @BeforeEach
    void setup() {
        store = new InMemoryUsers();
        service = new IdentityService(store, new FakePasswords(), new FakeTokens());
    }

    @Test
    void cadastroPublicoCriaApenasAlunoEEmailNormalizado() {
        var result = service.registerStudent(new RegisterStudentCommand(
                "  Aluna de Testes  ", "  ALUNA@EXEMPLO.COM ", "UmaSenhaSegura#2026"));
        assertEquals(UserRole.STUDENT, result.role());
        assertEquals("Aluna de Testes", result.name());
        assertEquals("aluna@exemplo.com", result.email());
        assertEquals("Bearer", result.tokenType());
        assertEquals(900L, result.expiresIn());
        assertNotNull(store.findByEmail(result.email()).orElseThrow().passwordHash());
        assertNotEquals("UmaSenhaSegura#2026", store.findByEmail(result.email()).orElseThrow().passwordHash());
    }

    @Test
    void rejeitaEmailDuplicadoSemDiferenciarMaiusculas() {
        service.registerStudent(new RegisterStudentCommand("Primeira Aluna", "teste@exemplo.com", "SenhaSegura#2026"));
        assertThrows(DuplicateEmailException.class, () -> service.registerStudent(
                new RegisterStudentCommand("Segunda Aluna", "TESTE@EXEMPLO.COM", "SenhaSegura#2026")));
    }

    @Test
    void rejeitaSenhaCurtaOuEmailInvalido() {
        assertThrows(InvalidRegistrationException.class, () -> service.registerStudent(
                new RegisterStudentCommand("Aluno", "aluno@exemplo.com", "123")));
        assertThrows(InvalidRegistrationException.class, () -> service.registerStudent(
                new RegisterStudentCommand("Aluno", "email-invalido", "SenhaSegura#2026")));
    }

    @Test
    void loginValidoEhAutenticado() {
        service.registerStudent(new RegisterStudentCommand("Estudante", "teste@exemplo.com", "SenhaSegura#2026"));
        var login = service.login(new LoginCommand("  TESTE@EXEMPLO.COM ", "SenhaSegura#2026"));
        assertEquals(UserRole.STUDENT, login.role());
        assertNotNull(login.accessToken());
    }

    @Test
    void loginComSenhaErradaOuContaInexistenteFalha() {
        service.registerStudent(new RegisterStudentCommand("Estudante", "teste@exemplo.com", "SenhaSegura#2026"));
        assertThrows(InvalidCredentialsException.class, () -> service.login(
                new LoginCommand("teste@exemplo.com", "senha-incorreta")));
        assertThrows(InvalidCredentialsException.class, () -> service.login(
                new LoginCommand("inexistente@exemplo.com", "qualquer-senha")));
    }

    @Test
    void usuarioInativoNaoAutenticaNemConsultaPerfil() {
        var id = UUID.randomUUID();
        store.save(new UserAccount(id, "Conta Inativa", "inativo@exemplo.com",
                "hashed|SenhaSegura#2026", UserRole.STUDENT, false));
        assertThrows(InvalidCredentialsException.class, () -> service.login(
                new LoginCommand("inativo@exemplo.com", "SenhaSegura#2026")));
        assertThrows(AccountUnavailableException.class, () -> service.findCurrentUser(id));
    }

    @Test
    void perfilDaContaCadastradaPodeSerConsultado() {
        var result = service.registerStudent(new RegisterStudentCommand(
                "Aluna Teste", "aluna@exemplo.com", "UmaSenhaSegura#2026"));
        var account = service.findCurrentUser(result.userId());
        assertEquals(UserRole.STUDENT, account.role());
        assertEquals("Aluna Teste", account.fullName());
    }

    private static final class InMemoryUsers implements UserStorePort {
        private final Map<UUID, UserAccount> users = new HashMap<>();

        @Override
        public Optional<UserAccount> findByEmail(String email) {
            return users.values().stream().filter(u -> u.email().equals(email)).findFirst();
        }

        @Override
        public Optional<UserAccount> findById(UUID id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public UserAccount save(UserAccount account) {
            users.put(account.id(), account);
            return account;
        }
    }

    private static final class FakePasswords implements PasswordHasherPort {
        @Override public String hash(String password) { return "hashed|" + password; }
        @Override public boolean matches(String input, String encoded) {
            return ("hashed|" + input).equals(encoded);
        }
    }

    private static final class FakeTokens implements AccessTokenPort {
        @Override public String issue(UserAccount account) { return "fake-token-for-" + account.id(); }
        @Override public long expiresInSeconds() { return 900L; }
    }
}
