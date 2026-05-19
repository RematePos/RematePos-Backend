package com.corhuila.microservices.invoice_microservice.billing.provider;

import java.time.Instant;

public record BillingProviderResponse(
        String provider,
        String environment,
        BillingProviderStatus status,
        String providerReference,
        String cufe,
        String cude,
        String qrCode,
        String xmlContent,
        String pdfUrl,
        String validationMessage,
        Instant issuedAt,
        Boolean fiscalValid
) {
}
