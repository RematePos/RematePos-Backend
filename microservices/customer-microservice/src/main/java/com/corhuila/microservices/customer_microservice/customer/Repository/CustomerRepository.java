package com.corhuila.microservices.customer_microservice.customer.Repository;

import com.corhuila.microservices.customer_microservice.customer.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
	Optional<Customer> findByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
}
