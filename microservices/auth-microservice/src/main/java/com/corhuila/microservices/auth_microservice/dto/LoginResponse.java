package com.corhuila.microservices.auth_microservice.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserSessionResponse user
) {
}
