package com.corhuila.microservices.invoice_microservice.billing.provider;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;

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
        return new BillingProviderResolver(mockDian, alanube, factus);
    }
}
