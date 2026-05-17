package com.corhuila.microservices.auth_microservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record RegisterRequest(
        @NotBlank String username,
        @Email @NotBlank String email,
        @NotBlank String fullName,
        @Size(min = 8) @NotBlank String password,
        Set<String> roles
) {
}
