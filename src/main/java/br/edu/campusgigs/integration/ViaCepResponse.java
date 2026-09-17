package br.edu.campusgigs.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(String localidade, String uf, Boolean erro) {}
