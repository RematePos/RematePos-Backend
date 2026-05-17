package com.corhuila.microservices.purchase_microservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cash_movements")
@Getter
@Setter
public class CashMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantId;

    @Column(nullable = false)
    private Long cashSessionId;

    @Column(nullable = false)
    private Long saleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashMovementType type;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(length = 240)
    private String description;

    private String createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
