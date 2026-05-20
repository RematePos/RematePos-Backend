package com.corhuila.microservices.invoice_microservice.billing.provider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

final class TestBillingRequests {

    private TestBillingRequests() {
    }

    static BillingProviderRequest baseRequest() {
        return new BillingProviderRequest(
                "INV-TEST-1",
                1L,
                "cust-1",
                "CC",
                "123456",
                "Cliente Uno",
                new BigDecimal("10000.00"),
                new BigDecimal("1900.00"),
                new BigDecimal("11900.00"),
                Instant.parse("2026-05-20T12:00:00Z"),
                List.of(new BillingProviderRequest.LineItem(
                        1,
                        "Producto Uno",
                        2,
                        new BigDecimal("5000.00"),
                        new BigDecimal("10000.00")
                ))
        );
    }
}
