package br.edu.campusgigs.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import br.edu.campusgigs.api.dto.*;
import br.edu.campusgigs.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return auth.register(request);
    }
    @PostMapping("/login")
    public AuthService.TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }
}
