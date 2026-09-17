package br.edu.campusgigs.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.edu.campusgigs.domain.*;
import br.edu.campusgigs.repository.UserRepository;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final String email;
    private final String password;
    public AdminBootstrap(UserRepository users, PasswordEncoder passwords,
                          @Value("${campusgigs.admin.email:}") String email,
                          @Value("${campusgigs.admin.password:}") String password) {
        this.users = users;
        this.passwords = passwords;
        this.email = email;
        this.password = password;
    }
    @Override
    public void run(String... args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.isBlank() || password.length() < 12) {
            throw new IllegalArgumentException("Configure e-mail e senha ADMIN com pelo menos 12 caracteres.");
        }
        var existing = users.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            if (existing.get().getRole() != Role.ADMIN) {
                throw new IllegalStateException("E-mail ADMIN já pertence a um usuário comum.");
            }
            return;
        }
        users.save(new UserAccount("Administrador", email.trim().toLowerCase(),
                passwords.encode(password), Role.ADMIN, "01001000", "São Paulo", "SP"));
    }
}
