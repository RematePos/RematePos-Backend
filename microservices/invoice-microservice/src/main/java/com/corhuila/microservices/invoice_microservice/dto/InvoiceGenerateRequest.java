package com.corhuila.microservices.invoice_microservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceGenerateRequest(
        @NotNull Long purchaseId,
        @NotNull String customerId,
        @NotNull String customerDocumentType,
        @NotNull String customerDocumentNumber,
        @NotNull String customerFullName,
        @NotNull BigDecimal subtotal,
        @NotNull BigDecimal tax,
        @NotNull BigDecimal total,
        @NotEmpty List<@Valid InvoiceGenerateItemRequest> items
) {
}

