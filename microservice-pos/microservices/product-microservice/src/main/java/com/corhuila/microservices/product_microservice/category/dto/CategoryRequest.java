package com.corhuila.microservices.product_microservice.category.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryRequest(
        Integer id,
        @NotNull(message = "Category name is required")
        String name,
        String description
) {

}
