package com.corhuila.microservices.invoice_microservice.dto;

public record InvoiceGenerateResponse(
		Long invoiceId,
		String invoiceNumber
) {
}

