package com.corhuila.microservices.purchase_microservice.client;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceGenerateRequest(
        Long purchaseId,
        String customerId,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerFullName,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        List<InvoiceGenerateItemRequest> items
) {
}

