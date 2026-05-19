package com.corhuila.microservices.invoice_microservice.billing.provider;

public enum BillingProviderStatus {
    VALIDATED_SIMULATED,
    PENDING,
    VALIDATED,
    REJECTED,
    PROVIDER_FAILED
}
