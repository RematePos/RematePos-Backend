package com.corhuila.microservices.auth_microservice.repository;

import com.corhuila.microservices.auth_microservice.model.TenantMembership;
import com.corhuila.microservices.auth_microservice.model.TenantStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantMembershipRepository extends JpaRepository<TenantMembership, Long> {
    List<TenantMembership> findByUserIdAndActiveTrueAndTenantStatus(Long userId, TenantStatus tenantStatus);

    List<TenantMembership> findByTenantId(Long tenantId);

    Optional<TenantMembership> findByUserIdAndTenantId(Long userId, Long tenantId);

    Optional<TenantMembership> findByTenantIdAndUserId(Long tenantId, Long userId);

    Optional<TenantMembership> findByUserIdAndTenantIdAndActiveTrueAndTenantStatus(
            Long userId,
            Long tenantId,
            TenantStatus tenantStatus
    );

    boolean existsByTenantIdAndUserId(Long tenantId, Long userId);

    long countByTenantIdAndRoleNameAndActiveTrue(Long tenantId, String roleName);
}
