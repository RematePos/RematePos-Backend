package com.corhuila.microservices.purchase_microservice.controller;

import com.corhuila.microservices.purchase_microservice.security.PurchaseSecurityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice(basePackages = "com.corhuila.microservices.purchase_microservice")
public class PurchaseExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(PurchaseSecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurity(PurchaseSecurityException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(Map.of("message", exception.getMessage()));
    }
}
