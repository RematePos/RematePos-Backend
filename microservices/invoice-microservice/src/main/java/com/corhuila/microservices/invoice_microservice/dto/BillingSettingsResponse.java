package com.corhuila.microservices.invoice_microservice.dto;

import java.time.Instant;

public record BillingSettingsResponse(
        String provider,
        String providerEnvironment,
        Boolean enabled,
        String baseUrl,
        String username,
        Boolean tokenConfigured,
        String tokenMasked,
        Integer timeoutMs,
        String lastTestStatus,
        String lastTestMessage,
        Instant updatedAt
) {
}
