package br.edu.campusgigs.api;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import br.edu.campusgigs.api.dto.*;
import br.edu.campusgigs.security.CurrentUser;
import br.edu.campusgigs.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final CurrentUser current;
    private final UserService users;
    public UserController(CurrentUser current, UserService users) {
        this.current = current;
        this.users = users;
    }
    @PatchMapping("/me/cep")
    public UserResponse updateCep(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddressRequest request) {
        return users.updateCep(current.from(jwt), request.cep());
    }
}
