package br.edu.campusgigs;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import br.edu.campusgigs.api.ApiException;
import br.edu.campusgigs.integration.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressLookupTest {
    private final ViaCepClient client = mock(ViaCepClient.class);
    private final AddressLookup lookup = new AddressLookup(client);

    @Test
    void nonexistentCepReturnsClearError() {
        when(client.lookup("99999999")).thenReturn(new ViaCepResponse(null, null, true));
        assertThatThrownBy(() -> lookup.lookup("99999999"))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException)e).code()).isEqualTo("CEP_NOT_FOUND"));
    }
    @Test
    void timeoutReturnsServiceUnavailable() {
        when(client.lookup("01001000")).thenThrow(new ResourceAccessException("timeout"));
        assertThatThrownBy(() -> lookup.lookup("01001000"))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException)e).code()).isEqualTo("CEP_SERVICE_UNAVAILABLE"));
    }
    @Test
    void malformedCepDoesNotCallExternalService() {
        assertThatThrownBy(() -> lookup.lookup("123"))
                .isInstanceOf(ApiException.class);
        verifyNoInteractions(client);
    }
}
