package com.corhuila.microservices.invoice_microservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InvoiceGenerateItemRequest(
        @NotNull Integer productId,
        @NotNull String productName,
        @NotNull @Positive Integer quantity,
        @NotNull BigDecimal unitPrice,
        @NotNull BigDecimal lineTotal
) {
}

