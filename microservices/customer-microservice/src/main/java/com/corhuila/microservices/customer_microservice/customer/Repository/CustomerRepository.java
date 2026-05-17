package com.corhuila.microservices.customer_microservice.customer.Repository;

import com.corhuila.microservices.customer_microservice.customer.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
	Optional<Customer> findByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
	Optional<Customer> findFirstByDocumentTypeAndDocumentNumberOrderByIdDesc(String documentType, String documentNumber);
	List<Customer> findAllByDocumentTypeAndDocumentNumberOrderByIdDesc(String documentType, String documentNumber);

	// Tenant-aware methods
	Optional<Customer> findByIdAndTenantId(String id, String tenantId);
	List<Customer> findAllByTenantId(String tenantId);
	Optional<Customer> findFirstByDocumentTypeAndDocumentNumberAndTenantIdOrderByIdDesc(String documentType, String documentNumber, String tenantId);
	List<Customer> findAllByDocumentTypeAndDocumentNumberAndTenantIdOrderByIdDesc(String documentType, String documentNumber, String tenantId);
}
