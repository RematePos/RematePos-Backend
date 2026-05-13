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
        String paymentMethod,
        String paymentProvider,
        String paymentReference,
        String providerTransactionId,
        String providerStatus,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        BigDecimal paidAmount,
        BigDecimal cashReceived,
        BigDecimal changeAmount,
        Long cashRegisterSessionId,
        Long invoiceId,
        String invoiceNumber,
        Instant createdAt,
        Instant paidAt,
        String notes,
        String paymentEvidenceNote,
        List<PurchaseItemResponse> items
) {
}

