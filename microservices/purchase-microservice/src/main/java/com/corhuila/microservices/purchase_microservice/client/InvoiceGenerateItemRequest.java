package com.corhuila.microservices.purchase_microservice.client;

import java.math.BigDecimal;

public record InvoiceGenerateItemRequest(
        Integer productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}

