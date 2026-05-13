package com.corhuila.microservices.purchase_microservice.repository;

import com.corhuila.microservices.purchase_microservice.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<Purchase> findByCustomerDocumentTypeAndCustomerDocumentNumberOrderByCreatedAtDesc(
            String customerDocumentType,
            String customerDocumentNumber
    );

    Optional<Purchase> findByInvoiceNumber(String invoiceNumber);

    Optional<Purchase> findByPaymentReference(String paymentReference);
}

