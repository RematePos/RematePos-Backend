package com.corhuila.microservices.purchase_microservice.client;

public record ProductQuantityRequest(
        Integer productId,
        Integer quantity
) {
}

