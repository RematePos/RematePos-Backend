package com.corhuila.microservices.purchase_microservice.dto;

import java.math.BigDecimal;

public record PurchaseItemResponse(
        Integer productId,
        String productName,
        String categoryName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}

