package com.corhuila.microservices.invoice_microservice.service;

import com.corhuila.microservices.invoice_microservice.dto.InvoiceGenerateRequest;
import com.corhuila.microservices.invoice_microservice.dto.InvoiceGenerateResponse;
import com.corhuila.microservices.invoice_microservice.dto.InvoiceResponse;

import java.util.List;

public interface InvoiceService {

    InvoiceGenerateResponse generate(InvoiceGenerateRequest request);

    InvoiceResponse getById(Long id);

    InvoiceResponse getByPurchaseId(Long purchaseId);

    InvoiceResponse getByInvoiceNumber(String invoiceNumber);

    List<InvoiceResponse> getByCustomerId(String customerId);

    List<InvoiceResponse> getByDocument(String documentType, String documentNumber);

    List<InvoiceResponse> getRecent(int limit);
}

