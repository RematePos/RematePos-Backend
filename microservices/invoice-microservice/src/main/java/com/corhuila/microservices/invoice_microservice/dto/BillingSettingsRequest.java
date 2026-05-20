package com.corhuila.microservices.invoice_microservice.dto;

public record BillingSettingsRequest(
        String provider,
        String providerEnvironment,
        Boolean enabled,
        String baseUrl,
        String username,
        String token,
        Integer timeoutMs
) {
}
