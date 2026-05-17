package com.corhuila.microservices.purchase_microservice.security;

import org.springframework.http.HttpStatus;

public class PurchaseSecurityException extends RuntimeException {

    private final HttpStatus status;

    public PurchaseSecurityException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
