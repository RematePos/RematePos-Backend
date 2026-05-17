package com.corhuila.microservices.auth_microservice.repository;

import com.corhuila.microservices.auth_microservice.model.TenantMembership;
import com.corhuila.microservices.auth_microservice.model.TenantStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantMembershipRepository extends JpaRepository<TenantMembership, Long> {
    List<TenantMembership> findByUserIdAndActiveTrueAndTenantStatus(Long userId, TenantStatus tenantStatus);

    Optional<TenantMembership> findByUserIdAndTenantId(Long userId, Long tenantId);

    Optional<TenantMembership> findByUserIdAndTenantIdAndActiveTrueAndTenantStatus(
            Long userId,
            Long tenantId,
            TenantStatus tenantStatus
    );
}
