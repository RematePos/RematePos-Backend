package com.corhuila.microservices.product_microservice.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductQuantityRequest(
        @NotNull(message = "Product ID cannot be null")
        @Positive(message = "Product ID must be positive")
        Integer productId,
        
        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity
) {

}