package com.corhuila.microservices.purchase_microservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentWebhookRequest(
        @NotBlank(message = "Reference is required")
        String reference,

        String providerTransactionId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        String currency,

        @NotBlank(message = "Status is required")
        String status,

        String signature
) {
}
