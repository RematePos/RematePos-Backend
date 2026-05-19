package com.corhuila.microservices.invoice_microservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices", uniqueConstraints = @UniqueConstraint(columnNames = "purchaseId"))
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tenantId;

    @Column(nullable = false, unique = true)
    private Long purchaseId;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String customerDocumentType;

    @Column(nullable = false)
    private String customerDocumentNumber;

    @Column(nullable = false)
    private String customerFullName;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, updatable = false)
    private Instant issuedAt;

    private String provider;

    private String providerEnvironment;

    private String providerStatus;

    private String providerReference;

    private String cufe;

    private String cude;

    @Column(columnDefinition = "TEXT")
    private String qrCode;

    @Column(columnDefinition = "TEXT")
    private String xmlContent;

    private String pdfUrl;

    private Boolean fiscalValid;

    @Column(columnDefinition = "TEXT")
    private String validationMessage;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InvoiceItem> items = new ArrayList<>();

    @PrePersist
    void prePersist() {
        issuedAt = Instant.now();
    }
}

