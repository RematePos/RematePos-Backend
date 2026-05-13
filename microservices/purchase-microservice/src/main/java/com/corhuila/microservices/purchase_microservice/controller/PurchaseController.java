package com.corhuila.microservices.purchase_microservice.controller;

import com.corhuila.microservices.purchase_microservice.dto.PurchaseCheckoutRequest;
import com.corhuila.microservices.purchase_microservice.dto.ElectronicPaymentRequest;
import com.corhuila.microservices.purchase_microservice.dto.PaymentWebhookRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchasePaymentRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseReturnRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseReturnResponse;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseResponse;
import com.corhuila.microservices.purchase_microservice.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService service;

    @PostMapping("/checkout")
    public ResponseEntity<PurchaseResponse> checkout(@Valid @RequestBody PurchaseCheckoutRequest request) {
        return ResponseEntity.ok(service.checkout(request));
    }

    @PostMapping("/{purchaseId}/pay")
    public ResponseEntity<PurchaseResponse> registerPayment(
            @PathVariable Long purchaseId,
            @Valid @RequestBody PurchasePaymentRequest request
    ) {
        return ResponseEntity.ok(service.registerPayment(purchaseId, request));
    }

    @PostMapping("/{purchaseId}/gateway-payment")
    public ResponseEntity<PurchaseResponse> createElectronicPayment(
            @PathVariable Long purchaseId,
            @Valid @RequestBody ElectronicPaymentRequest request
    ) {
        return ResponseEntity.ok(service.createElectronicPayment(purchaseId, request));
    }

    @PostMapping("/payments/webhook/sandbox")
    public ResponseEntity<PurchaseResponse> processSandboxWebhook(
            @Valid @RequestBody PaymentWebhookRequest request
    ) {
        return ResponseEntity.ok(service.processPaymentWebhook(request));
    }

    @PatchMapping("/{purchaseId}/cancel")
    public ResponseEntity<PurchaseResponse> cancel(@PathVariable Long purchaseId) {
        return ResponseEntity.ok(service.cancel(purchaseId));
    }

    @PostMapping("/returns")
    public ResponseEntity<PurchaseReturnResponse> registerReturn(
            @Valid @RequestBody PurchaseReturnRequest request
    ) {
        return ResponseEntity.ok(service.registerReturn(request));
    }

    @GetMapping("/{purchaseId}")
    public ResponseEntity<PurchaseResponse> getById(@PathVariable Long purchaseId) {
        return ResponseEntity.ok(service.getById(purchaseId));
    }

    @GetMapping("/invoice/{invoiceNumber}")
    public ResponseEntity<PurchaseResponse> getByInvoiceNumber(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(service.getByInvoiceNumber(invoiceNumber));
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<List<PurchaseResponse>> getByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(service.getByCustomerId(customerId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PurchaseResponse>> getByDocument(
            @RequestParam("type") String documentType,
            @RequestParam("number") String documentNumber
    ) {
        return ResponseEntity.ok(service.getByDocument(documentType, documentNumber));
    }
}
