package com.corhuila.microservices.invoice_microservice.service;

import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProvider;
import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProviderResolver;
import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProviderResponse;
import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProviderStatus;
import com.corhuila.microservices.invoice_microservice.dto.BillingSettingsRequest;
import com.corhuila.microservices.invoice_microservice.model.TenantBillingSettings;
import com.corhuila.microservices.invoice_microservice.repository.TenantBillingSettingsRepository;
import com.corhuila.microservices.invoice_microservice.security.BillingSettingsTokenProtector;
import com.corhuila.microservices.invoice_microservice.security.CurrentUserContext;
import com.corhuila.microservices.invoice_microservice.security.InvoiceSecurityException;
import com.corhuila.microservices.invoice_microservice.security.SecurityContextHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantBillingSettingsServiceTest {

    @Mock
    private TenantBillingSettingsRepository repository;

    @Mock
    private SecurityContextHelper securityContextHelper;

    @Mock
    private BillingProviderResolver billingProviderResolver;

    @Mock
    private BillingProvider billingProvider;

    @Test
    void tenantWithoutSettingsGetsMockDianFallbackResponse() {
        TenantBillingSettingsService service = service();
        when(securityContextHelper.requireTenant()).thenReturn(ownerContext());
        when(repository.findByTenantId("tenant-1")).thenReturn(Optional.empty());

        var response = service.getCurrentSettings();

        assertEquals("MOCK_DIAN", response.provider());
        assertFalse(response.tokenConfigured());
    }

    @Test
    void businessOwnerCanUpdateAlanubeSettingsAndTokenIsMasked() {
        TenantBillingSettingsService service = service();
        when(securityContextHelper.requireTenant()).thenReturn(ownerContext());
        when(repository.findByTenantId("tenant-1")).thenReturn(Optional.empty());
        when(repository.save(any(TenantBillingSettings.class))).thenAnswer(invocation -> {
            TenantBillingSettings settings = invocation.getArgument(0);
            settings.setUpdatedAt(Instant.parse("2026-05-20T12:00:00Z"));
            return settings;
        });

        var response = service.upsertCurrentSettings(alanubeRequest("sandbox-token-1234"));

        assertEquals("ALANUBE_SANDBOX", response.provider());
        assertTrue(response.tokenConfigured());
        assertEquals("************1234", response.tokenMasked());
        assertNotEquals("sandbox-token-1234", response.tokenMasked());
    }

    @Test
    void businessAdminWithPermissionCanUpdateSettings() {
        TenantBillingSettingsService service = service();
        when(securityContextHelper.requireTenant()).thenReturn(adminContext());
        when(repository.findByTenantId("tenant-1")).thenReturn(Optional.empty());
        when(repository.save(any(TenantBillingSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.upsertCurrentSettings(new BillingSettingsRequest(
                "MOCK_DIAN",
                "DEMO",
                true,
                null,
                null,
                null,
                10000
        ));

        assertEquals("MOCK_DIAN", response.provider());
    }

    @Test
    void cashierCannotUpdateSettings() {
        TenantBillingSettingsService service = service();
        when(securityContextHelper.requireTenant()).thenReturn(cashierContext());

        assertThrows(InvoiceSecurityException.class, () -> service.upsertCurrentSettings(alanubeRequest("sandbox-token-1234")));
    }

    @Test
    void platformAdminWithoutTenantIsBlockedByTenantRequirement() {
        TenantBillingSettingsService service = service();
        when(securityContextHelper.requireTenant()).thenThrow(new InvoiceSecurityException(org.springframework.http.HttpStatus.FORBIDDEN, "Tenant context is required"));

        assertThrows(InvoiceSecurityException.class, service::getCurrentSettings);
    }

    @Test
    void tokenEmptyKeepsPreviousEncryptedToken() {
        BillingSettingsTokenProtector protector = tokenProtector();
        TenantBillingSettingsService service = service(protector);
        TenantBillingSettings existing = existingAlanubeSettings(protector.encrypt("previous-token-9999"));
        when(securityContextHelper.requireTenant()).thenReturn(ownerContext());
        when(repository.findByTenantId("tenant-1")).thenReturn(Optional.of(existing));
        when(repository.save(any(TenantBillingSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.upsertCurrentSettings(alanubeRequest(""));

        assertEquals("previous-token-9999", protector.decrypt(existing.getTokenEncrypted()));
    }

    @Test
    void alanubeWithoutTokenFailsValidationWhenNoPreviousTokenExists() {
        TenantBillingSettingsService service = service();
        when(securityContextHelper.requireTenant()).thenReturn(ownerContext());
        when(repository.findByTenantId("tenant-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.upsertCurrentSettings(alanubeRequest(null)));
    }

    @Test
    void testSettingsWithoutTokenReturnsProviderFailedControlled() {
        TenantBillingSettingsService service = service();
        TenantBillingSettings existing = existingAlanubeSettings(null);
        when(securityContextHelper.requireTenant()).thenReturn(ownerContext());
        when(billingProviderResolver.resolve("tenant-1")).thenReturn(billingProvider);
        when(billingProvider.issueInvoice(any())).thenReturn(new BillingProviderResponse(
                "ALANUBE_SANDBOX",
                "SANDBOX",
                BillingProviderStatus.PROVIDER_FAILED,
                null,
                null,
                null,
                null,
                null,
                null,
                "Tenant billing provider configuration is incomplete",
                Instant.now(),
                false
        ));
        when(repository.findByTenantId("tenant-1")).thenReturn(Optional.of(existing));
        when(repository.save(any(TenantBillingSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.testCurrentSettings();

        assertEquals("PROVIDER_FAILED", response.status());
        assertEquals("Tenant billing provider configuration is incomplete", response.message());
    }

    private TenantBillingSettingsService service() {
        return service(tokenProtector());
    }

    private TenantBillingSettingsService service(BillingSettingsTokenProtector tokenProtector) {
        return new TenantBillingSettingsService(repository, securityContextHelper, tokenProtector, billingProviderResolver);
    }

    private BillingSettingsTokenProtector tokenProtector() {
        BillingSettingsTokenProtector protector = new BillingSettingsTokenProtector();
        ReflectionTestUtils.setField(protector, "encryptionKey", "1234567890123456");
        return protector;
    }

    private BillingSettingsRequest alanubeRequest(String token) {
        return new BillingSettingsRequest(
                "ALANUBE_SANDBOX",
                "SANDBOX",
                true,
                "https://sandbox-api.example.com/e-provider/co/v1",
                "example@example.com",
                token,
                10000
        );
    }

    private TenantBillingSettings existingAlanubeSettings(String encryptedToken) {
        TenantBillingSettings settings = new TenantBillingSettings();
        settings.setTenantId("tenant-1");
        settings.setProvider("ALANUBE_SANDBOX");
        settings.setProviderEnvironment("SANDBOX");
        settings.setEnabled(true);
        settings.setBaseUrl("https://sandbox-api.example.com/e-provider/co/v1");
        settings.setUsername("example@example.com");
        settings.setTokenEncrypted(encryptedToken);
        settings.setTimeoutMs(10000);
        return settings;
    }

    private CurrentUserContext ownerContext() {
        return new CurrentUserContext("1", "owner", Set.of("BUSINESS_OWNER"), Set.of("BUSINESS_SETTINGS_UPDATE"), "tenant-1", "tenant-one");
    }

    private CurrentUserContext adminContext() {
        return new CurrentUserContext("2", "admin", Set.of("BUSINESS_ADMIN"), Set.of("BUSINESS_SETTINGS_UPDATE"), "tenant-1", "tenant-one");
    }

    private CurrentUserContext cashierContext() {
        return new CurrentUserContext("3", "cashier", Set.of("CASHIER"), Set.of("INVOICES_READ"), "tenant-1", "tenant-one");
    }
}
