package br.com.cursohub.identity.domain;

import java.util.Objects;
import java.util.UUID;

/** Entidade pura de domínio. Nunca retorne passwordHash nas APIs. */
public record UserAccount(UUID id, String fullName, String email, String passwordHash, UserRole role, boolean active) {
    public UserAccount {
        Objects.requireNonNull(id);
        Objects.requireNonNull(fullName);
        Objects.requireNonNull(email);
        Objects.requireNonNull(passwordHash);
        Objects.requireNonNull(role);
        if (fullName.isBlank() || email.isBlank() || passwordHash.isBlank())
            throw new IllegalArgumentException("Dados obrigatórios do usuário ausentes");
    }
}
