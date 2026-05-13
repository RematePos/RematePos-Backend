package com.corhuila.microservices.purchase_microservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@Getter
@Setter
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String customerDocumentType;

    @Column(nullable = false)
    private String customerDocumentNumber;

    @Column(nullable = false)
    private String customerFullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal paidAmount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(length = 800)
    private String notes;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PurchaseItem> items = new ArrayList<>();

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
