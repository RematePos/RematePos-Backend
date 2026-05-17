package com.corhuila.microservices.customer_microservice.customer.controller;

import com.corhuila.microservices.customer_microservice.customer.dto.CustomerRequest;
import com.corhuila.microservices.customer_microservice.customer.dto.CustomerResponse;
import com.corhuila.microservices.customer_microservice.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;
    private final com.corhuila.microservices.customer_microservice.security.SecurityContextHelper securityContextHelper;

    @PostMapping
    public ResponseEntity<String> createCustomer(@Valid @RequestBody CustomerRequest request){
        securityContextHelper.requirePermission("CUSTOMERS_CREATE");
        return ResponseEntity.ok(service.saveCustomer(request));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable("customerId") String customerId) {
        securityContextHelper.requirePermission("CUSTOMERS_READ");
        return ResponseEntity.ok(service.getCustomerById(customerId));
    }

    @GetMapping("/document")
    public ResponseEntity<CustomerResponse> getCustomerByDocument(
            @RequestParam("type") String documentType,
            @RequestParam("number") String documentNumber) {
        securityContextHelper.requirePermission("CUSTOMERS_READ");
        return ResponseEntity.ok(service.getCustomerByDocument(documentType, documentNumber));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getCustomers() {
        securityContextHelper.requirePermission("CUSTOMERS_READ");
        return ResponseEntity.ok(service.getCustomers());
    }

    @PutMapping
    public ResponseEntity<Void> updateCustomer(@Valid @RequestBody  CustomerRequest request) {
        securityContextHelper.requirePermission("CUSTOMERS_UPDATE");
        service.saveCustomer(request);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomerById(@PathVariable("customerId") String customerId) {
        securityContextHelper.requirePermission("CUSTOMERS_UPDATE");
        service.deleteCustomerById(customerId);
        return ResponseEntity.accepted().build();
    }


}