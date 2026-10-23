package br.com.cursohub.identity.adapters.out.persistence;

import br.com.cursohub.identity.domain.UserAccount;
import br.com.cursohub.identity.domain.UserRole;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class JpaUserEntity {
    @Id
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected JpaUserEntity() {}

    static JpaUserEntity fromDomain(UserAccount account) {
        JpaUserEntity entity = new JpaUserEntity();
        entity.id = account.id();
        entity.fullName = account.fullName();
        entity.email = account.email();
        entity.passwordHash = account.passwordHash();
        entity.role = account.role();
        entity.active = account.active();
        entity.createdAt = Instant.now();
        entity.updatedAt = entity.createdAt;
        return entity;
    }

    UserAccount toDomain() {
        return new UserAccount(id, fullName, email, passwordHash, role, active);
    }
}
