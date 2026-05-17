package com.corhuila.microservices.invoice_microservice.security;

import org.springframework.http.HttpStatus;

public class InvoiceSecurityException extends RuntimeException {

    private final HttpStatus status;

    public InvoiceSecurityException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
