package com.corhuila.microservices.customer_microservice.exceptions;

import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.corhuila.microservices.common_exceptions.ErrorResponse;
import com.corhuila.microservices.common_exceptions.GlobalExceptionHandler;
import com.corhuila.microservices.customer_microservice.security.CustomerSecurityException;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.corhuila.microservices.customer_microservice")
@Primary
@Slf4j
public class CustomerExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle (CustomerNotFoundException exception)
    {
        var errors = new HashMap<String, String>();
        var fieldName = "customer";
        errors.put(fieldName, exception.getMessage());

        log.warn("Customer not found: {}", exception.toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
    }

    @ExceptionHandler(CustomerSecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(CustomerSecurityException ex) {
        Map<String, String> errors = Map.of("message", ex.getMessage());
        log.warn("Security exception: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(errors));
    }

}