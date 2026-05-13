package com.corhuila.microservices.purchase_microservice.dto;

import java.time.Instant;

public record PurchaseReturnResponse(
        Long purchaseId,
        String invoiceNumber,
        Integer productId,
        String productName,
        Integer returnedQuantity,
        Integer totalReturnedQuantity,
        Integer purchasedQuantity,
        String reason,
        Instant returnedAt,
        String message
) {
}
