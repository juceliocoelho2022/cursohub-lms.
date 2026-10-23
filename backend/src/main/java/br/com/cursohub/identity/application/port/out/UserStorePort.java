package br.com.cursohub.identity.application.port.out;

import br.com.cursohub.identity.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;

public interface UserStorePort {
    Optional<UserAccount> findByEmail(String normalizedEmail);
    Optional<UserAccount> findById(UUID id);
    UserAccount save(UserAccount account);
}
