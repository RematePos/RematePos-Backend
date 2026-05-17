package com.corhuila.gateway.security;

import java.util.List;

public record JwtClaims(
        String userId,
        String username,
        String tenantId,
        String tenantSlug,
        List<String> roles,
        List<String> permissions
) {
}
