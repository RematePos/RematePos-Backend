package com.corhuila.microservices.purchase_microservice.client;

public record CustomerClientResponse(
        String id,
        String documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String city
) {
}

