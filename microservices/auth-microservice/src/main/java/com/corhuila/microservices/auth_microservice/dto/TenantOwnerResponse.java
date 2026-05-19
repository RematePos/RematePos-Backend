package com.corhuila.microservices.auth_microservice.dto;

public record TenantOwnerResponse(
        Long ownerUserId,
        String ownerUsername,
        String ownerRole
) {
}
