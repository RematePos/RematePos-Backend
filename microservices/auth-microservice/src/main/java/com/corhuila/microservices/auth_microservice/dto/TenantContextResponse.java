package com.corhuila.microservices.auth_microservice.dto;

public record TenantContextResponse(
        Long tenantId,
        String tenantName,
        String tenantSlug
) {
}
