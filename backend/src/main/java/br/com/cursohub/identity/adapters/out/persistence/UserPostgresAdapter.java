package br.com.cursohub.identity.adapters.out.persistence;

import br.com.cursohub.identity.application.exception.DuplicateEmailException;
import br.com.cursohub.identity.application.port.out.UserStorePort;
import br.com.cursohub.identity.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserPostgresAdapter implements UserStorePort {
    private final JpaUserRepository repository;

    public UserPostgresAdapter(JpaUserRepository repository) { this.repository = repository; }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(JpaUserEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findById(UUID id) {
        return repository.findById(id).map(JpaUserEntity::toDomain);
    }

    @Override
    @Transactional
    public UserAccount save(UserAccount account) {
        try {
            return repository.saveAndFlush(JpaUserEntity.fromDomain(account)).toDomain();
        } catch (DataIntegrityViolationException ex) {
            // Protege contra cadastros simultâneos do mesmo e-mail.
            throw new DuplicateEmailException();
        }
    }
}
