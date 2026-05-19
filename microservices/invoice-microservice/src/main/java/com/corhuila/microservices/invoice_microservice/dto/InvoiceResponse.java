package com.corhuila.microservices.invoice_microservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record InvoiceResponse(
		Long invoiceId,
		String invoiceNumber,
		Long purchaseId,
		String customerId,
		String customerDocumentType,
		String customerDocumentNumber,
		String customerFullName,
		BigDecimal subtotal,
		BigDecimal tax,
		BigDecimal total,
		Instant issuedAt,
		String provider,
		String providerEnvironment,
		String providerStatus,
		String cufe,
		String cude,
		String qrCode,
		String xmlContent,
		String pdfUrl,
		Boolean fiscalValid,
		String validationMessage,
		List<InvoiceItemResponse> items
) {
}

