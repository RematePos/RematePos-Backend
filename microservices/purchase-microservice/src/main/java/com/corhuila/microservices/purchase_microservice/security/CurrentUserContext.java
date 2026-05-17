package com.corhuila.microservices.purchase_microservice.security;

import java.util.Set;

public record CurrentUserContext(
        String userId,
        String username,
        Set<String> roles,
        Set<String> permissions,
        String tenantId,
        String tenantSlug
) {
}
