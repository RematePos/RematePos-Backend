package com.corhuila.microservices.product_microservice.exceptions;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.corhuila.microservices.common_exceptions.ErrorResponse;
import com.corhuila.microservices.common_exceptions.GlobalExceptionHandler;
import com.corhuila.microservices.product_microservice.security.ProductSecurityException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice(basePackages = "com.corhuila.microservices.product_microservice")
@Primary
@Slf4j
public class ProductExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(CategoryException.class)
    public ResponseEntity<ErrorResponse> handle (CategoryException exception)
    {

        var errors = new HashMap<String, String>();
        var fieldName = "product-service";
        errors.put(fieldName, exception.getMessage());

        log.warn("Category error: {}", exception.toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));

    }

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ErrorResponse> handle (ProductException exception)
    {

        var errors = new HashMap<String, String>();
        var fieldName = "product-service";
        errors.put(fieldName, exception.getMessage());

        log.warn("Product error: {}", exception.toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));

    }

    @ExceptionHandler(ProductSecurityException.class)
    public ResponseEntity<ErrorResponse> handle(ProductSecurityException exception)
    {

        var errors = new HashMap<String, String>();
        var fieldName = "product-service";
        errors.put(fieldName, exception.getMessage());

        log.warn("Product security error: {}", exception.getMessage());

        return ResponseEntity.status(exception.getStatus()).body(new ErrorResponse(errors));

    }
}
