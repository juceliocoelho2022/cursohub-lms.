package br.com.cursohub.identity.adapters.out.security;

import br.com.cursohub.identity.application.port.out.AccessTokenPort;
import br.com.cursohub.identity.domain.UserAccount;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessTokenAdapter implements AccessTokenPort {
    private static final long EXPIRES_IN_SECONDS = Duration.ofMinutes(15).toSeconds();
    private final JwtEncoder encoder;
    private final String issuer;

    public JwtAccessTokenAdapter(JwtEncoder encoder, @Value("${security.jwt.issuer}") String issuer) {
        this.encoder = encoder;
        this.issuer = issuer;
    }

    @Override
    public String issue(UserAccount user) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(EXPIRES_IN_SECONDS))
                .subject(user.id().toString())
                .claim("role", user.role().name())
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public long expiresInSeconds() { return EXPIRES_IN_SECONDS; }
}
