package br.com.cursohub.identity.adapters.in.web;

import br.com.cursohub.identity.domain.UserAccount;
import br.com.cursohub.identity.domain.UserRole;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, UserRole role) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.id(), user.fullName(), user.email(), user.role());
    }
}
