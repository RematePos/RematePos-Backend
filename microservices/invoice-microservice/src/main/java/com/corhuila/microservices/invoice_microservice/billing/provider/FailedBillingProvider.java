package com.corhuila.microservices.invoice_microservice.billing.provider;

import java.time.Instant;

class FailedBillingProvider implements BillingProvider {

    private final String provider;
    private final String environment;
    private final String message;

    FailedBillingProvider(String provider, String environment, String message) {
        this.provider = provider;
        this.environment = environment;
        this.message = message;
    }

    @Override
    public BillingProviderResponse issueInvoice(BillingProviderRequest request) {
        return new BillingProviderResponse(
                provider,
                environment,
                BillingProviderStatus.PROVIDER_FAILED,
                null,
                null,
                null,
                null,
                null,
                null,
                message,
                Instant.now(),
                false
        );
    }

    @Override
    public String getProviderName() {
        return provider;
    }

    @Override
    public boolean supportsSandbox() {
        return true;
    }
}
