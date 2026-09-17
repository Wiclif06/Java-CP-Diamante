package br.edu.campusgigs.api;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import br.edu.campusgigs.api.dto.*;
import br.edu.campusgigs.security.CurrentUser;
import br.edu.campusgigs.service.*;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final CurrentUser current;
    private final OfferService offers;
    private final ContractService contracts;
    public ServiceController(CurrentUser current, OfferService offers, ContractService contracts) {
        this.current = current;
        this.offers = offers;
        this.contracts = contracts;
    }
    @GetMapping
    public List<ServiceResponse> list() { return offers.list(); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse publish(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ServiceRequest request) {
        return offers.publish(current.from(jwt), request);
    }
    @PutMapping("/{id}")
    public ServiceResponse edit(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
                                @Valid @RequestBody ServiceRequest request) {
        return offers.edit(id, current.from(jwt), request);
    }
    @PatchMapping("/{id}/close")
    public ServiceResponse close(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return offers.close(id, current.from(jwt));
    }
    @PatchMapping("/{id}/status")
    public ServiceResponse changeStatus(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
                                        @Valid @RequestBody StatusRequest request) {
        return offers.changeStatus(id, current.from(jwt), request.status());
    }
    @PostMapping("/{id}/contracts")
    @ResponseStatus(HttpStatus.CREATED)
    public ContractResponse hire(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return contracts.hire(id, current.from(jwt));
    }
}
