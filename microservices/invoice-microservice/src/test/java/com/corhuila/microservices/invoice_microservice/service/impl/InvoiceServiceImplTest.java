package com.corhuila.microservices.invoice_microservice.service.impl;

import com.corhuila.microservices.invoice_microservice.dto.InvoiceGenerateItemRequest;
import com.corhuila.microservices.invoice_microservice.dto.InvoiceGenerateRequest;
import com.corhuila.microservices.invoice_microservice.model.Invoice;
import com.corhuila.microservices.invoice_microservice.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository repository;

    @InjectMocks
    private InvoiceServiceImpl service;

    @Test
    void generateShouldReturnExistingInvoiceWhenPurchaseAlreadyHasOne() {
        Invoice existing = new Invoice();
        existing.setId(3L);
        existing.setPurchaseId(99L);
        existing.setInvoiceNumber("INV-20260422-99");

        when(repository.findByPurchaseId(99L)).thenReturn(Optional.of(existing));

        var response = service.generate(baseRequest(99L));

        assertEquals(3L, response.invoiceId());
        assertEquals("INV-20260422-99", response.invoiceNumber());
        verify(repository, never()).save(any(Invoice.class));
    }

    @Test
    void generateShouldCreateInvoiceWhenPurchaseHasNoInvoice() {
        when(repository.findByPurchaseId(100L)).thenReturn(Optional.empty());
        when(repository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice entity = invocation.getArgument(0);
            entity.setId(44L);
            entity.setIssuedAt(Instant.parse("2026-04-22T11:00:00Z"));
            return entity;
        });

        var response = service.generate(baseRequest(100L));

        assertEquals(44L, response.invoiceId());
        assertTrue(response.invoiceNumber().startsWith("INV-"));
        assertTrue(response.invoiceNumber().endsWith("-100"));
    }

    @Test
    void getByPurchaseIdShouldFailWhenInvoiceDoesNotExist() {
        when(repository.findByPurchaseId(500L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> service.getByPurchaseId(500L));

        assertEquals("Invoice not found for purchase", exception.getMessage());
    }

    @Test
    void getRecentShouldClampLimitAndReturnLatestInvoices() {
        Invoice invoice = new Invoice();
        invoice.setId(9L);
        invoice.setPurchaseId(12L);
        invoice.setInvoiceNumber("INV-20260506-12");
        invoice.setCustomerId("cust-1");
        invoice.setCustomerDocumentType("CC");
        invoice.setCustomerDocumentNumber("123456");
        invoice.setCustomerFullName("Cliente Uno");
        invoice.setSubtotal(new BigDecimal("10000.00"));
        invoice.setTax(new BigDecimal("1900.00"));
        invoice.setTotal(new BigDecimal("11900.00"));
        invoice.setIssuedAt(Instant.parse("2026-05-06T11:00:00Z"));

        when(repository.findAllByOrderByIssuedAtDesc(PageRequest.of(0, 50))).thenReturn(List.of(invoice));

        var response = service.getRecent(500);

        assertEquals(1, response.size());
        assertEquals("INV-20260506-12", response.get(0).invoiceNumber());
    }

    private InvoiceGenerateRequest baseRequest(Long purchaseId) {
        return new InvoiceGenerateRequest(
                purchaseId,
                "cust-1",
                "CC",
                "123456",
                "Cliente Uno",
                new BigDecimal("10000.00"),
                new BigDecimal("1900.00"),
                new BigDecimal("11900.00"),
                List.of(new InvoiceGenerateItemRequest(
                        1,
                        "Producto Uno",
                        2,
                        new BigDecimal("5000.00"),
                        new BigDecimal("10000.00")
                ))
        );
    }
}

