package br.edu.campusgigs.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.campusgigs.api.ApiException;
import br.edu.campusgigs.api.dto.ContractResponse;
import br.edu.campusgigs.domain.*;
import br.edu.campusgigs.repository.ContractRepository;
import br.edu.campusgigs.repository.ServiceRepository;

@Service
public class ContractService {
    private final ContractRepository contracts;
    private final ServiceRepository services;
    public ContractService(ContractRepository contracts, ServiceRepository services) {
        this.contracts = contracts;
        this.services = services;
    }
    @Transactional
    public ContractResponse hire(Long serviceId, UserAccount actor) {
        ServiceOffer offer = services.findLockedById(serviceId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "SERVICE_NOT_FOUND", "Serviço não encontrado."));
        if (offer.getStatus() != ServiceStatus.ATIVO) {
            throw new ApiException(HttpStatus.CONFLICT, "SERVICE_NOT_ACTIVE", "Somente serviço ativo pode ser contratado.");
        }
        if (offer.getProvider().getId().equals(actor.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "SELF_HIRE", "Você não pode contratar o próprio serviço.");
        }
        return ContractResponse.from(contracts.save(new Contract(offer, actor)));
    }
}
