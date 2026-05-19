package com.corhuila.microservices.invoice_microservice.billing.provider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BillingProviderRequest(
        String invoiceNumber,
        Long purchaseId,
        String customerId,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerFullName,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        Instant issuedAt,
        List<LineItem> items
) {
    public record LineItem(
            Integer productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }
}
