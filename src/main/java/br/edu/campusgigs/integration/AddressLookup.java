package br.edu.campusgigs.integration;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import br.edu.campusgigs.api.ApiException;

@Service
public class AddressLookup {
    private final ViaCepClient client;
    public AddressLookup(ViaCepClient client) { this.client = client; }

    public AddressData lookup(String cep) {
        if (cep == null || !cep.matches("\\d{8}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CEP", "CEP deve conter exatamente 8 dígitos.");
        }
        try {
            ViaCepResponse response = client.lookup(cep);
            if (response == null || Boolean.TRUE.equals(response.erro())
                    || response.localidade() == null || response.uf() == null) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "CEP_NOT_FOUND", "CEP não encontrado.");
            }
            return new AddressData(cep, response.localidade(), response.uf());
        } catch (ApiException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CEP_SERVICE_UNAVAILABLE",
                    "Consulta de CEP indisponível. Tente novamente.");
        }
    }
    public record AddressData(String cep, String city, String uf) {}
}
