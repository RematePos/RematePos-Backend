package com.corhuila.microservices.invoice_microservice.service.impl;

import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProviderRequest;
import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProviderResolver;
import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProviderResponse;
import com.corhuila.microservices.invoice_microservice.billing.provider.BillingProviderStatus;
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
    private final BillingProviderResolver billingProviderResolver;

    @Override
    @Transactional
    public InvoiceGenerateResponse generate(InvoiceGenerateRequest request) {
        String tenantId = securityContextHelper.getTenantIdForInternalService("purchase-microservice");
        return repository.findByPurchaseIdAndTenantId(request.purchaseId(), tenantId)
                .map(existing -> {
                    if (existing.getProviderStatus() == null || existing.getProviderStatus().isBlank()) {
                        applyBillingProvider(existing);
                        existing = repository.save(existing);
                    }
                    return toGenerateResponse(existing);
                })
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

                    applyBillingProvider(invoice);
                    invoice = repository.save(invoice);
                    return toGenerateResponse(invoice);
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

    private void applyBillingProvider(Invoice invoice) {
        try {
            BillingProviderResponse providerResponse = billingProviderResolver.resolve()
                    .issueInvoice(toBillingProviderRequest(invoice));
            applyBillingProviderResponse(invoice, providerResponse);
        } catch (Exception ex) {
            invoice.setProviderStatus(BillingProviderStatus.PROVIDER_FAILED.name());
            invoice.setFiscalValid(false);
            invoice.setValidationMessage(ex.getMessage());
        }
    }

    private BillingProviderRequest toBillingProviderRequest(Invoice invoice) {
        List<BillingProviderRequest.LineItem> items = invoice.getItems().stream()
                .map(item -> new BillingProviderRequest.LineItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal()
                ))
                .toList();

        return new BillingProviderRequest(
                invoice.getInvoiceNumber(),
                invoice.getPurchaseId(),
                invoice.getCustomerId(),
                invoice.getCustomerDocumentType(),
                invoice.getCustomerDocumentNumber(),
                invoice.getCustomerFullName(),
                invoice.getSubtotal(),
                invoice.getTax(),
                invoice.getTotal(),
                invoice.getIssuedAt() == null ? Instant.now() : invoice.getIssuedAt(),
                items
        );
    }

    private void applyBillingProviderResponse(Invoice invoice, BillingProviderResponse response) {
        invoice.setProvider(response.provider());
        invoice.setProviderEnvironment(response.environment());
        invoice.setProviderStatus(response.status() == null ? null : response.status().name());
        invoice.setProviderReference(response.providerReference());
        invoice.setCufe(response.cufe());
        invoice.setCude(response.cude());
        invoice.setQrCode(response.qrCode());
        invoice.setXmlContent(response.xmlContent());
        invoice.setPdfUrl(response.pdfUrl());
        invoice.setFiscalValid(response.fiscalValid());
        invoice.setValidationMessage(response.validationMessage());
    }

    private InvoiceGenerateResponse toGenerateResponse(Invoice invoice) {
        return new InvoiceGenerateResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getProvider(),
                invoice.getProviderEnvironment(),
                invoice.getProviderStatus(),
                invoice.getCufe(),
                invoice.getCude(),
                invoice.getQrCode(),
                invoice.getPdfUrl(),
                invoice.getFiscalValid(),
                invoice.getValidationMessage()
        );
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
                invoice.getProvider(),
                invoice.getProviderEnvironment(),
                invoice.getProviderStatus(),
                invoice.getCufe(),
                invoice.getCude(),
                invoice.getQrCode(),
                invoice.getXmlContent(),
                invoice.getPdfUrl(),
                invoice.getFiscalValid(),
                invoice.getValidationMessage(),
                items
        );
    }
}

