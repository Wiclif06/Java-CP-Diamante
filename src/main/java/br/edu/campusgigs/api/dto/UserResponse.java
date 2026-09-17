package br.edu.campusgigs.api.dto;

import br.edu.campusgigs.domain.*;

public record UserResponse(Long id, String name, String email, Role role, String cep, String city, String uf) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(),
                user.getCep(), user.getCity(), user.getUf());
    }
}
