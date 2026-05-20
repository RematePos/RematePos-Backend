package com.corhuila.microservices.invoice_microservice.repository;

import com.corhuila.microservices.invoice_microservice.model.TenantBillingSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantBillingSettingsRepository extends JpaRepository<TenantBillingSettings, Long> {

    Optional<TenantBillingSettings> findByTenantId(String tenantId);

    boolean existsByTenantId(String tenantId);
}
