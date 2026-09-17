package br.edu.campusgigs.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.campusgigs.api.ApiException;
import br.edu.campusgigs.api.dto.*;
import br.edu.campusgigs.domain.*;
import br.edu.campusgigs.integration.AddressLookup;
import br.edu.campusgigs.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    private final AddressLookup address;
    private final String issuer;
    private final long minutes;

    public AuthService(UserRepository users, PasswordEncoder passwords, JwtEncoder encoder,
                       AddressLookup address, @Value("${campusgigs.jwt.issuer}") String issuer,
                       @Value("${campusgigs.jwt.minutes}") long minutes) {
        this.users = users;
        this.passwords = passwords;
        this.encoder = encoder;
        this.address = address;
        this.issuer = issuer;
        this.minutes = minutes;
    }
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "E-mail já cadastrado.");
        }
        AddressLookup.AddressData addr = address.lookup(request.cep());
        UserAccount user = new UserAccount(request.name().trim(), email, passwords.encode(request.password()),
                Role.USER, addr.cep(), addr.city(), addr.uf());
        return UserResponse.from(users.save(user));
    }
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> invalidCredentials());
        if (!passwords.matches(request.password(), user.getPasswordHash())) throw invalidCredentials();
        Instant now = Instant.now();
        Instant expiry = now.plus(minutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer).issuedAt(now).expiresAt(expiry)
                .subject(user.getId().toString()).claim("role", user.getRole().name()).build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiry);
    }
    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "E-mail ou senha inválidos.");
    }
    public record TokenResponse(String accessToken, String tokenType, Instant expiresAt) {}
}
