package br.com.cursohub.identity.application.model;

import br.com.cursohub.identity.domain.UserRole;
import java.util.UUID;

public record AuthenticatedSession(String accessToken, String tokenType, long expiresIn,
                                   UUID userId, String name, String email, UserRole role) {}
