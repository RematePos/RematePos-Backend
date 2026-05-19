package com.corhuila.microservices.invoice_microservice.billing.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class BillingProviderResolver {

    private static final String MOCK_DIAN = "MOCK_DIAN";
    private static final String FACTUS_SANDBOX = "FACTUS_SANDBOX";

    private final MockDianBillingProvider mockDianBillingProvider;
    private final FactusBillingProvider factusBillingProvider;

    @Value("${billing.provider:MOCK_DIAN}")
    private String configuredProvider;

    public BillingProviderResolver(
            MockDianBillingProvider mockDianBillingProvider,
            FactusBillingProvider factusBillingProvider
    ) {
        this.mockDianBillingProvider = mockDianBillingProvider;
        this.factusBillingProvider = factusBillingProvider;
    }

    public BillingProvider resolve() {
        String provider = configuredProvider == null
                ? MOCK_DIAN
                : configuredProvider.trim().toUpperCase(Locale.ROOT);

        return switch (provider) {
            case FACTUS_SANDBOX -> factusBillingProvider;
            case "FACTUS_PRODUCTION" -> throw new IllegalStateException("FACTUS_PRODUCTION is reserved and not enabled yet.");
            case MOCK_DIAN, "" -> mockDianBillingProvider;
            default -> throw new IllegalStateException("Unsupported billing provider: " + provider);
        };
    }
}
