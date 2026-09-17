package br.edu.campusgigs.api.dto;

import jakarta.validation.constraints.*;

public record AddressRequest(
        @NotBlank @Pattern(regexp = "\\d{8}", message = "deve conter exatamente 8 dígitos") String cep
) {}
