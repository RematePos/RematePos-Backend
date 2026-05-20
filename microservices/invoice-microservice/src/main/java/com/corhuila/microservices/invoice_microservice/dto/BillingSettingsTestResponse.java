package com.corhuila.microservices.invoice_microservice.dto;

public record BillingSettingsTestResponse(
        String status,
        String message,
        String provider,
        String providerEnvironment
) {
}
