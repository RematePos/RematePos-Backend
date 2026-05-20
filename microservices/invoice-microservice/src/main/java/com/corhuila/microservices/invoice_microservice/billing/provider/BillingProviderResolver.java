package com.corhuila.microservices.invoice_microservice.billing.provider;

import com.corhuila.microservices.invoice_microservice.model.TenantBillingSettings;
import com.corhuila.microservices.invoice_microservice.repository.TenantBillingSettingsRepository;
import com.corhuila.microservices.invoice_microservice.security.BillingSettingsTokenProtector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class BillingProviderResolver {

    private static final String MOCK_DIAN = "MOCK_DIAN";
    private static final String ALANUBE_SANDBOX = "ALANUBE_SANDBOX";
    private static final String FACTUS_SANDBOX = "FACTUS_SANDBOX";

    private final MockDianBillingProvider mockDianBillingProvider;
    private final AlanubeSandboxBillingProvider alanubeSandboxBillingProvider;
    private final FactusBillingProvider factusBillingProvider;
    private final TenantBillingSettingsRepository tenantBillingSettingsRepository;
    private final BillingSettingsTokenProtector tokenProtector;

    @Value("${billing.provider:MOCK_DIAN}")
    private String configuredProvider;

    public BillingProviderResolver(
            MockDianBillingProvider mockDianBillingProvider,
            AlanubeSandboxBillingProvider alanubeSandboxBillingProvider,
            FactusBillingProvider factusBillingProvider,
            TenantBillingSettingsRepository tenantBillingSettingsRepository,
            BillingSettingsTokenProtector tokenProtector
    ) {
        this.mockDianBillingProvider = mockDianBillingProvider;
        this.alanubeSandboxBillingProvider = alanubeSandboxBillingProvider;
        this.factusBillingProvider = factusBillingProvider;
        this.tenantBillingSettingsRepository = tenantBillingSettingsRepository;
        this.tokenProtector = tokenProtector;
    }

    public BillingProvider resolve() {
        String provider = configuredProvider == null
                ? MOCK_DIAN
                : configuredProvider.trim().toUpperCase(Locale.ROOT);

        return switch (provider) {
            case ALANUBE_SANDBOX -> alanubeSandboxBillingProvider;
            case FACTUS_SANDBOX -> factusBillingProvider;
            case "FACTUS_PRODUCTION" -> throw new IllegalStateException("FACTUS_PRODUCTION is reserved and not enabled yet.");
            case MOCK_DIAN, "" -> mockDianBillingProvider;
            default -> throw new IllegalStateException("Unsupported billing provider: " + provider);
        };
    }

    public BillingProvider resolve(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return resolve();
        }
        return tenantBillingSettingsRepository.findByTenantId(tenantId)
                .filter(settings -> Boolean.TRUE.equals(settings.getEnabled()))
                .map(this::fromTenantSettings)
                .orElseGet(this::resolve);
    }

    private BillingProvider fromTenantSettings(TenantBillingSettings settings) {
        String provider = normalizeProvider(settings.getProvider());
        return switch (provider) {
            case MOCK_DIAN -> mockDianBillingProvider;
            case ALANUBE_SANDBOX -> alanubeFromTenantSettings(settings);
            case FACTUS_SANDBOX -> factusBillingProvider;
            case "FACTUS_PRODUCTION" -> failed(provider, settings.getProviderEnvironment(), "Billing provider is not enabled for production");
            default -> failed(provider, settings.getProviderEnvironment(), "Unsupported tenant billing provider");
        };
    }

    private BillingProvider alanubeFromTenantSettings(TenantBillingSettings settings) {
        if (isBlank(settings.getBaseUrl()) || isBlank(settings.getUsername()) || isBlank(settings.getTokenEncrypted())) {
            return failed(ALANUBE_SANDBOX, settings.getProviderEnvironment(), "Tenant billing provider configuration is incomplete");
        }
        try {
            String token = tokenProtector.decrypt(settings.getTokenEncrypted());
            AlanubeSandboxProviderConfig config = new AlanubeSandboxProviderConfig(
                    settings.getBaseUrl(),
                    settings.getUsername(),
                    token,
                    settings.getTimeoutMs(),
                    settings.getProviderEnvironment()
            );
            return new BillingProvider() {
                @Override
                public BillingProviderResponse issueInvoice(BillingProviderRequest request) {
                    return alanubeSandboxBillingProvider.issueInvoice(request, config);
                }

                @Override
                public String getProviderName() {
                    return ALANUBE_SANDBOX;
                }

                @Override
                public boolean supportsSandbox() {
                    return true;
                }
            };
        } catch (IllegalArgumentException ex) {
            return failed(ALANUBE_SANDBOX, settings.getProviderEnvironment(), "Tenant billing provider token cannot be used");
        }
    }

    private BillingProvider failed(String provider, String environment, String message) {
        return new FailedBillingProvider(provider, isBlank(environment) ? "DEMO" : environment, message);
    }

    private String normalizeProvider(String provider) {
        return provider == null ? MOCK_DIAN : provider.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
