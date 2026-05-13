package com.corhuila.microservices.invoice_microservice.dto;

import java.math.BigDecimal;

public record InvoiceItemResponse(
        Integer productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}

