package com.corhuila.microservices.invoice_microservice.billing.provider;

public record AlanubeSandboxProviderConfig(
        String baseUrl,
        String username,
        String token,
        Integer timeoutMs,
        String environment
) {
}
