package com.corhuila.microservices.auth_microservice.repository;

import com.corhuila.microservices.auth_microservice.model.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}
