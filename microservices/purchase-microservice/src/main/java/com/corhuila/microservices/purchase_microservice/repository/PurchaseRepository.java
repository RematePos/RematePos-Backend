package com.corhuila.microservices.purchase_microservice.repository;

import com.corhuila.microservices.purchase_microservice.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}

