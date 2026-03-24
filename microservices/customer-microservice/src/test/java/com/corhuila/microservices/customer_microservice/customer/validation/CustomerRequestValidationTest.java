package com.corhuila.microservices.customer_microservice.customer.validation;

import com.corhuila.microservices.customer_microservice.customer.dto.CustomerRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    void shouldAcceptAliasAndValidNumberForCc() {
        var violations = validator.validate(baseRequest("cedula de ciudadania", "123456789"));
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectUnknownDocumentType() {
        var violations = validator.validate(baseRequest("RUT", "123456789"));
        assertTrue(violations.stream().anyMatch(v -> "documentType".equals(v.getPropertyPath().toString())));
    }

    @Test
    void shouldRejectInvalidNumberForPassport() {
        var violations = validator.validate(baseRequest("PAS", "12-ABC"));
        assertTrue(violations.stream().anyMatch(v -> "documentNumber".equals(v.getPropertyPath().toString())));
    }

    @Test
    void shouldRequirePhoneAddressAndCity() {
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

        assertTrue(violations.stream().anyMatch(v -> "phone".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "address".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "city".equals(v.getPropertyPath().toString())));
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
}

