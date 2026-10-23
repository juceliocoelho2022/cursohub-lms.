package br.com.cursohub.identity.application.port.out;

import br.com.cursohub.identity.domain.UserAccount;

public interface AccessTokenPort {
    String issue(UserAccount account);
    long expiresInSeconds();
}
