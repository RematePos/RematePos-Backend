package com.corhuila.microservices.invoice_microservice.service;

import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProviderRequest;
import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProviderResolver;
import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProviderResponse;
import com.corhuila.microservices.invoice_microservice.dto.BillingSettingsRequest;
import com.corhuila.microservices.invoice_microservice.dto.BillingSettingsResponse;
import com.corhuila.microservices.invoice_microservice.dto.BillingSettingsTestResponse;
import com.corhuila.microservices.invoice_microservice.model.TenantBillingSettings;
import com.corhuila.microservices.invoice_microservice.repository.TenantBillingSettingsRepository;
import com.corhuila.microservices.invoice_microservice.security.BillingSettingsTokenProtector;
import com.corhuila.microservices.invoice_microservice.security.CurrentUserContext;
import com.corhuila.microservices.invoice_microservice.security.InvoiceSecurityException;
import com.corhuila.microservices.invoice_microservice.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TenantBillingSettingsService {

    private static final String MOCK_DIAN = "MOCK_DIAN";
    private static final String ALANUBE_SANDBOX = "ALANUBE_SANDBOX";
    private static final String FACTUS_SANDBOX = "FACTUS_SANDBOX";
    private static final String BUSINESS_OWNER = "BUSINESS_OWNER";
    private static final String BUSINESS_ADMIN = "BUSINESS_ADMIN";
    private static final String BUSINESS_SETTINGS_UPDATE = "BUSINESS_SETTINGS_UPDATE";

    private final TenantBillingSettingsRepository repository;
    private final SecurityContextHelper securityContextHelper;
    private final BillingSettingsTokenProtector tokenProtector;
    private final BillingProviderResolver billingProviderResolver;

    @Transactional(readOnly = true)
    public BillingSettingsResponse getCurrentSettings() {
        CurrentUserContext context = requireBillingSettingsAccess();
        return repository.findByTenantId(context.tenantId())
                .map(this::toResponse)
                .orElseGet(this::defaultResponse);
    }

    @Transactional
    public BillingSettingsResponse upsertCurrentSettings(BillingSettingsRequest request) {
        CurrentUserContext context = requireBillingSettingsAccess();
        String provider = normalizeProvider(request.provider());
        String providerEnvironment = normalizeEnvironment(request.providerEnvironment());
        boolean enabled = request.enabled() == null || request.enabled();

        TenantBillingSettings settings = repository.findByTenantId(context.tenantId()).orElseGet(() -> {
            TenantBillingSettings created = new TenantBillingSettings();
            created.setTenantId(context.tenantId());
            return created;
        });

        boolean hasExistingToken = !isBlank(settings.getTokenEncrypted());
        boolean hasIncomingToken = !isBlank(request.token());
        validateRequest(request, provider, enabled, hasExistingToken, hasIncomingToken);

        settings.setProvider(provider);
        settings.setProviderEnvironment(providerEnvironment);
        settings.setEnabled(enabled);
        settings.setBaseUrl(blankToNull(request.baseUrl()));
        settings.setUsername(blankToNull(request.username()));
        settings.setTimeoutMs(safeTimeoutMs(request.timeoutMs()));
        if (hasIncomingToken) {
            settings.setTokenEncrypted(tokenProtector.encrypt(request.token().trim()));
        }

        return toResponse(repository.save(settings), hasIncomingToken ? request.token().trim() : null);
    }

    @Transactional
    public BillingSettingsTestResponse testCurrentSettings() {
        CurrentUserContext context = requireBillingSettingsAccess();
        BillingProviderResponse response = billingProviderResolver.resolve(context.tenantId())
                .issueInvoice(testRequest());

        repository.findByTenantId(context.tenantId()).ifPresent(settings -> {
            settings.setLastTestStatus(response.status() == null ? null : response.status().name());
            settings.setLastTestMessage(response.validationMessage());
            repository.save(settings);
        });

        return new BillingSettingsTestResponse(
                response.status() == null ? null : response.status().name(),
                response.validationMessage(),
                response.provider(),
                response.environment()
        );
    }

    private CurrentUserContext requireBillingSettingsAccess() {
        CurrentUserContext context = securityContextHelper.requireTenant();
        if (context.roles().contains(BUSINESS_OWNER)) {
            return context;
        }
        if (context.roles().contains(BUSINESS_ADMIN) && context.permissions().contains(BUSINESS_SETTINGS_UPDATE)) {
            return context;
        }
        throw new InvoiceSecurityException(HttpStatus.FORBIDDEN, "Billing settings access is not allowed");
    }

    private void validateRequest(
            BillingSettingsRequest request,
            String provider,
            boolean enabled,
            boolean hasExistingToken,
            boolean hasIncomingToken
    ) {
        if (!List.of(MOCK_DIAN, ALANUBE_SANDBOX, FACTUS_SANDBOX).contains(provider)) {
            throw new IllegalArgumentException("Unsupported billing provider");
        }
        if (request.timeoutMs() != null && request.timeoutMs() <= 0) {
            throw new IllegalArgumentException("Timeout must be greater than zero");
        }
        if (ALANUBE_SANDBOX.equals(provider) && enabled) {
            if (isBlank(request.baseUrl()) || isBlank(request.username())) {
                throw new IllegalArgumentException("Sandbox provider base URL and username are required");
            }
            if (!hasExistingToken && !hasIncomingToken) {
                throw new IllegalArgumentException("Sandbox provider token is required");
            }
            if (hasIncomingToken && !tokenProtector.canEncrypt()) {
                throw new IllegalArgumentException("Billing settings encryption key is not configured");
            }
        }
    }

    private BillingSettingsResponse defaultResponse() {
        return new BillingSettingsResponse(
                MOCK_DIAN,
                "DEMO",
                false,
                null,
                null,
                false,
                null,
                10000,
                null,
                "Tenant billing settings are not configured. MOCK_DIAN fallback is active.",
                null
        );
    }

    private BillingSettingsResponse toResponse(TenantBillingSettings settings) {
        return toResponse(settings, null);
    }

    private BillingSettingsResponse toResponse(TenantBillingSettings settings, String visibleToken) {
        boolean tokenConfigured = !isBlank(settings.getTokenEncrypted());
        return new BillingSettingsResponse(
                settings.getProvider(),
                settings.getProviderEnvironment(),
                settings.getEnabled(),
                settings.getBaseUrl(),
                settings.getUsername(),
                tokenConfigured,
                !isBlank(visibleToken) ? tokenProtector.mask(visibleToken) : (tokenConfigured ? "************" : null),
                settings.getTimeoutMs(),
                settings.getLastTestStatus(),
                settings.getLastTestMessage(),
                settings.getUpdatedAt()
        );
    }

    private BillingProviderRequest testRequest() {
        return new BillingProviderRequest(
                "TEST-BILLING-SETTINGS",
                0L,
                "test-customer",
                "CC",
                "000000",
                "Cliente de prueba",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Instant.now(),
                List.of()
        );
    }

    private String normalizeProvider(String provider) {
        return isBlank(provider) ? MOCK_DIAN : provider.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeEnvironment(String providerEnvironment) {
        return isBlank(providerEnvironment) ? "DEMO" : providerEnvironment.trim().toUpperCase(Locale.ROOT);
    }

    private Integer safeTimeoutMs(Integer timeoutMs) {
        return timeoutMs == null ? 10000 : timeoutMs;
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
