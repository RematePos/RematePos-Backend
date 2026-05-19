package com.corhuila.microservices.auth_microservice.dto;

public record TenantStatusUpdateResponse(
        Long tenantId,
        String tenantName,
        String tenantSlug,
        String tenantStatus
) {
}
