package com.corhuila.microservices.invoice_microservice.billing.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class MockDianBillingProvider implements BillingProvider {

    private static final String PROVIDER = "MOCK_DIAN";
    private static final String ENVIRONMENT = "DEMO";

    @Value("${billing.demo-legend:Documento generado en ambiente de demostración, sin validez tributaria.}")
    private String demoLegend;

    @Override
    public BillingProviderResponse issueInvoice(BillingProviderRequest request) {
        Instant issuedAt = request.issuedAt() == null ? Instant.now() : request.issuedAt();
        String cufe = simulatedCufe(request, issuedAt);
        String qrCode = "REMATEPOS-DEMO|invoiceNumber=" + request.invoiceNumber()
                + "|provider=" + PROVIDER
                + "|status=" + BillingProviderStatus.VALIDATED_SIMULATED;

        String xmlContent = "<InvoiceDemo>"
                + "<InvoiceNumber>" + escapeXml(request.invoiceNumber()) + "</InvoiceNumber>"
                + "<Provider>" + PROVIDER + "</Provider>"
                + "<Environment>" + ENVIRONMENT + "</Environment>"
                + "<FiscalValid>false</FiscalValid>"
                + "</InvoiceDemo>";

        return new BillingProviderResponse(
                PROVIDER,
                ENVIRONMENT,
                BillingProviderStatus.VALIDATED_SIMULATED,
                "MOCK-" + request.invoiceNumber(),
                cufe,
                null,
                qrCode,
                xmlContent,
                "demo://rematepos/invoices/" + request.invoiceNumber() + ".pdf",
                demoLegend,
                issuedAt,
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

    private String simulatedCufe(BillingProviderRequest request, Instant issuedAt) {
        String value = request.invoiceNumber() + "|" + request.purchaseId() + "|" + issuedAt;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return "SIM-CUFE-" + HexFormat.of().formatHex(hash).toUpperCase();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to generate simulated CUFE", ex);
        }
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
