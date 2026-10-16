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
import com.corhuila.microservices.purchase_microservice.security.SecurityContextHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    @Mock
    private SecurityContextHelper securityContextHelper;

    @InjectMocks
    private PurchaseServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "customerServiceUrl", "http://customer-service");
        ReflectionTestUtils.setField(service, "productServiceUrl", "http://product-service");
        ReflectionTestUtils.setField(service, "invoiceServiceUrl", "http://invoice-service");
        ReflectionTestUtils.setField(service, "paymentWebhookSecret", "rematepos-sandbox");
        when(securityContextHelper.getTenantId()).thenReturn("tenant-1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "user-1");
        request.addHeader("X-Username", "demo-test");
        request.addHeader("X-Tenant-Id", "tenant-1");
        request.addHeader("X-Tenant-Slug", "demo");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void checkoutShouldCreatePendingSaleWithoutDiscountingInventoryOrIssuingInvoice() {
        PurchaseCheckoutRequest request = new PurchaseCheckoutRequest(
                "CC",
                "123456",
                List.of(new PurchaseItemRequest(10, 2)),
                "venta mostrador"
        );

        when(restTemplate.exchange(eq("http://customer-service/api/v1/customers/document?type={type}&number={number}"),
                eq(HttpMethod.GET), any(HttpEntity.class), eq(CustomerClientResponse.class), eq("CC"), eq("123456")))
                .thenReturn(ResponseEntity.ok(new CustomerClientResponse("cust-1", "CC", "123456", "Ana", "Lopez", "ana@test.com", "300", "dir", "city")));
        when(restTemplate.exchange(eq("http://product-service/api/v1/products/{id}"),
                eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductClientResponse.class), eq(10)))
                .thenReturn(ResponseEntity.ok(new ProductClientResponse(10, "Arroz", "desc", 1000.0, 20, null, 1, "Abarrotes", null)));

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
        ArgumentCaptor<HttpEntity<?>> customerRequest = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("http://customer-service/api/v1/customers/document?type={type}&number={number}"),
                eq(HttpMethod.GET), customerRequest.capture(), eq(CustomerClientResponse.class), eq("CC"), eq("123456"));
        assertUserAndTenantHeaders(customerRequest.getValue().getHeaders());
        ArgumentCaptor<HttpEntity<?>> productRequest = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("http://product-service/api/v1/products/{id}"),
                eq(HttpMethod.GET), productRequest.capture(), eq(ProductClientResponse.class), eq(10));
        assertUserAndTenantHeaders(productRequest.getValue().getHeaders());
    }

    @Test
    void registerCashPaymentShouldApprovePaymentDiscountStockAndIssueInvoice() {
        Purchase purchase = buildPendingPurchase(9L, new BigDecimal("15000.00"));

        when(repository.findByIdAndTenantId(9L, "tenant-1")).thenReturn(Optional.of(purchase));
        when(restTemplate.postForEntity(eq("http://product-service/api/v1/products/purchase"), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());
        when(restTemplate.postForEntity(eq("http://invoice-service/api/v1/invoices/generate"), any(), eq(InvoiceGenerateResponse.class)))
                .thenReturn(ResponseEntity.ok(new InvoiceGenerateResponse(77L, "INV-20260422-9")));
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

        when(repository.findByIdAndTenantId(11L, "tenant-1")).thenReturn(Optional.of(purchase));

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

        when(repository.findByIdAndTenantId(12L, "tenant-1")).thenReturn(Optional.of(purchase));

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
        purchase.setTenantId("tenant-1");
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

    private void assertUserAndTenantHeaders(HttpHeaders headers) {
        assertEquals("user-1", headers.getFirst("X-User-Id"));
        assertEquals("demo-test", headers.getFirst("X-Username"));
        assertEquals("tenant-1", headers.getFirst("X-Tenant-Id"));
        assertEquals("demo", headers.getFirst("X-Tenant-Slug"));
        assertNull(headers.getFirst(HttpHeaders.AUTHORIZATION));
        assertNull(headers.getFirst("X-Internal-Service-Token"));
    }
}
