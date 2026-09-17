package br.edu.campusgigs.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import br.edu.campusgigs.api.ApiException;
import br.edu.campusgigs.domain.UserAccount;
import br.edu.campusgigs.repository.UserRepository;
import org.springframework.http.HttpStatus;

@Component
public class CurrentUser {
    private final UserRepository users;
    public CurrentUser(UserRepository users) { this.users = users; }
    public UserAccount from(Jwt jwt) {
        try {
            Long id = Long.valueOf(jwt.getSubject());
            return users.findById(id).orElseThrow(() ->
                    new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Usuário do token não existe."));
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_SUBJECT", "Token inválido.");
        }
    }
}
