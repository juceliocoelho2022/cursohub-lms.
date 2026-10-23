package br.com.cursohub.loyalty.adapters.in;

import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.cursohub.loyalty.application.port.in.LoyaltyUseCase;

/** Recompensa de aprendizagem: pontos calculados no banco, jamais informados pelo cliente. */
@RestController
public class LoyaltyController {
    private final LoyaltyUseCase useCase;
    public LoyaltyController(LoyaltyUseCase useCase){this.useCase=useCase;}

    @GetMapping("/api/v1/students/me/loyalty")
    public LoyaltyUseCase.Summary summary(@AuthenticationPrincipal Jwt jwt) {
        return useCase.summary(UUID.fromString(jwt.getSubject()));
    }
}
