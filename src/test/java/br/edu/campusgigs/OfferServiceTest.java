package br.edu.campusgigs;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import br.edu.campusgigs.api.ApiException;
import br.edu.campusgigs.domain.*;
import br.edu.campusgigs.repository.ServiceRepository;
import br.edu.campusgigs.service.OfferService;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OfferServiceTest {
    private final ServiceRepository repository = mock(ServiceRepository.class);
    private final OfferService offers = new OfferService(repository);

    @Test
    void userCannotCloseAnotherUsersService() {
        UserAccount provider = user(1L, Role.USER);
        ServiceOffer offer = offer(provider);
        when(repository.findLockedById(10L)).thenReturn(Optional.of(offer));
        assertThatThrownBy(() -> offers.close(10L, user(2L, Role.USER)))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException)e).status()).isEqualTo(HttpStatus.FORBIDDEN));
        assertThat(offer.getStatus()).isEqualTo(ServiceStatus.ATIVO);
    }

    @Test
    void adminCanCloseAnotherUsersService() {
        ServiceOffer offer = offer(user(1L, Role.USER));
        when(repository.findLockedById(10L)).thenReturn(Optional.of(offer));
        offers.close(10L, user(3L, Role.ADMIN));
        assertThat(offer.getStatus()).isEqualTo(ServiceStatus.ENCERRADO);
    }

    static UserAccount user(Long id, Role role) {
        UserAccount user = mock(UserAccount.class);
        when(user.getId()).thenReturn(id);
        when(user.getRole()).thenReturn(role);
        return user;
    }
    static ServiceOffer offer(UserAccount provider) {
        return new ServiceOffer(provider, "Revisão Java", "Aula", "EDUCACAO", new BigDecimal("40.00"));
    }
}
