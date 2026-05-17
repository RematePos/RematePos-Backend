package com.corhuila.microservices.customer_microservice.customer.service.impl;


import com.corhuila.microservices.customer_microservice.customer.Repository.CustomerRepository;
import com.corhuila.microservices.customer_microservice.security.SecurityContextHelper;
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
    private final SecurityContextHelper securityContextHelper;

    @Override
    public String saveCustomer(CustomerRequest request) {
        var customer = mapper.toCustomer(request);
        customer.setDocumentType(normalizeDocumentType(customer.getDocumentType()));
        customer.setDocumentNumber(normalizeDocumentNumber(customer.getDocumentNumber()));
        String tenantId = securityContextHelper.getTenantId();

        // preserve tenant on updates, require tenant on create/update flows
        if (!isBlank(customer.getId())) {
            // update flow - validate existence within tenant
            repository.findByIdAndTenantId(customer.getId(), tenantId)
                .orElseThrow(() -> new com.corhuila.microservices.customer_microservice.exceptions.CustomerNotFoundException(
                    String.format("Customer with id %s not found", customer.getId())
                ));
            customer.setTenantId(tenantId);
        } else {
            // create flow - check duplicates within tenant
            if (!isBlank(customer.getDocumentType()) && !isBlank(customer.getDocumentNumber())) {
            repository
                .findFirstByDocumentTypeAndDocumentNumberAndTenantIdOrderByIdDesc(
                    customer.getDocumentType(),
                    customer.getDocumentNumber(),
                    tenantId)
                .ifPresent(existingCustomer -> customer.setId(existingCustomer.getId()));
            }
            customer.setTenantId(tenantId);
        }

        var savedCustomer = repository.save(customer);
        return savedCustomer.getId();
    }

    @Override
    public CustomerResponse getCustomerById(String customerId) {
        String tenantId = securityContextHelper.getTenantId();
        return repository
            .findByIdAndTenantId(customerId, tenantId)
            .map(mapper::toCustomerResponse)
            .orElseThrow(() -> new CustomerNotFoundException(
                String.format("Customer with id %s not found", customerId)
            ));
    }

    @Override
    public CustomerResponse getCustomerByDocument(String documentType, String documentNumber) {
        var normalizedDocumentType = normalizeDocumentType(documentType);
        var normalizedDocumentNumber = normalizeDocumentNumber(documentNumber);
        String tenantId = securityContextHelper.getTenantId();

        return repository
            .findFirstByDocumentTypeAndDocumentNumberAndTenantIdOrderByIdDesc(normalizedDocumentType, normalizedDocumentNumber, tenantId)
            .map(mapper::toCustomerResponse)
            .orElseThrow(() -> new CustomerNotFoundException(
                String.format("Customer with document %s-%s not found", normalizedDocumentType, normalizedDocumentNumber)
            ));
    }

    @Override
    public List<CustomerResponse> getCustomers() {
        String tenantId = securityContextHelper.getTenantId();
        return repository
            .findAllByTenantId(tenantId).stream()
            .map(mapper::toCustomerResponse)
            .toList();
    }

    @Override
    public void deleteCustomerById(String customerId) {
        String tenantId = securityContextHelper.getTenantId();
        repository
            .findByIdAndTenantId(customerId, tenantId)
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
