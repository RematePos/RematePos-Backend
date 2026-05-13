package com.corhuila.microservices.purchase_microservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record PurchaseItemRequest(
        @NotNull(message = "Product ID cannot be null")
        @Positive(message = "Product ID must be positive")
        Integer productId,

        @NotBlank(message = "Product name is required")
        String productName,

        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity,

        @NotNull(message = "Unit price cannot be null")
        @Positive(message = "Unit price must be greater than 0")
        BigDecimal unitPrice
) {
}

