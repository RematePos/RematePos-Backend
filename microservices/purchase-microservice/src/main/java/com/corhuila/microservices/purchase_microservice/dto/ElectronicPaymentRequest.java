package com.corhuila.microservices.purchase_microservice.dto;

import com.corhuila.microservices.purchase_microservice.model.PaymentMethod;
import com.corhuila.microservices.purchase_microservice.model.PaymentProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ElectronicPaymentRequest(
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        PaymentProvider paymentProvider,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount
) {
}
