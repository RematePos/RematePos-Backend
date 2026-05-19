package com.corhuila.microservices.invoice_microservice.billing.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class FactusBillingProvider implements BillingProvider {

    private static final String PROVIDER = "FACTUS";
    private static final String NOT_CONFIGURED = "Factus sandbox credentials are not configured.";
    private static final String NOT_IMPLEMENTED = "Factus provider adapter is ready, but real provider calls are not implemented yet.";

    @Value("${billing.environment:DEMO}")
    private String billingEnvironment;

    @Value("${billing.factus.api-base-url:}")
    private String apiBaseUrl;

    @Value("${billing.factus.api-key:}")
    private String apiKey;

    @Value("${billing.factus.client-id:}")
    private String clientId;

    @Value("${billing.factus.client-secret:}")
    private String clientSecret;

    @Override
    public BillingProviderResponse issueInvoice(BillingProviderRequest request) {
        String message = hasCredentials() ? NOT_IMPLEMENTED : NOT_CONFIGURED;
        return new BillingProviderResponse(
                PROVIDER,
                billingEnvironment,
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
        return PROVIDER;
    }

    @Override
    public boolean supportsSandbox() {
        return true;
    }

    private boolean hasCredentials() {
        return !isBlank(apiBaseUrl) && !isBlank(apiKey) && !isBlank(clientId) && !isBlank(clientSecret);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
