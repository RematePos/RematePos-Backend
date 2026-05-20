package com.corhuila.microservices.invoice_microservice.billing.provider;

import com.corhuila.microservices.invoice_microservice.dto.InvoiceResponse;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlanubeSandboxBillingProviderTest {

    @Test
    void mockDianProviderStillReturnsSimulatedValidation() {
        MockDianBillingProvider provider = new MockDianBillingProvider();
        ReflectionTestUtils.setField(provider, "demoLegend", "Documento demo");

        BillingProviderResponse response = provider.issueInvoice(baseRequest());

        assertEquals("MOCK_DIAN", response.provider());
        assertEquals(BillingProviderStatus.VALIDATED_SIMULATED, response.status());
        assertNotNull(response.cufe());
    }

    @Test
    void sandboxProviderFailsSafelyWhenTokenIsMissing() {
        AlanubeSandboxBillingProvider provider = new AlanubeSandboxBillingProvider();
        ReflectionTestUtils.setField(provider, "baseUrl", "https://sandbox-api.example.com/e-provider/co/v1");
        ReflectionTestUtils.setField(provider, "username", "example@example.com");
        ReflectionTestUtils.setField(provider, "token", "");

        BillingProviderResponse response = provider.issueInvoice(baseRequest());

        assertEquals("ALANUBE_SANDBOX", response.provider());
        assertEquals(BillingProviderStatus.PROVIDER_FAILED, response.status());
        assertEquals("Sandbox billing provider configuration is incomplete", response.validationMessage());
    }

    @Test
    void sandboxProviderDoesNotExposeTokenInFailureMessages() {
        AlanubeSandboxBillingProvider provider = new AlanubeSandboxBillingProvider();
        ReflectionTestUtils.setField(provider, "baseUrl", "::::");
        ReflectionTestUtils.setField(provider, "username", "example@example.com");
        ReflectionTestUtils.setField(provider, "token", "redacted-value");

        BillingProviderResponse response = provider.issueInvoice(baseRequest());

        assertEquals(BillingProviderStatus.PROVIDER_FAILED, response.status());
        assertEquals("Sandbox billing provider request failed", response.validationMessage());
        assertFalse(response.validationMessage().contains("redacted-value"));
    }

    @Test
    void sandboxProviderMapsHttpErrorsAsProviderFailed() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/invoices", exchange -> {
            byte[] body = "{\"message\":\"bad request\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            AlanubeSandboxBillingProvider provider = new AlanubeSandboxBillingProvider();
            ReflectionTestUtils.setField(provider, "baseUrl", "http://127.0.0.1:" + server.getAddress().getPort());
            ReflectionTestUtils.setField(provider, "username", "example@example.com");
            ReflectionTestUtils.setField(provider, "token", "redacted-value");
            ReflectionTestUtils.setField(provider, "timeoutMs", 1000);

            BillingProviderResponse response = provider.issueInvoice(baseRequest());

            assertEquals(BillingProviderStatus.PROVIDER_FAILED, response.status());
            assertEquals("Sandbox billing provider returned an error", response.validationMessage());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void sandboxProviderMapsOptionalProviderArtifactsWhenPresent() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/invoices", exchange -> {
            byte[] body = """
                    {
                      "status": "validated",
                      "reference": "SANDBOX-123",
                      "cufe": "CUFE-123",
                      "cude": "CUDE-123",
                      "qrCode": "QR-123",
                      "xml": "<xml/>",
                      "pdfUrl": "https://sandbox.example.test/invoice.pdf",
                      "message": "Validated in sandbox"
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            AlanubeSandboxBillingProvider provider = new AlanubeSandboxBillingProvider();
            ReflectionTestUtils.setField(provider, "baseUrl", "http://127.0.0.1:" + server.getAddress().getPort());
            ReflectionTestUtils.setField(provider, "username", "example@example.com");
            ReflectionTestUtils.setField(provider, "token", "redacted-value");
            ReflectionTestUtils.setField(provider, "timeoutMs", 1000);

            BillingProviderResponse response = provider.issueInvoice(baseRequest());

            assertEquals(BillingProviderStatus.VALIDATED, response.status());
            assertEquals("SANDBOX-123", response.providerReference());
            assertEquals("CUFE-123", response.cufe());
            assertEquals("CUDE-123", response.cude());
            assertEquals("QR-123", response.qrCode());
            assertEquals("<xml/>", response.xmlContent());
            assertEquals("https://sandbox.example.test/invoice.pdf", response.pdfUrl());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void invoiceResponseDoesNotExposeProviderSecrets() {
        List<String> componentNames = Arrays.stream(InvoiceResponse.class.getRecordComponents())
                .map(component -> component.getName().toLowerCase())
                .toList();

        assertFalse(componentNames.contains("token"));
        assertFalse(componentNames.contains("password"));
        assertFalse(componentNames.contains("secret"));
    }

    private BillingProviderRequest baseRequest() {
        return new BillingProviderRequest(
                "INV-20260520-1",
                1L,
                "cust-1",
                "CC",
                "123456",
                "Cliente Uno",
                new BigDecimal("10000.00"),
                new BigDecimal("1900.00"),
                new BigDecimal("11900.00"),
                Instant.parse("2026-05-20T12:00:00Z"),
                List.of(new BillingProviderRequest.LineItem(
                        1,
                        "Producto Uno",
                        2,
                        new BigDecimal("5000.00"),
                        new BigDecimal("10000.00")
                ))
        );
    }
}
