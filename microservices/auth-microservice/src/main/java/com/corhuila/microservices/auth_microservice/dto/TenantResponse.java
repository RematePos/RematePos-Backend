package com.corhuila.microservices.auth_microservice.dto;

public record TenantResponse(
        Long tenantId,
        String tenantName,
        String tenantSlug,
        String tenantStatus
) {
}
