package br.edu.campusgigs.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.campusgigs.api.ApiException;
import br.edu.campusgigs.api.dto.*;
import br.edu.campusgigs.domain.*;
import br.edu.campusgigs.repository.ServiceRepository;

@Service
public class OfferService {
    private final ServiceRepository services;
    public OfferService(ServiceRepository services) { this.services = services; }

    @Transactional
    public ServiceResponse publish(UserAccount actor, ServiceRequest request) {
        ServiceOffer offer = new ServiceOffer(actor, request.title().trim(), request.description().trim(),
                request.category().trim(), request.price());
        return ServiceResponse.from(services.save(offer));
    }
    @Transactional(readOnly = true)
    public List<ServiceResponse> list() {
        return services.findAllWithProvider().stream().map(ServiceResponse::from).toList();
    }
    @Transactional
    public ServiceResponse edit(Long id, UserAccount actor, ServiceRequest request) {
        ServiceOffer offer = locked(id);
        if (!offer.getProvider().getId().equals(actor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_OWNER", "Somente o prestador pode editar este serviço.");
        }
        if (offer.getStatus() == ServiceStatus.ENCERRADO) {
            throw new ApiException(HttpStatus.CONFLICT, "SERVICE_CLOSED", "Serviço encerrado não pode ser editado.");
        }
        offer.edit(request.title().trim(), request.description().trim(),
                request.category().trim(), request.price());
        return ServiceResponse.from(offer);
    }
    @Transactional
    public ServiceResponse close(Long id, UserAccount actor) {
        ServiceOffer offer = locked(id);
        boolean owner = offer.getProvider().getId().equals(actor.getId());
        if (!owner && actor.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_ALLOWED", "Você não pode encerrar este serviço.");
        }
        if (offer.getStatus() == ServiceStatus.ENCERRADO) {
            throw new ApiException(HttpStatus.CONFLICT, "ALREADY_CLOSED", "Serviço já encerrado.");
        }
        offer.close();
        return ServiceResponse.from(offer);
    }
    @Transactional
    public ServiceResponse changeStatus(Long id, UserAccount actor, ServiceStatus next) {
        ServiceOffer offer = locked(id);
        if (!offer.getProvider().getId().equals(actor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_OWNER", "Somente o prestador pode alterar a situação.");
        }
        if (offer.getStatus() == ServiceStatus.ENCERRADO || next == ServiceStatus.ENCERRADO) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_STATUS_CHANGE",
                    "Use a operação de encerramento; serviço encerrado não pode ser reativado.");
        }
        offer.changeStatus(next);
        return ServiceResponse.from(offer);
    }
    public ServiceOffer locked(Long id) {
        return services.findLockedById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "SERVICE_NOT_FOUND", "Serviço não encontrado."));
    }
}
