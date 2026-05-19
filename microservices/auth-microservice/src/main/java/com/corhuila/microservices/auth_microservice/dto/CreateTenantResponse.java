package com.corhuila.microservices.auth_microservice.dto;

public record CreateTenantResponse(
        Long tenantId,
        String tenantName,
        String tenantSlug,
        String tenantStatus,
        Long ownerUserId,
        String ownerUsername,
        String ownerRole
) {
}
