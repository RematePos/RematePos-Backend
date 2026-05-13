package com.corhuila.microservices.customer_microservice.customer.service.impl;


import com.corhuila.microservices.customer_microservice.customer.Repository.CustomerRepository;
import com.corhuila.microservices.customer_microservice.customer.dto.CustomerRequest;
import com.corhuila.microservices.customer_microservice.customer.dto.CustomerResponse;
import com.corhuila.microservices.customer_microservice.customer.mapper.CustomerMapper;
import com.corhuila.microservices.customer_microservice.customer.service.CustomerService;
import com.corhuila.microservices.customer_microservice.exceptions.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    public String saveCustomer(CustomerRequest request) {
        var customer = mapper.toCustomer(request);
        customer.setDocumentType(normalizeDocumentType(customer.getDocumentType()));
        customer.setDocumentNumber(normalizeDocumentNumber(customer.getDocumentNumber()));

        if (isBlank(customer.getId()) && !isBlank(customer.getDocumentType()) && !isBlank(customer.getDocumentNumber())) {
            repository
                    .findFirstByDocumentTypeAndDocumentNumberOrderByIdDesc(
                            customer.getDocumentType(),
                            customer.getDocumentNumber())
                    .ifPresent(existingCustomer -> customer.setId(existingCustomer.getId()));
        }

        var savedCustomer = repository.save(customer);
        return savedCustomer.getId();
    }

    @Override
    public CustomerResponse getCustomerById(String customerId) {
        return repository
                .findById(customerId)
                .map(mapper::toCustomerResponse)
                .orElseThrow(()-> new CustomerNotFoundException(
                        String.format("Customer with id %s not found", customerId)
                ));
    }

    @Override
    public CustomerResponse getCustomerByDocument(String documentType, String documentNumber) {
        var normalizedDocumentType = normalizeDocumentType(documentType);
        var normalizedDocumentNumber = normalizeDocumentNumber(documentNumber);

        return repository
                .findFirstByDocumentTypeAndDocumentNumberOrderByIdDesc(normalizedDocumentType, normalizedDocumentNumber)
                .map(mapper::toCustomerResponse)
                .orElseThrow(() -> new CustomerNotFoundException(
                        String.format("Customer with document %s-%s not found", normalizedDocumentType, normalizedDocumentNumber)
                ));
    }

    @Override
    public List<CustomerResponse> getCustomers() {
        return repository
                .findAll().stream()
                .map(mapper::toCustomerResponse)
                .toList();
    }

    @Override
    public void deleteCustomerById(String customerId) {
        repository
                .findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        String.format("Customer with id %s not found", customerId)));
        repository.deleteById(customerId);
    }

    private String normalizeDocumentType(String documentType) {
        return documentType == null ? null : documentType.trim().toUpperCase();
    }

    private String normalizeDocumentNumber(String documentNumber) {
        return documentNumber == null ? null : documentNumber.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
