package br.edu.campusgigs.integration;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface ViaCepClient {
    @GetExchange("/ws/{cep}/json/")
    ViaCepResponse lookup(@PathVariable String cep);
}
