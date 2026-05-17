package com.corhuila.microservices.purchase_microservice.repository;

import com.corhuila.microservices.purchase_microservice.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Optional<Purchase> findByIdAndTenantId(Long id, String tenantId);

    List<Purchase> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<Purchase> findByCustomerIdAndTenantIdOrderByCreatedAtDesc(String customerId, String tenantId);

    List<Purchase> findByCustomerDocumentTypeAndCustomerDocumentNumberOrderByCreatedAtDesc(
            String customerDocumentType,
            String customerDocumentNumber
    );

    List<Purchase> findByCustomerDocumentTypeAndCustomerDocumentNumberAndTenantIdOrderByCreatedAtDesc(
            String customerDocumentType,
            String customerDocumentNumber,
            String tenantId
    );

    Optional<Purchase> findByInvoiceNumber(String invoiceNumber);

    Optional<Purchase> findByInvoiceNumberAndTenantId(String invoiceNumber, String tenantId);

    Optional<Purchase> findByPaymentReference(String paymentReference);
}

