package com.corhuila.microservices.customer_microservice.customer.validation;

import com.corhuila.microservices.customer_microservice.customer.dto.CustomerRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerRequestValidationTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidCustomerRequest() {
        var violations = validator.validate(baseRequest("cedula de ciudadania", "123456789"));
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRequireFirstNameLastNameAndEmail() {
        var request = new CustomerRequest(
                null,
                "CC",
                "123456789",
                "",
                "",
                "",
                "3001234567",
                "Calle 1",
                "Neiva"
        );

        var violations = validator.validate(request);

        assertTrue(hasViolation(violations, "firstName"));
        assertTrue(hasViolation(violations, "lastName"));
        assertTrue(hasViolation(violations, "email"));
    }

    @Test
    void shouldRejectInvalidEmail() {
        var violations = validator.validate(new CustomerRequest(
                null,
                "CC",
                "123456789",
                "Carlo",
                "Diaz",
                "invalid-email",
                "3001234567",
                "Calle 1",
                "Neiva"
        ));

        assertTrue(hasViolation(violations, "email"));
    }

    @Test
    void shouldAllowOptionalContactFields() {
        var request = new CustomerRequest(
                null,
                "CC",
                "123456789",
                "Carlo",
                "Diaz",
                "carlo@test.com",
                "",
                "",
                ""
        );

        var violations = validator.validate(request);

        assertFalse(hasViolation(violations, "phone"));
        assertFalse(hasViolation(violations, "address"));
        assertFalse(hasViolation(violations, "city"));
    }

    private CustomerRequest baseRequest(String documentType, String documentNumber) {
        return new CustomerRequest(
                null,
                documentType,
                documentNumber,
                "Carlo",
                "Diaz",
                "carlo@test.com",
                "3001234567",
                "Calle 1",
                "Neiva"
        );
    }

    private boolean hasViolation(Iterable<? extends ConstraintViolation<?>> violations, String propertyName) {
        for (var violation : violations) {
            if (propertyName.equals(violation.getPropertyPath().toString())) {
                return true;
            }
        }
        return false;
    }
}

