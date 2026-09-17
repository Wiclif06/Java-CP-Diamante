package br.edu.campusgigs.api.dto;

import jakarta.validation.constraints.NotNull;
import br.edu.campusgigs.domain.ServiceStatus;

public record StatusRequest(@NotNull ServiceStatus status) {}
