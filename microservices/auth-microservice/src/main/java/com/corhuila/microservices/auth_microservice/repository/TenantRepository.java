package com.corhuila.microservices.auth_microservice.repository;

import com.corhuila.microservices.auth_microservice.model.Tenant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
