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
		List<InvoiceItemResponse> items
) {
}

