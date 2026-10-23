package br.com.cursohub.identity.adapters.out.security;

import br.com.cursohub.identity.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordAdapter implements PasswordHasherPort {
    private final PasswordEncoder encoder;

    public BCryptPasswordAdapter(PasswordEncoder encoder) { this.encoder = encoder; }

    @Override
    public String hash(String password) { return encoder.encode(password); }

    @Override
    public boolean matches(String candidate, String encoded) { return encoder.matches(candidate, encoded); }
}
