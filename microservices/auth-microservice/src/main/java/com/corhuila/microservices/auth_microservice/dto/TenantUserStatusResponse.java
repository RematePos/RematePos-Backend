package com.corhuila.microservices.auth_microservice.dto;

public record TenantUserStatusResponse(
        Long userId,
        Long tenantId,
        String role,
        boolean active
) {
}
