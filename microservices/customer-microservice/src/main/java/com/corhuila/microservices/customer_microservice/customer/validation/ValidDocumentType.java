package com.corhuila.microservices.customer_microservice.customer.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Documented
@Constraint(validatedBy = DocumentTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDocumentType {
    String message() default "Document type is invalid. Allowed values: CC, DNI, TI, PAS, RC, CE";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
