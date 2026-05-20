package com.corhuila.microservices.invoice_microservice.billing.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Component
public class AlanubeSandboxBillingProvider implements BillingProvider {

    private static final String PROVIDER = "ALANUBE_SANDBOX";
    private static final String ENVIRONMENT = "SANDBOX";
    private static final String NOT_CONFIGURED = "Sandbox billing provider configuration is incomplete";
    private static final String REQUEST_FAILED = "Sandbox billing provider request failed";
    private static final String PROVIDER_RETURNED_ERROR = "Sandbox billing provider returned an error";
    private static final String RESPONSE_PROCESSING_FAILED = "Sandbox billing provider response could not be processed";
    private static final String DEFAULT_ENDPOINT_PATH = "/invoices";

    private final AlanubeSandboxInvoiceMapper invoiceMapper;
    private final ObjectMapper objectMapper;

    @Value("${billing.provider-base-url:}")
    private String baseUrl;

    @Value("${billing.provider-username:}")
    private String username;

    @Value("${billing.provider-token:}")
    private String token;

    @Value("${billing.provider-timeout-ms:10000}")
    private int timeoutMs;

    public AlanubeSandboxBillingProvider() {
        this(new AlanubeSandboxInvoiceMapper(), new ObjectMapper());
    }

    AlanubeSandboxBillingProvider(AlanubeSandboxInvoiceMapper invoiceMapper, ObjectMapper objectMapper) {
        this.invoiceMapper = invoiceMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public BillingProviderResponse issueInvoice(BillingProviderRequest request) {
        if (!hasCredentials()) {
            return failed(NOT_CONFIGURED);
        }

        try {
            Map<String, Object> payload = invoiceMapper.toPayload(request);
            String requestBody = objectMapper.writeValueAsString(payload);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(safeTimeoutMs()))
                    .build();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(normalizedBaseUrl() + DEFAULT_ENDPOINT_PATH))
                    .timeout(Duration.ofMillis(safeTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .header("X-Provider-Username", username)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return failed(PROVIDER_RETURNED_ERROR);
            }

            try {
                return fromSuccessResponse(response.body());
            } catch (IOException ex) {
                return failed(RESPONSE_PROCESSING_FAILED);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return failed(REQUEST_FAILED);
        } catch (IllegalArgumentException | IOException ex) {
            return failed(REQUEST_FAILED);
        }
    }

    @Override
    public String getProviderName() {
        return PROVIDER;
    }

    @Override
    public boolean supportsSandbox() {
        return true;
    }

    private BillingProviderResponse fromSuccessResponse(String responseBody) throws IOException {
        JsonNode root = isBlank(responseBody) ? objectMapper.createObjectNode() : objectMapper.readTree(responseBody);
        BillingProviderStatus status = mapStatus(firstText(root, "providerStatus", "status", "state", "validationStatus"));
        return new BillingProviderResponse(
                PROVIDER,
                ENVIRONMENT,
                status,
                firstText(root, "providerReference", "reference", "id", "uuid", "number"),
                firstText(root, "cufe", "CUFE"),
                firstText(root, "cude", "CUDE"),
                firstText(root, "qrCode", "qr", "qrUrl"),
                firstText(root, "xmlContent", "xml", "xmlUrl"),
                firstText(root, "pdfUrl", "pdf", "pdfLink"),
                firstText(root, "validationMessage", "message", "description"),
                Instant.now(),
                false
        );
    }

    private BillingProviderStatus mapStatus(String providerStatus) {
        if (isBlank(providerStatus)) {
            return BillingProviderStatus.VALIDATED;
        }

        String normalized = providerStatus.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("REJECT")) {
            return BillingProviderStatus.REJECTED;
        }
        if (normalized.contains("PENDING") || normalized.contains("PROCESS")) {
            return BillingProviderStatus.PENDING;
        }
        if (normalized.contains("FAIL") || normalized.contains("ERROR")) {
            return BillingProviderStatus.PROVIDER_FAILED;
        }
        return BillingProviderStatus.VALIDATED;
    }

    private String firstText(JsonNode root, String... fields) {
        for (String field : fields) {
            JsonNode value = root.findValue(field);
            if (value != null && !value.isNull() && !isBlank(value.asText())) {
                return value.asText();
            }
        }
        return null;
    }

    private BillingProviderResponse failed(String message) {
        return new BillingProviderResponse(
                PROVIDER,
                ENVIRONMENT,
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

    private boolean hasCredentials() {
        return !isBlank(baseUrl) && !isBlank(username) && !isBlank(token);
    }

    private String normalizedBaseUrl() {
        String trimmed = baseUrl.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private int safeTimeoutMs() {
        return timeoutMs <= 0 ? 10000 : timeoutMs;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
