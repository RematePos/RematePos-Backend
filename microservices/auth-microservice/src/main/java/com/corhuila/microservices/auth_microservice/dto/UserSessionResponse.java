package com.corhuila.microservices.auth_microservice.dto;

import java.util.Set;

public record UserSessionResponse(
        Long id,
        String username,
        String email,
        String fullName,
        Set<String> roles,
        Set<String> permissions
) {
}
