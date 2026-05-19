package com.corhuila.microservices.auth_microservice.dto;

public record TenantUserResponse(
        Long userId,
        String username,
        String email,
        String fullName,
        Long tenantId,
        String tenantSlug,
        String role,
        boolean active
) {
}
