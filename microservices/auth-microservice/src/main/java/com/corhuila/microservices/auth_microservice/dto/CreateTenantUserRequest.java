package com.corhuila.microservices.auth_microservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTenantUserRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        String fullName,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String role
) {
}
