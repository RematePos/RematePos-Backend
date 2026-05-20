package com.corhuila.microservices.invoice_microservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "tenant_billing_settings",
        uniqueConstraints = @UniqueConstraint(name = "uk_tenant_billing_settings_tenant_id", columnNames = "tenant_id")
)
@Getter
@Setter
public class TenantBillingSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_environment", nullable = false)
    private String providerEnvironment;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "base_url")
    private String baseUrl;

    private String username;

    @Column(name = "token_encrypted", columnDefinition = "TEXT")
    private String tokenEncrypted;

    @Column(name = "timeout_ms")
    private Integer timeoutMs;

    @Column(name = "last_test_status")
    private String lastTestStatus;

    @Column(name = "last_test_message", columnDefinition = "TEXT")
    private String lastTestMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
