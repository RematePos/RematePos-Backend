package com.corhuila.microservices.invoice_microservice.repository;

import com.corhuila.microservices.invoice_microservice.model.Invoice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByPurchaseId(Long purchaseId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByCustomerIdOrderByIssuedAtDesc(String customerId);

    List<Invoice> findByCustomerDocumentTypeAndCustomerDocumentNumberOrderByIssuedAtDesc(String customerDocumentType, String customerDocumentNumber);

    List<Invoice> findAllByOrderByIssuedAtDesc(Pageable pageable);
}

