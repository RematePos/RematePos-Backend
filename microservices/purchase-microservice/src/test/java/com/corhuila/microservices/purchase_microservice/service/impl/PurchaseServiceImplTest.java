package com.corhuila.microservices.purchase_microservice.service.impl;

import com.corhuila.microservices.purchase_microservice.client.CustomerClientResponse;
import com.corhuila.microservices.purchase_microservice.client.InvoiceGenerateResponse;
import com.corhuila.microservices.purchase_microservice.client.ProductClientResponse;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseCheckoutRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseItemRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchasePaymentRequest;
import com.corhuila.microservices.purchase_microservice.model.PaymentMethod;
import com.corhuila.microservices.purchase_microservice.model.PaymentProvider;
import com.corhuila.microservices.purchase_microservice.model.PaymentStatus;
import com.corhuila.microservices.purchase_microservice.model.Purchase;
import com.corhuila.microservices.purchase_microservice.model.PurchaseItem;
import com.corhuila.microservices.purchase_microservice.model.PurchaseStatus;
import com.corhuila.microservices.purchase_microservice.repository.CashMovementRepository;
import com.corhuila.microservices.purchase_microservice.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository repository;

    @Mock
    private CashMovementRepository cashMovementRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PurchaseServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "customerServiceUrl", "http://customer-service");
        ReflectionTestUtils.setField(service, "productServiceUrl", "http://product-service");
        ReflectionTestUtils.setField(service, "invoiceServiceUrl", "http://invoice-service");
        ReflectionTestUtils.setField(service, "paymentWebhookSecret", "rematepos-sandbox");
    }

    @Test
    void checkoutShouldCreatePendingSaleWithoutDiscountingInventoryOrIssuingInvoice() {
        PurchaseCheckoutRequest request = new PurchaseCheckoutRequest(
                "CC",
                "123456",
                List.of(new PurchaseItemRequest(10, 2)),
                "venta mostrador"
        );

        when(restTemplate.getForObject("http://customer-service/api/v1/customers/document?type={type}&number={number}", CustomerClientResponse.class, "CC", "123456"))
                .thenReturn(new CustomerClientResponse("cust-1", "CC", "123456", "Ana", "Lopez", "ana@test.com", "300", "dir", "city"));
        when(restTemplate.getForObject("http://product-service/api/v1/products/{id}", ProductClientResponse.class, 10))
                .thenReturn(new ProductClientResponse(10, "Arroz", "desc", 1000.0, 20, null, 1, "Abarrotes", null));

        when(repository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setCreatedAt(Instant.parse("2026-04-22T10:00:00Z"));
            return entity;
        });

        var response = service.checkout(request);

        assertEquals("cust-1", response.customerId());
        assertEquals("PENDING_PAYMENT", response.status());
        assertEquals("PENDING", response.paymentStatus());
        assertEquals(new BigDecimal("2380.00"), response.total());
        assertEquals(new BigDecimal("0.00"), response.paidAmount());
        assertNull(response.invoiceId());
        assertEquals(1, response.items().size());

        verify(repository, times(1)).save(any(Purchase.class));
    }

    @Test
    void registerCashPaymentShouldApprovePaymentDiscountStockAndIssueInvoice() {
        Purchase purchase = buildPendingPurchase(9L, new BigDecimal("15000.00"));

        when(repository.findById(9L)).thenReturn(Optional.of(purchase));
        when(restTemplate.postForEntity(eq("http://product-service/api/v1/products/purchase"), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());
        when(restTemplate.postForObject(eq("http://invoice-service/api/v1/invoices/generate"), any(), eq(InvoiceGenerateResponse.class)))
                .thenReturn(new InvoiceGenerateResponse(77L, "INV-20260422-9"));
        when(repository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.registerPayment(9L, new PurchasePaymentRequest(
                PaymentMethod.CASH,
                new BigDecimal("15000.00"),
                new BigDecimal("20000.00"),
                null,
                null,
                null,
                null,
                null,
                "Pago recibido en caja"
        ));

        assertEquals("INVOICED", response.status());
        assertEquals("APPROVED", response.paymentStatus());
        assertEquals("CASH", response.paymentMethod());
        assertEquals("INTERNAL", response.paymentProvider());
        assertEquals("CONFIRMED_BY_CASHIER", response.providerStatus());
        assertEquals(new BigDecimal("15000.00"), response.paidAmount());
        assertEquals(new BigDecimal("20000.00"), response.cashReceived());
        assertEquals(new BigDecimal("5000.00"), response.changeAmount());
        assertNotNull(response.paidAt());
        assertEquals(77L, response.invoiceId());

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(repository).save(captor.capture());
        assertEquals(PaymentStatus.APPROVED, captor.getValue().getPaymentStatus());
        assertEquals(PaymentMethod.CASH, captor.getValue().getPaymentMethod());
    }

    @Test
    void registerCashPaymentShouldFailWhenReceivedCashIsLowerThanTotal() {
        Purchase purchase = buildPendingPurchase(11L, new BigDecimal("10000.00"));

        when(repository.findById(11L)).thenReturn(Optional.of(purchase));

        PurchasePaymentRequest request = new PurchasePaymentRequest(
                PaymentMethod.CASH,
                new BigDecimal("9999.00"),
                new BigDecimal("9999.00"),
                null,
                null,
                null,
                null,
                null,
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.registerPayment(11L, request));

        assertEquals("Cash received cannot be lower than sale total", exception.getMessage());
    }

    @Test
    void registerPaymentShouldRejectElectronicPaymentWithoutGatewayWebhook() {
        Purchase purchase = buildPendingPurchase(12L, new BigDecimal("10000.00"));

        when(repository.findById(12L)).thenReturn(Optional.of(purchase));

        PurchasePaymentRequest request = new PurchasePaymentRequest(
                PaymentMethod.NEQUI,
                new BigDecimal("10000.00"),
                null,
                null,
                PaymentProvider.NEQUI_DIRECT,
                null,
                null,
                null,
                null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.registerPayment(12L, request));

        assertEquals("Electronic payments must be created as gateway transactions and approved by webhook", exception.getMessage());
    }

    private Purchase buildPendingPurchase(Long id, BigDecimal total) {
        Purchase purchase = new Purchase();
        purchase.setId(id);
        purchase.setStatus(PurchaseStatus.PENDING_PAYMENT);
        purchase.setPaymentStatus(PaymentStatus.PENDING);
        purchase.setTotal(total);
        purchase.setPaidAmount(new BigDecimal("0.00"));
        purchase.setCustomerId("cust-" + id);
        purchase.setCustomerDocumentType("CC");
        purchase.setCustomerDocumentNumber("444");
        purchase.setCustomerFullName("Cliente Demo");
        purchase.setSubtotal(total.divide(new BigDecimal("1.19"), 2, java.math.RoundingMode.HALF_UP));
        purchase.setTax(total.subtract(purchase.getSubtotal()));
        purchase.setCreatedAt(Instant.parse("2026-04-22T09:00:00Z"));

        PurchaseItem item = new PurchaseItem();
        item.setProductId(10);
        item.setProductName("Arroz");
        item.setQuantity(1);
        item.setUnitPrice(purchase.getSubtotal());
        item.setLineTotal(purchase.getSubtotal());
        item.setPurchase(purchase);
        purchase.getItems().add(item);

        return purchase;
    }
}
