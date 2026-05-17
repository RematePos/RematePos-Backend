package com.corhuila.microservices.invoice_microservice.security;

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
