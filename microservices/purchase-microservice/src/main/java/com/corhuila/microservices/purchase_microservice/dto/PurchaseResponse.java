package com.corhuila.microservices.purchase_microservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PurchaseResponse(
        Long purchaseId,
        String customerId,
        String customerDocumentType,
        String customerDocumentNumber,
        String customerFullName,
        String status,
        String paymentStatus,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        BigDecimal paidAmount,
        Instant createdAt,
        String notes,
        List<PurchaseItemResponse> items
) {
}

