package com.corhuila.microservices.purchase_microservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PurchaseCheckoutRequest(
        @NotBlank(message = "Document type is required")
        String documentType,

        @NotBlank(message = "Document number is required")
        String documentNumber,

        @NotEmpty(message = "Purchase must include at least one item")
        List<@Valid PurchaseItemRequest> items,

        String notes
) {
}

