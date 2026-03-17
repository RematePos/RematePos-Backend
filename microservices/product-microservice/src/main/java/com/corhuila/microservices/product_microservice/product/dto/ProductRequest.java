package com.corhuila.microservices.product_microservice.product.dto;


import jakarta.validation.constraints.NotNull;

public record ProductRequest(
        Integer id,
        @NotNull(message = "Product Name cannot be null")
        String name,
        String description,
        @NotNull(message = "Price cannot be null")
        Double price,
        Integer stock,
        String imageUrl,
        @NotNull(message = "Category ID cannot be null")
        Integer categoryId
) {

}
