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
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @PostMapping
    public ResponseEntity<String> createCustomer(@Valid @RequestBody CustomerRequest request){
        return ResponseEntity.ok(service.saveCustomer(request));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable("customerId") String customerId) {
        return ResponseEntity.ok(service.getCustomerById(customerId));//
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getCustomers() {
        return ResponseEntity.ok(service.getCustomers());
    }

    @PutMapping
    public ResponseEntity<Void> updateCustomer(@Valid @RequestBody  CustomerRequest request) {
        service.saveCustomer(request);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomerById(@PathVariable("customerId") String customerId) {
        service.deleteCustomerById(customerId);
        return ResponseEntity.accepted().build();
    }


}