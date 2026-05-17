package com.corhuila.microservices.customer_microservice.security;

import org.springframework.http.HttpStatus;

public class CustomerSecurityException extends RuntimeException {

    private final HttpStatus status;

    public CustomerSecurityException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
