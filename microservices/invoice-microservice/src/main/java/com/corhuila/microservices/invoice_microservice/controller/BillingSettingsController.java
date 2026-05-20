package com.corhuila.microservices.invoice_microservice.controller;

import com.corhuila.microservices.invoice_microservice.dto.BillingSettingsRequest;
import com.corhuila.microservices.invoice_microservice.dto.BillingSettingsResponse;
import com.corhuila.microservices.invoice_microservice.dto.BillingSettingsTestResponse;
import com.corhuila.microservices.invoice_microservice.service.TenantBillingSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing/settings")
@RequiredArgsConstructor
public class BillingSettingsController {

    private final TenantBillingSettingsService service;

    @GetMapping
    public ResponseEntity<BillingSettingsResponse> getCurrentSettings() {
        return ResponseEntity.ok(service.getCurrentSettings());
    }

    @PutMapping
    public ResponseEntity<BillingSettingsResponse> upsertCurrentSettings(@RequestBody BillingSettingsRequest request) {
        return ResponseEntity.ok(service.upsertCurrentSettings(request));
    }

    @PostMapping("/test")
    public ResponseEntity<BillingSettingsTestResponse> testCurrentSettings() {
        return ResponseEntity.ok(service.testCurrentSettings());
    }
}
