package com.corhuila.microservices.invoice_microservice.billing.provider;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlanubeSandboxInvoiceMapper {

    private static final DateTimeFormatter ISSUED_AT_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public Map<String, Object> toPayload(BillingProviderRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("internalInvoiceNumber", request.invoiceNumber());
        payload.put("purchaseId", request.purchaseId());
        payload.put("issuedAt", request.issuedAt() == null ? null : ISSUED_AT_FORMATTER.format(request.issuedAt().atOffset(ZoneOffset.UTC)));
        payload.put("customer", customerPayload(request));
        payload.put("items", itemPayload(request.items()));
        payload.put("totals", totalsPayload(request));
        return payload;
    }

    private Map<String, Object> customerPayload(BillingProviderRequest request) {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("id", request.customerId());
        customer.put("documentType", request.customerDocumentType());
        customer.put("documentNumber", request.customerDocumentNumber());
        customer.put("fullName", request.customerFullName());
        return customer;
    }

    private List<Map<String, Object>> itemPayload(List<BillingProviderRequest.LineItem> items) {
        return items.stream()
                .map(item -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("productId", item.productId());
                    row.put("description", item.productName());
                    row.put("quantity", item.quantity());
                    row.put("unitPrice", safeAmount(item.unitPrice()));
                    row.put("lineTotal", safeAmount(item.lineTotal()));
                    return row;
                })
                .toList();
    }

    private Map<String, Object> totalsPayload(BillingProviderRequest request) {
        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("subtotal", safeAmount(request.subtotal()));
        totals.put("tax", safeAmount(request.tax()));
        totals.put("total", safeAmount(request.total()));
        return totals;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
