package br.com.ticketsexpress.tickets_express_api.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        UserRole role
) {
    @JsonCreator
    public static RegisterRequest of(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("password") String password,
            @JsonProperty("role") UserRole role
    ) {
        return new RegisterRequest(name, email, password, role);
    }
}
