package com.corhuila.microservices.invoice_microservice.dto;

public record InvoiceGenerateResponse(
		Long invoiceId,
		String invoiceNumber,
		String provider,
		String providerEnvironment,
		String providerStatus,
		String cufe,
		String cude,
		String qrCode,
		String pdfUrl,
		Boolean fiscalValid,
		String validationMessage
) {
}

