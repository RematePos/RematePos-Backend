package com.corhuila.microservices.purchase_microservice.client;

public record InvoiceGenerateResponse(
        Long invoiceId,
        String invoiceNumber
) {
}

