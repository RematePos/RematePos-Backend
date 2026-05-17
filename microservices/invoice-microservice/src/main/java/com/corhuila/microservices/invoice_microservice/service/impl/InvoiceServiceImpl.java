package com.corhuila.microservices.invoice_microservice.service.impl;

import com.corhuila.microservices.invoice_microservice.dto.*;
import com.corhuila.microservices.invoice_microservice.model.Invoice;
import com.corhuila.microservices.invoice_microservice.model.InvoiceItem;
import com.corhuila.microservices.invoice_microservice.repository.InvoiceRepository;
import com.corhuila.microservices.invoice_microservice.security.SecurityContextHelper;
import com.corhuila.microservices.invoice_microservice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository repository;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional
    public InvoiceGenerateResponse generate(InvoiceGenerateRequest request) {
        String tenantId = securityContextHelper.getTenantIdForInternalService("purchase-microservice");
        return repository.findByPurchaseIdAndTenantId(request.purchaseId(), tenantId)
                .map(existing -> new InvoiceGenerateResponse(existing.getId(), existing.getInvoiceNumber()))
                .orElseGet(() -> {
                    Invoice invoice = new Invoice();
                    invoice.setTenantId(tenantId);
                    invoice.setPurchaseId(request.purchaseId());
                    invoice.setInvoiceNumber(buildInvoiceNumber(request.purchaseId()));
                    invoice.setCustomerId(request.customerId());
                    invoice.setCustomerDocumentType(request.customerDocumentType());
                    invoice.setCustomerDocumentNumber(request.customerDocumentNumber());
                    invoice.setCustomerFullName(request.customerFullName());
                    invoice.setSubtotal(request.subtotal());
                    invoice.setTax(request.tax());
                    invoice.setTotal(request.total());

                    for (InvoiceGenerateItemRequest item : request.items()) {
                        InvoiceItem entity = new InvoiceItem();
                        entity.setInvoice(invoice);
                        entity.setProductId(item.productId());
                        entity.setProductName(item.productName());
                        entity.setQuantity(item.quantity());
                        entity.setUnitPrice(item.unitPrice());
                        entity.setLineTotal(item.lineTotal());
                        invoice.getItems().add(entity);
                    }

                    invoice = repository.save(invoice);
                    return new InvoiceGenerateResponse(invoice.getId(), invoice.getInvoiceNumber());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long id) {
        String tenantId = securityContextHelper.getTenantId();
        return repository.findByIdAndTenantId(id, tenantId)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getByPurchaseId(Long purchaseId) {
        String tenantId = securityContextHelper.getTenantId();
        return repository.findByPurchaseIdAndTenantId(purchaseId, tenantId)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found for purchase"));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getByInvoiceNumber(String invoiceNumber) {
        String tenantId = securityContextHelper.getTenantId();
        return repository.findByInvoiceNumberAndTenantId(normalizeInvoiceNumber(invoiceNumber), tenantId)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getByCustomerId(String customerId) {
        String tenantId = securityContextHelper.getTenantId();
        return repository.findByCustomerIdAndTenantIdOrderByIssuedAtDesc(customerId, tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getByDocument(String documentType, String documentNumber) {
        String tenantId = securityContextHelper.getTenantId();
        return repository.findByCustomerDocumentTypeAndCustomerDocumentNumberAndTenantIdOrderByIssuedAtDesc(documentType, documentNumber, tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getRecent(int limit) {
        String tenantId = securityContextHelper.getTenantId();
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return repository.findAllByTenantIdOrderByIssuedAtDesc(tenantId, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String buildInvoiceNumber(Long purchaseId) {
        String date = DateTimeFormatter.ofPattern("yyyyMMdd")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        return "INV-" + date + "-" + purchaseId;
    }

    private String normalizeInvoiceNumber(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("NV-")) {
            return "I" + normalized;
        }
        return normalized;
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        List<InvoiceItemResponse> items = invoice.getItems().stream()
                .map(i -> new InvoiceItemResponse(
                        i.getProductId(),
                        i.getProductName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getLineTotal()
                ))
                .toList();

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getPurchaseId(),
                invoice.getCustomerId(),
                invoice.getCustomerDocumentType(),
                invoice.getCustomerDocumentNumber(),
                invoice.getCustomerFullName(),
                invoice.getSubtotal(),
                invoice.getTax(),
                invoice.getTotal(),
                invoice.getIssuedAt(),
                items
        );
    }
}

