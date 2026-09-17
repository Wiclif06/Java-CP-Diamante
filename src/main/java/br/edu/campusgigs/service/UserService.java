package br.edu.campusgigs.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import br.edu.campusgigs.api.ApiException;
import br.edu.campusgigs.api.dto.UserResponse;
import br.edu.campusgigs.domain.UserAccount;
import br.edu.campusgigs.integration.AddressLookup;
import br.edu.campusgigs.repository.UserRepository;

@Service
public class UserService {
    private final AddressLookup address;
    private final UserRepository users;
    public UserService(AddressLookup address, UserRepository users) {
        this.address = address;
        this.users = users;
    }
    @Transactional
    public UserResponse updateCep(UserAccount actor, String cep) {
        AddressLookup.AddressData data = address.lookup(cep);
        UserAccount managed = users.findById(actor.getId()).orElseThrow(() ->
                new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Usuário não existe."));
        managed.updateAddress(data.cep(), data.city(), data.uf());
        return UserResponse.from(managed);
    }
}
