package br.edu.campusgigs;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import br.edu.campusgigs.api.ApiException;
import br.edu.campusgigs.domain.*;
import br.edu.campusgigs.repository.*;
import br.edu.campusgigs.service.ContractService;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContractServiceTest {
    private final ContractRepository contracts = mock(ContractRepository.class);
    private final ServiceRepository services = mock(ServiceRepository.class);
    private final ContractService hiring = new ContractService(contracts, services);

    @Test
    void providerCannotHireOwnService() {
        UserAccount provider = OfferServiceTest.user(1L, Role.USER);
        when(services.findLockedById(8L)).thenReturn(Optional.of(OfferServiceTest.offer(provider)));
        assertThatThrownBy(() -> hiring.hire(8L, provider))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException)e).code()).isEqualTo("SELF_HIRE"));
        verifyNoInteractions(contracts);
    }

    @Test
    void closedServiceCannotBeHired() {
        ServiceOffer offer = OfferServiceTest.offer(OfferServiceTest.user(1L, Role.USER));
        offer.close();
        when(services.findLockedById(8L)).thenReturn(Optional.of(offer));
        assertThatThrownBy(() -> hiring.hire(8L, OfferServiceTest.user(2L, Role.USER)))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException)e).status()).isEqualTo(HttpStatus.CONFLICT));
        verifyNoInteractions(contracts);
    }
}
