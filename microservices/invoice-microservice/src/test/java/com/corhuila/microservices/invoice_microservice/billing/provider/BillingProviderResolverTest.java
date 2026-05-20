package com.corhuila.microservices.invoice_microservice.billing.provider;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.corhuila.microservices.invoice_microservice.model.TenantBillingSettings;
import com.corhuila.microservices.invoice_microservice.repository.TenantBillingSettingsRepository;
import com.corhuila.microservices.invoice_microservice.security.BillingSettingsTokenProtector;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BillingProviderResolverTest {

    @Test
    void resolvesMockDianByDefault() {
        MockDianBillingProvider mockDian = new MockDianBillingProvider();
        BillingProviderResolver resolver = resolver(mockDian, new AlanubeSandboxBillingProvider(), new FactusBillingProvider());

        ReflectionTestUtils.setField(resolver, "configuredProvider", null);

        assertSame(mockDian, resolver.resolve());
    }

    @Test
    void resolvesAlanubeSandboxWhenConfigured() {
        MockDianBillingProvider mockDian = new MockDianBillingProvider();
        AlanubeSandboxBillingProvider alanube = new AlanubeSandboxBillingProvider();
        BillingProviderResolver resolver = resolver(mockDian, alanube, new FactusBillingProvider());

        ReflectionTestUtils.setField(resolver, "configuredProvider", "ALANUBE_SANDBOX");

        assertSame(alanube, resolver.resolve());
    }

    @Test
    void resolvesTenantSpecificAlanubeSandboxSettings() {
        MockDianBillingProvider mockDian = new MockDianBillingProvider();
        AlanubeSandboxBillingProvider alanube = new AlanubeSandboxBillingProvider();
        TenantBillingSettingsRepository repository = mock(TenantBillingSettingsRepository.class);
        BillingSettingsTokenProtector tokenProtector = tokenProtector();
        TenantBillingSettings settings = alanubeSettings("tenant-a", tokenProtector.encrypt("tenant-token-value"));
        BillingProviderResolver resolver = resolver(mockDian, alanube, new FactusBillingProvider(), repository, tokenProtector);

        when(repository.findByTenantId("tenant-a")).thenReturn(Optional.of(settings));

        BillingProvider provider = resolver.resolve("tenant-a");

        assertEquals("ALANUBE_SANDBOX", provider.getProviderName());
    }

    @Test
    void tenantWithoutSettingsUsesGlobalFallback() {
        MockDianBillingProvider mockDian = new MockDianBillingProvider();
        TenantBillingSettingsRepository repository = mock(TenantBillingSettingsRepository.class);
        BillingProviderResolver resolver = resolver(mockDian, new AlanubeSandboxBillingProvider(), new FactusBillingProvider(), repository, tokenProtector());

        ReflectionTestUtils.setField(resolver, "configuredProvider", "MOCK_DIAN");
        when(repository.findByTenantId("tenant-without-settings")).thenReturn(Optional.empty());

        assertSame(mockDian, resolver.resolve("tenant-without-settings"));
    }

    @Test
    void tenantSettingsAreResolvedByCurrentTenantOnly() {
        MockDianBillingProvider mockDian = new MockDianBillingProvider();
        TenantBillingSettingsRepository repository = mock(TenantBillingSettingsRepository.class);
        BillingSettingsTokenProtector tokenProtector = tokenProtector();
        BillingProviderResolver resolver = resolver(mockDian, new AlanubeSandboxBillingProvider(), new FactusBillingProvider(), repository, tokenProtector);

        when(repository.findByTenantId("tenant-a")).thenReturn(Optional.of(alanubeSettings("tenant-a", tokenProtector.encrypt("tenant-a-token"))));
        when(repository.findByTenantId("tenant-b")).thenReturn(Optional.empty());

        assertEquals("ALANUBE_SANDBOX", resolver.resolve("tenant-a").getProviderName());
        assertSame(mockDian, resolver.resolve("tenant-b"));
    }

    @Test
    void incompleteTenantAlanubeSettingsReturnControlledFailureProvider() {
        TenantBillingSettingsRepository repository = mock(TenantBillingSettingsRepository.class);
        TenantBillingSettings settings = alanubeSettings("tenant-a", null);
        BillingProviderResolver resolver = resolver(new MockDianBillingProvider(), new AlanubeSandboxBillingProvider(), new FactusBillingProvider(), repository, tokenProtector());

        when(repository.findByTenantId("tenant-a")).thenReturn(Optional.of(settings));

        BillingProviderResponse response = resolver.resolve("tenant-a").issueInvoice(TestBillingRequests.baseRequest());

        assertEquals(BillingProviderStatus.PROVIDER_FAILED, response.status());
        assertEquals("Tenant billing provider configuration is incomplete", response.validationMessage());
    }

    @Test
    void keepsFactusSandboxReservedAdapter() {
        MockDianBillingProvider mockDian = new MockDianBillingProvider();
        FactusBillingProvider factus = new FactusBillingProvider();
        BillingProviderResolver resolver = resolver(mockDian, new AlanubeSandboxBillingProvider(), factus);

        ReflectionTestUtils.setField(resolver, "configuredProvider", "FACTUS_SANDBOX");

        assertSame(factus, resolver.resolve());
    }

    private BillingProviderResolver resolver(
            MockDianBillingProvider mockDian,
            AlanubeSandboxBillingProvider alanube,
            FactusBillingProvider factus
    ) {
        return resolver(mockDian, alanube, factus, mock(TenantBillingSettingsRepository.class), tokenProtector());
    }

    private BillingProviderResolver resolver(
            MockDianBillingProvider mockDian,
            AlanubeSandboxBillingProvider alanube,
            FactusBillingProvider factus,
            TenantBillingSettingsRepository repository,
            BillingSettingsTokenProtector tokenProtector
    ) {
        return new BillingProviderResolver(mockDian, alanube, factus, repository, tokenProtector);
    }

    private TenantBillingSettings alanubeSettings(String tenantId, String tokenEncrypted) {
        TenantBillingSettings settings = new TenantBillingSettings();
        settings.setTenantId(tenantId);
        settings.setProvider("ALANUBE_SANDBOX");
        settings.setProviderEnvironment("SANDBOX");
        settings.setEnabled(true);
        settings.setBaseUrl("https://sandbox-api.example.com/e-provider/co/v1");
        settings.setUsername("example@example.com");
        settings.setTokenEncrypted(tokenEncrypted);
        settings.setTimeoutMs(10000);
        return settings;
    }

    private BillingSettingsTokenProtector tokenProtector() {
        BillingSettingsTokenProtector protector = new BillingSettingsTokenProtector();
        ReflectionTestUtils.setField(protector, "encryptionKey", "1234567890123456");
        return protector;
    }
}
