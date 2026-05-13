package com.corhuila.microservices.invoice_microservice.controller;

import com.corhuila.microservices.invoice_microservice.dto.InvoiceGenerateRequest;
import com.corhuila.microservices.invoice_microservice.dto.InvoiceGenerateResponse;
import com.corhuila.microservices.invoice_microservice.dto.InvoiceResponse;
import com.corhuila.microservices.invoice_microservice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @PostMapping("/generate")
    public ResponseEntity<InvoiceGenerateResponse> generate(@Valid @RequestBody InvoiceGenerateRequest request) {
        return ResponseEntity.ok(service.generate(request));
    }

    @GetMapping("/purchase/{purchaseId}")
    public ResponseEntity<InvoiceResponse> getByPurchaseId(@PathVariable Long purchaseId) {
        return ResponseEntity.ok(service.getByPurchaseId(purchaseId));
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<InvoiceResponse> getByInvoiceNumber(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(service.getByInvoiceNumber(invoiceNumber));
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<List<InvoiceResponse>> getByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(service.getByCustomerId(customerId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<InvoiceResponse>> getByDocument(
            @RequestParam("type") String documentType,
            @RequestParam("number") String documentNumber
    ) {
        return ResponseEntity.ok(service.getByDocument(documentType, documentNumber));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<InvoiceResponse>> getRecent(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(service.getRecent(limit));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(service.getById(invoiceId));
    }
}

