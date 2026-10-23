package br.com.cursohub.identity.adapters.in.web;

import br.com.cursohub.identity.application.model.LoginCommand;
import br.com.cursohub.identity.application.model.RegisterStudentCommand;
import br.com.cursohub.identity.application.port.in.IdentityUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final IdentityUseCase identity;

    public AuthController(IdentityUseCase identity) { this.identity = identity; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Object register(@Valid @RequestBody RegistrationRequest request) {
        return identity.registerStudent(new RegisterStudentCommand(request.name(), request.email(), request.password()));
    }

    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest request) {
        return identity.login(new LoginCommand(request.email(), request.password()));
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(identity.findCurrentUser(UUID.fromString(jwt.getSubject())));
    }
}
