package com.corhuila.microservices.customer_microservice.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        String tokenType,
        Long expiresInSeconds,
        String username
) {
}
