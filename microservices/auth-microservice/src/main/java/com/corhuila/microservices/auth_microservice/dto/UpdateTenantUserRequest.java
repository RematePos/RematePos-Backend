package com.corhuila.microservices.auth_microservice.dto;

import jakarta.validation.constraints.Email;

public record UpdateTenantUserRequest(
        @Email String email,
        String fullName,
        Boolean active,
        String role
) {
}
