package com.corhuila.microservices.product_microservice.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        Integer id,
        
        @NotNull(message = "Product Name cannot be null")
        @NotBlank(message = "Product Name cannot be blank")
        @Size(min = 2, max = 255, message = "Product Name must be between 2 and 255 characters")
        String name,
        
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,
        
        @NotNull(message = "Price cannot be null")
        @Positive(message = "Price must be greater than 0")
        Double price,
        
        @PositiveOrZero(message = "Stock cannot be negative")
        Integer stock,
        
        String imageUrl,
        
        @NotNull(message = "Category ID cannot be null")
        @Positive(message = "Category ID must be positive")
        Integer categoryId
) {

}
