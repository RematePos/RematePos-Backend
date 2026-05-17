package com.corhuila.microservices.invoice_microservice.repository;

import com.corhuila.microservices.invoice_microservice.model.Invoice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByIdAndTenantId(Long id, String tenantId);

    Optional<Invoice> findByPurchaseId(Long purchaseId);

    Optional<Invoice> findByPurchaseIdAndTenantId(Long purchaseId, String tenantId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByInvoiceNumberAndTenantId(String invoiceNumber, String tenantId);

    List<Invoice> findByCustomerIdOrderByIssuedAtDesc(String customerId);

    List<Invoice> findByCustomerIdAndTenantIdOrderByIssuedAtDesc(String customerId, String tenantId);

    List<Invoice> findByCustomerDocumentTypeAndCustomerDocumentNumberOrderByIssuedAtDesc(String customerDocumentType, String customerDocumentNumber);

    List<Invoice> findByCustomerDocumentTypeAndCustomerDocumentNumberAndTenantIdOrderByIssuedAtDesc(
            String customerDocumentType,
            String customerDocumentNumber,
            String tenantId
    );

    List<Invoice> findAllByOrderByIssuedAtDesc(Pageable pageable);

    List<Invoice> findAllByTenantIdOrderByIssuedAtDesc(String tenantId, Pageable pageable);
}

