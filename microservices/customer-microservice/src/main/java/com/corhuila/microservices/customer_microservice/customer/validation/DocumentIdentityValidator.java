package com.corhuila.microservices.customer_microservice.customer.validation;
import com.corhuila.microservices.customer_microservice.customer.dto.CustomerRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class DocumentIdentityValidator implements ConstraintValidator<ValidDocumentIdentity, CustomerRequest> {
    @Override
    public boolean isValid(CustomerRequest value, ConstraintValidatorContext context) {
        if (value == null || isBlank(value.documentType()) || isBlank(value.documentNumber())) {
            return true;
        }
        var documentType = DocumentType.parse(value.documentType());
        if (documentType.isEmpty()) {
            return true;
        }
        if (documentType.get().isDocumentNumberValid(value.documentNumber())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("documentNumber")
                .addConstraintViolation();
        return false;
    }
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}