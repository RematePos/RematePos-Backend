package com.corhuila.microservices.product_microservice.security;

import org.springframework.http.HttpStatus;

public class ProductSecurityException extends RuntimeException {

    private final HttpStatus status;

    public ProductSecurityException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
