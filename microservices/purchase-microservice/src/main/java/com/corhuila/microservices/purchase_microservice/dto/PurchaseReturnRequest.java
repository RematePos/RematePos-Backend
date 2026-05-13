package com.corhuila.microservices.purchase_microservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PurchaseReturnRequest(
        @NotBlank(message = "Invoice number is required")
        String invoiceNumber,

        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID must be positive")
        Integer productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity,

        @NotBlank(message = "Return reason is required")
        String reason
) {
}
