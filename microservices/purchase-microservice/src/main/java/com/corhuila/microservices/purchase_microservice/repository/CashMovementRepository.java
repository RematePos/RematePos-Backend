package com.corhuila.microservices.purchase_microservice.repository;

import com.corhuila.microservices.purchase_microservice.model.CashMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {
}
