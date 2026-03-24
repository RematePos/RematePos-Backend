package com.corhuila.microservices.customer_microservice.customer.dto;


public record CustomerResponse(
        String id,
        String documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String city
)  {

}