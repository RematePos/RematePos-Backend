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
            var customer = repository.save(mapper.toCustomer(request));
            return customer.getId();
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

}

