package com.corhuila.microservices.auth_microservice.dto;

import java.util.Set;

public record UserSessionResponse(
        Long id,
        String username,
        String email,
        String fullName,
        TenantContextResponse tenant,
        Set<String> roles,
        Set<String> permissions,
        Set<String> platformRoles,
        Set<TenantMembershipResponse> memberships
) {
}
