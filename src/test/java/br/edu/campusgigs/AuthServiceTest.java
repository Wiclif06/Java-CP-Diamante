package br.edu.campusgigs;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import br.edu.campusgigs.api.dto.RegisterRequest;
import br.edu.campusgigs.domain.*;
import br.edu.campusgigs.integration.AddressLookup;
import br.edu.campusgigs.repository.UserRepository;
import br.edu.campusgigs.service.AuthService;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Test
    void registrationHashesPasswordAndNeverAcceptsAdminRole() {
        UserRepository users = mock(UserRepository.class);
        AddressLookup lookup = mock(AddressLookup.class);
        JwtEncoder encoder = mock(JwtEncoder.class);
        when(lookup.lookup("01001000"))
                .thenReturn(new AddressLookup.AddressData("01001000", "São Paulo", "SP"));
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuthService auth = new AuthService(users, new BCryptPasswordEncoder(), encoder, lookup, "campusgigs", 60);
        var response = auth.register(new RegisterRequest("Ana", "ANA@campus.test", "SenhaForte123!", "01001000"));
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.email()).isEqualTo("ana@campus.test");
        verify(users).save(argThat(user ->
                user.getPasswordHash().startsWith("$2") &&
                !user.getPasswordHash().equals("SenhaForte123!")));
    }
}
