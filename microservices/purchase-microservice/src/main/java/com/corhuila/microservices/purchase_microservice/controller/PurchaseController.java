package com.corhuila.microservices.purchase_microservice.controller;

import com.corhuila.microservices.purchase_microservice.dto.PurchaseCheckoutRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseResponse;
import com.corhuila.microservices.purchase_microservice.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService service;

    @PostMapping("/checkout")
    public ResponseEntity<PurchaseResponse> checkout(@Valid @RequestBody PurchaseCheckoutRequest request) {
        return ResponseEntity.ok(service.checkout(request));
    }
}
