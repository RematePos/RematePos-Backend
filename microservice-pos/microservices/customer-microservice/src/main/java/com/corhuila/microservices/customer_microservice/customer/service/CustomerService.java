package com.corhuila.microservices.customer_microservice.customer.service;


import com.corhuila.microservices.customer_microservice.customer.dto.CustomerRequest;
import com.corhuila.microservices.customer_microservice.customer.dto.CustomerResponse;

import java.util.List;

public interface CustomerService {
    String saveCustomer(CustomerRequest request);
    CustomerResponse getCustomerById(String customerId);
    List<CustomerResponse> getCustomers();
    void deleteCustomerById(String customerId);
}
