package br.edu.campusgigs.api.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record ServiceRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank String description,
        @NotBlank @Size(max = 80) String category,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal price
) {}
