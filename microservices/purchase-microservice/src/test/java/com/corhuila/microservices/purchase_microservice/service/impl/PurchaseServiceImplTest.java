package com.corhuila.microservices.purchase_microservice.service.impl;

import com.corhuila.microservices.purchase_microservice.dto.PurchaseCheckoutRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseItemRequest;
import com.corhuila.microservices.purchase_microservice.model.PaymentStatus;
import com.corhuila.microservices.purchase_microservice.model.Purchase;
import com.corhuila.microservices.purchase_microservice.model.PurchaseStatus;
import com.corhuila.microservices.purchase_microservice.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository repository;

    @InjectMocks
    private PurchaseServiceImpl service;

    @Test
    void checkoutShouldCreatePurchaseInPendingPaymentStatus() {
        PurchaseCheckoutRequest request = new PurchaseCheckoutRequest(
                "cc",
                " 123456 ",
                " Ana Lopez ",
                List.of(new PurchaseItemRequest(10, "Arroz", 2, new BigDecimal("1000.00"))),
                " venta mostrador "
        );

        when(repository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setCreatedAt(Instant.parse("2026-05-12T10:00:00Z"));
            return entity;
        });

        var response = service.checkout(request);

        assertEquals(1L, response.purchaseId());
        assertEquals("CC-123456", response.customerId());
        assertEquals("CC", response.customerDocumentType());
        assertEquals("123456", response.customerDocumentNumber());
        assertEquals("Ana Lopez", response.customerFullName());
        assertEquals("PENDING_PAYMENT", response.status());
        assertEquals("PENDING", response.paymentStatus());
        assertEquals(new BigDecimal("2000.00"), response.subtotal());
        assertEquals(new BigDecimal("380.00"), response.tax());
        assertEquals(new BigDecimal("2380.00"), response.total());
        assertEquals(new BigDecimal("0.00"), response.paidAmount());
        assertEquals("venta mostrador", response.notes());
        assertEquals(1, response.items().size());

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(repository).save(captor.capture());
        assertEquals(PurchaseStatus.PENDING_PAYMENT, captor.getValue().getStatus());
        assertEquals(PaymentStatus.PENDING, captor.getValue().getPaymentStatus());
    }

}
