package com.corhuila.microservices.purchase_microservice.dto;

import com.corhuila.microservices.purchase_microservice.model.PaymentMethod;
import com.corhuila.microservices.purchase_microservice.model.PaymentProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchasePaymentRequest(
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotNull(message = "Paid amount is required")
        @DecimalMin(value = "0.01", message = "Paid amount must be greater than 0")
        BigDecimal paidAmount,

        BigDecimal cashReceived,

        Long cashRegisterSessionId,

        PaymentProvider paymentProvider,

        String paymentReference,

        String providerTransactionId,

        String providerStatus,

        String evidenceNote
) {
}

