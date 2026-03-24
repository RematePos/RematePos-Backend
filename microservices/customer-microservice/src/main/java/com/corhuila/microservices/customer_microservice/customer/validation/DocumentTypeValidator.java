package com.corhuila.microservices.customer_microservice.customer.validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class DocumentTypeValidator implements ConstraintValidator<ValidDocumentType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return DocumentType.parse(value).isPresent();
    }
}