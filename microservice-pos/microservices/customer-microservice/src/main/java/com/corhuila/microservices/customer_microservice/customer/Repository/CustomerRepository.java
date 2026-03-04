package com.corhuila.microservices.customer_microservice.customer.Repository;

import com.corhuila.microservices.customer_microservice.customer.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {
}
