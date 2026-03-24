package com.corhuila.microservices.customer_microservice.customer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = DocumentIdentityValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDocumentIdentity {
    String message() default "Document number is invalid for the selected document type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

