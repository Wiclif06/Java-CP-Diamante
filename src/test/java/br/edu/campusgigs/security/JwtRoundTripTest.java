package br.edu.campusgigs.security;

import java.time.Instant;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import static org.assertj.core.api.Assertions.*;

class JwtRoundTripTest {
    private final SecurityConfig config = new SecurityConfig();

    @Test
    void hs256TokenValidatesSignatureIssuerAndExpiration() {
        SecretKey key = config.jwtKey("uma-chave-de-teste-com-mais-de-trinta-e-dois-bytes");
        JwtEncoder encoder = config.jwtEncoder(key);
        JwtDecoder decoder = config.jwtDecoder(key, "campusgigs");
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("campusgigs").subject("42")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60))
                .claim("role", "USER").build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        assertThat(decoder.decode(token).getSubject()).isEqualTo("42");
        SecretKey otherKey = config.jwtKey("outra-chave-de-teste-com-mais-de-trinta-e-dois-bytes");
        assertThatThrownBy(() -> config.jwtDecoder(otherKey, "campusgigs").decode(token))
                .isInstanceOf(JwtException.class);
    }
}
