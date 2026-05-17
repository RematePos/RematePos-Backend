package com.corhuila.microservices.auth_microservice.dto;

public record TenantMembershipResponse(
        Long tenantId,
        String tenantName,
        String tenantSlug,
        String role
) {
}
