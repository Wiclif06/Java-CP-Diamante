package br.edu.campusgigs.api.dto;

import java.math.BigDecimal;
import br.edu.campusgigs.domain.*;

public record ServiceResponse(Long id, Long providerId, String title, String description,
                              String category, BigDecimal price, ServiceStatus status) {
    public static ServiceResponse from(ServiceOffer service) {
        return new ServiceResponse(service.getId(), service.getProvider().getId(), service.getTitle(),
                service.getDescription(), service.getCategory(), service.getPrice(), service.getStatus());
    }
}
