package br.edu.campusgigs.api.dto;

import java.time.OffsetDateTime;
import br.edu.campusgigs.domain.*;

public record ContractResponse(Long id, Long serviceId, Long customerId, ContractStatus status, OffsetDateTime createdAt) {
    public static ContractResponse from(Contract contract) {
        return new ContractResponse(contract.getId(), contract.getService().getId(),
                contract.getCustomer().getId(), contract.getStatus(), contract.getCreatedAt());
    }
}
