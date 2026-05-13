package com.corhuila.microservices.purchase_microservice.client;

public record ProductClientResponse(
        Integer id,
        String name,
        String description,
        Double price,
        Integer stock,
        String imageUrl,
        Integer categoryId,
        String categoryName,
        String categoryDescription
) {
}

