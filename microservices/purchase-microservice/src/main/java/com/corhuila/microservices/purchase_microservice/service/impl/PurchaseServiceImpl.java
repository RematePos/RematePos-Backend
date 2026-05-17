package com.corhuila.microservices.purchase_microservice.service.impl;

import com.corhuila.microservices.purchase_microservice.client.CustomerClientResponse;
import com.corhuila.microservices.purchase_microservice.client.InvoiceGenerateItemRequest;
import com.corhuila.microservices.purchase_microservice.client.InvoiceGenerateRequest;
import com.corhuila.microservices.purchase_microservice.client.InvoiceGenerateResponse;
import com.corhuila.microservices.purchase_microservice.client.ProductClientResponse;
import com.corhuila.microservices.purchase_microservice.client.ProductQuantityRequest;
import com.corhuila.microservices.purchase_microservice.dto.ElectronicPaymentRequest;
import com.corhuila.microservices.purchase_microservice.dto.PaymentWebhookRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseCheckoutRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseItemResponse;
import com.corhuila.microservices.purchase_microservice.dto.PurchasePaymentRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseResponse;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseReturnRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseReturnResponse;
import com.corhuila.microservices.purchase_microservice.model.PaymentMethod;
import com.corhuila.microservices.purchase_microservice.model.PaymentProvider;
import com.corhuila.microservices.purchase_microservice.model.PaymentStatus;
import com.corhuila.microservices.purchase_microservice.model.CashMovement;
import com.corhuila.microservices.purchase_microservice.model.CashMovementType;
import com.corhuila.microservices.purchase_microservice.model.Purchase;
import com.corhuila.microservices.purchase_microservice.model.PurchaseItem;
import com.corhuila.microservices.purchase_microservice.model.PurchaseStatus;
import com.corhuila.microservices.purchase_microservice.repository.CashMovementRepository;
import com.corhuila.microservices.purchase_microservice.repository.PurchaseRepository;
import com.corhuila.microservices.purchase_microservice.security.SecurityContextHelper;
import com.corhuila.microservices.purchase_microservice.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.19");
    private static final String COP = "COP";
    private static final String SANDBOX_SIGNATURE = "rematepos-sandbox";
    private static final List<String> INTERNAL_SECURITY_HEADERS = List.of(
            "X-User-Id",
            "X-Username",
            "X-Roles",
            "X-Permissions",
            "X-Tenant-Id",
            "X-Tenant-Slug"
    );
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";
    private static final String INTERNAL_SERVICE_TOKEN_HEADER = "X-Internal-Service-Token";
    private static final String PURCHASE_MICROSERVICE = "purchase-microservice";

    private final PurchaseRepository repository;
    private final CashMovementRepository cashMovementRepository;
    private final RestTemplate restTemplate;
    private final SecurityContextHelper securityContextHelper;

    @Value("${services.customer.url:http://customer-microservice:8091}")
    private String customerServiceUrl;

    @Value("${services.product.url:http://product-microservice:8092}")
    private String productServiceUrl;

    @Value("${services.invoice.url:http://invoice-microservice:8095}")
    private String invoiceServiceUrl;

    @Value("${payments.webhook.secret:rematepos-sandbox}")
    private String paymentWebhookSecret;

    @Value("${internal.service.token:}")
    private String internalServiceToken;

    @Override
    @Transactional
    public PurchaseResponse checkout(PurchaseCheckoutRequest request) {
        String tenantId = securityContextHelper.getTenantId();
        CustomerClientResponse customer = getCustomerByDocument(request.documentType(), request.documentNumber());

        List<PurchaseItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var item : request.items()) {
            ProductClientResponse product = getProduct(item.productId());
            BigDecimal unitPrice = BigDecimal.valueOf(product.price()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity())).setScale(2, RoundingMode.HALF_UP);

            PurchaseItem entity = new PurchaseItem();
            entity.setProductId(product.id());
            entity.setProductName(product.name());
            entity.setCategoryName(product.categoryName());
            entity.setQuantity(item.quantity());
            entity.setUnitPrice(unitPrice);
            entity.setLineTotal(lineTotal);
            items.add(entity);

            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        Purchase purchase = new Purchase();
        purchase.setTenantId(tenantId);
        purchase.setCustomerId(customer.id());
        purchase.setCustomerDocumentType(customer.documentType());
        purchase.setCustomerDocumentNumber(customer.documentNumber());
        purchase.setCustomerFullName((customer.firstName() + " " + customer.lastName()).trim());
        purchase.setStatus(PurchaseStatus.PENDING_PAYMENT);
        purchase.setPaymentStatus(PaymentStatus.PENDING);
        purchase.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        purchase.setTax(tax);
        purchase.setTotal(total);
        purchase.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        purchase.setNotes(request.notes());

        for (PurchaseItem item : items) {
            item.setPurchase(purchase);
            purchase.getItems().add(item);
        }

        return toResponse(repository.save(purchase));
    }

    @Override
    @Transactional
    public PurchaseResponse registerPayment(Long purchaseId, PurchasePaymentRequest request) {
        Purchase purchase = getPurchaseForPayment(purchaseId);

        if (request.paymentMethod() == PaymentMethod.CASH) {
            approveCashPayment(purchase, request);
            return finalizeApprovedPayment(purchase);
        }

        if (request.paymentMethod() == PaymentMethod.CARD_MANUAL) {
            approveManualCardPayment(purchase, request);
            return finalizeApprovedPayment(purchase);
        }

        throw new IllegalArgumentException(
                "Electronic payments must be created as gateway transactions and approved by webhook"
        );
    }

    @Override
    @Transactional
    public PurchaseResponse createElectronicPayment(Long purchaseId, ElectronicPaymentRequest request) {
        Purchase purchase = getPurchaseForPayment(purchaseId);
        validateElectronicMethod(request.paymentMethod());

        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(purchase.getTotal()) != 0) {
            throw new IllegalArgumentException("Payment amount must match sale total");
        }

        purchase.setPaymentMethod(request.paymentMethod());
        purchase.setPaymentProvider(request.paymentProvider() != null ? request.paymentProvider() : PaymentProvider.WOMPI);
        purchase.setPaymentStatus(PaymentStatus.PENDING);
        purchase.setPaymentReference(buildPaymentReference(purchase.getId()));
        purchase.setProviderTransactionId("SANDBOX-" + UUID.randomUUID());
        purchase.setProviderStatus("PENDING_GATEWAY_CONFIRMATION");
        purchase.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        return toResponse(repository.save(purchase));
    }

    @Override
    @Transactional
    public PurchaseResponse processPaymentWebhook(PaymentWebhookRequest request) {
        String reference = normalize(request.reference());
        Purchase purchase = repository.findByPaymentReference(reference)
                .orElseThrow(() -> new NoSuchElementException("Payment reference not found"));

        if (!isValidWebhookSignature(request)) {
            throw new IllegalArgumentException("Invalid payment webhook signature");
        }

        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(purchase.getTotal()) != 0) {
            throw new IllegalArgumentException("Webhook amount does not match sale total");
        }

        String currency = normalize(request.currency());
        if (currency != null && !COP.equalsIgnoreCase(currency)) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }

        String status = normalize(request.status()).toUpperCase(Locale.ROOT);
        purchase.setProviderStatus(status);
        purchase.setProviderTransactionId(firstNonBlank(request.providerTransactionId(), purchase.getProviderTransactionId()));

        if ("APPROVED".equals(status) || "PAID".equals(status)) {
            purchase.setPaymentStatus(PaymentStatus.APPROVED);
            purchase.setPaidAmount(purchase.getTotal());
            purchase.setPaidAt(Instant.now());
            return finalizeApprovedPayment(purchase);
        }

        if ("DECLINED".equals(status)) {
            purchase.setPaymentStatus(PaymentStatus.DECLINED);
        } else if ("EXPIRED".equals(status)) {
            purchase.setPaymentStatus(PaymentStatus.EXPIRED);
        } else {
            purchase.setPaymentStatus(PaymentStatus.FAILED);
        }

        purchase.setStatus(PurchaseStatus.FAILED);
        return toResponse(repository.save(purchase));
    }

    @Override
    @Transactional
    public PurchaseResponse cancel(Long id) {
        String tenantId = securityContextHelper.getTenantId();
        Purchase purchase = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Purchase not found"));

        if (isFinalized(purchase)) {
            throw new IllegalArgumentException("Paid or invoiced sales cannot be cancelled from this endpoint");
        }

        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchase.setPaymentStatus(PaymentStatus.EXPIRED);
        purchase.setProviderStatus("CANCELLED_BY_USER");
        return toResponse(repository.save(purchase));
    }

    @Override
    @Transactional
    public PurchaseReturnResponse registerReturn(PurchaseReturnRequest request) {
        String invoiceNumber = normalizeInvoiceNumber(request.invoiceNumber());
        String reason = normalize(request.reason());

        if (invoiceNumber == null) {
            throw new IllegalArgumentException("Invoice number is required");
        }

        String tenantId = securityContextHelper.getTenantId();
        Purchase purchase = repository.findByInvoiceNumberAndTenantId(invoiceNumber, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found: " + invoiceNumber));

        PurchaseItem item = purchase.getItems().stream()
                .filter(i -> i.getProductId().equals(request.productId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Product is not part of invoice " + invoiceNumber));

        int alreadyReturned = item.getReturnedQuantity() == null ? 0 : item.getReturnedQuantity();
        int availableToReturn = item.getQuantity() - alreadyReturned;

        if (request.quantity() > availableToReturn) {
            throw new IllegalArgumentException(
                    "Return quantity cannot be greater than available purchased quantity: " + availableToReturn
            );
        }

        restTemplate.postForEntity(
                productServiceUrl + "/api/v1/products/restock",
                internalSecurityEntity(List.of(new ProductQuantityRequest(item.getProductId(), request.quantity())), purchase),
                Void.class
        );

        int totalReturned = alreadyReturned + request.quantity();
        Instant returnedAt = Instant.now();
        item.setReturnedQuantity(totalReturned);
        purchase.setNotes(appendNote(
                purchase.getNotes(),
                "Return " + returnedAt
                        + " | product #" + item.getProductId()
                        + " " + item.getProductName()
                        + " | quantity " + request.quantity()
                        + " | reason: " + reason
        ));

        repository.save(purchase);

        return new PurchaseReturnResponse(
                purchase.getId(),
                purchase.getInvoiceNumber(),
                item.getProductId(),
                item.getProductName(),
                request.quantity(),
                totalReturned,
                item.getQuantity(),
                reason,
                returnedAt,
                "Return registered and product stock restored"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse getById(Long id) {
        String tenantId = securityContextHelper.getTenantId();
        return repository.findByIdAndTenantId(id, tenantId)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Purchase not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse getByInvoiceNumber(String invoiceNumber) {
        String tenantId = securityContextHelper.getTenantId();
        String normalized = normalizeInvoiceNumber(invoiceNumber);
        return repository.findByInvoiceNumberAndTenantId(normalized, tenantId)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found: " + normalized));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getByCustomerId(String customerId) {
        String tenantId = securityContextHelper.getTenantId();
        return repository.findByCustomerIdAndTenantIdOrderByCreatedAtDesc(customerId, tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getByDocument(String documentType, String documentNumber) {
        String tenantId = securityContextHelper.getTenantId();
        return repository.findByCustomerDocumentTypeAndCustomerDocumentNumberAndTenantIdOrderByCreatedAtDesc(documentType, documentNumber, tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Purchase getPurchaseForPayment(Long purchaseId) {
        String tenantId = securityContextHelper.getTenantId();
        Purchase purchase = repository.findByIdAndTenantId(purchaseId, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Purchase not found"));

        if (isFinalized(purchase)) {
            return purchase;
        }

        if (purchase.getStatus() != PurchaseStatus.PENDING_PAYMENT && purchase.getStatus() != PurchaseStatus.FAILED) {
            throw new IllegalArgumentException("Sale is not pending payment");
        }

        return purchase;
    }

    private void approveCashPayment(Purchase purchase, PurchasePaymentRequest request) {
        BigDecimal cashReceived = (request.cashReceived() != null ? request.cashReceived() : request.paidAmount())
                .setScale(2, RoundingMode.HALF_UP);

        if (cashReceived.compareTo(purchase.getTotal()) < 0) {
            throw new IllegalArgumentException("Cash received cannot be lower than sale total");
        }

        purchase.setPaidAmount(purchase.getTotal());
        purchase.setCashReceived(cashReceived);
        purchase.setChangeAmount(cashReceived.subtract(purchase.getTotal()).setScale(2, RoundingMode.HALF_UP));
        purchase.setCashRegisterSessionId(request.cashRegisterSessionId());
        purchase.setPaidAt(Instant.now());
        purchase.setPaymentStatus(PaymentStatus.APPROVED);
        purchase.setPaymentMethod(PaymentMethod.CASH);
        purchase.setPaymentProvider(PaymentProvider.INTERNAL);
        purchase.setProviderStatus("CONFIRMED_BY_CASHIER");
        purchase.setPaymentEvidenceNote(firstNonBlank(request.evidenceNote(), "Pago en efectivo recibido en caja."));

        CashMovement movement = new CashMovement();
        movement.setTenantId(purchase.getTenantId());
        movement.setCashSessionId(request.cashRegisterSessionId() != null ? request.cashRegisterSessionId() : 1L);
        movement.setSaleId(purchase.getId());
        movement.setType(CashMovementType.SALE_PAYMENT);
        movement.setAmount(purchase.getTotal());
        movement.setDescription("Pago en efectivo de venta POS");
        movement.setCreatedBy("cashier");
        cashMovementRepository.save(movement);
    }

    private void approveManualCardPayment(Purchase purchase, PurchasePaymentRequest request) {
        if (isBlank(request.paymentReference()) && isBlank(request.providerTransactionId())) {
            throw new IllegalArgumentException("Manual card payments require authorization reference");
        }

        purchase.setPaidAmount(purchase.getTotal());
        purchase.setPaidAt(Instant.now());
        purchase.setPaymentStatus(PaymentStatus.APPROVED);
        purchase.setPaymentMethod(PaymentMethod.CARD_MANUAL);
        purchase.setPaymentProvider(request.paymentProvider() != null ? request.paymentProvider() : PaymentProvider.OTHER);
        purchase.setPaymentReference(normalize(request.paymentReference()));
        purchase.setProviderTransactionId(normalize(request.providerTransactionId()));
        purchase.setProviderStatus(firstNonBlank(request.providerStatus(), "CONFIRMED_BY_CASHIER"));
        purchase.setPaymentEvidenceNote(firstNonBlank(request.evidenceNote(), "Pago con datafono registrado manualmente."));
    }

    private PurchaseResponse finalizeApprovedPayment(Purchase purchase) {
        if (!isFinalized(purchase)) {
            if (purchase.getInventoryDiscountedAt() == null) {
                List<ProductQuantityRequest> stockDiscount = purchase.getItems().stream()
                        .map(item -> new ProductQuantityRequest(item.getProductId(), item.getQuantity()))
                        .toList();
                restTemplate.postForEntity(
                        productServiceUrl + "/api/v1/products/purchase",
                        internalSecurityEntity(stockDiscount, purchase),
                        Void.class
                );
                purchase.setInventoryDiscountedAt(Instant.now());
            }

            purchase.setStatus(PurchaseStatus.PAID);
        }

        issueInvoiceIfPossible(purchase);
        return toResponse(repository.save(purchase));
    }

    private void issueInvoiceIfPossible(Purchase purchase) {
        if (purchase.getInvoiceNumber() != null) {
            purchase.setStatus(PurchaseStatus.INVOICED);
            return;
        }

        try {
            List<InvoiceGenerateItemRequest> invoiceItems = purchase.getItems().stream()
                    .map(item -> new InvoiceGenerateItemRequest(
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getLineTotal()
                    ))
                    .toList();

            InvoiceGenerateRequest invoiceRequest = new InvoiceGenerateRequest(
                    purchase.getId(),
                    purchase.getCustomerId(),
                    purchase.getCustomerDocumentType(),
                    purchase.getCustomerDocumentNumber(),
                    purchase.getCustomerFullName(),
                    purchase.getSubtotal(),
                    purchase.getTax(),
                    purchase.getTotal(),
                    invoiceItems
            );

            ResponseEntity<InvoiceGenerateResponse> invoiceResponse = restTemplate.postForEntity(
                    invoiceServiceUrl + "/api/v1/invoices/generate",
                    internalSecurityEntity(invoiceRequest, purchase),
                    InvoiceGenerateResponse.class
            );
            InvoiceGenerateResponse invoice = invoiceResponse.getBody();

            if (invoice != null) {
                purchase.setInvoiceId(invoice.invoiceId());
                purchase.setInvoiceNumber(invoice.invoiceNumber());
                purchase.setStatus(PurchaseStatus.INVOICED);
            }
        } catch (Exception ex) {
            purchase.setStatus(PurchaseStatus.PAID);
            purchase.setNotes(appendNote(
                    purchase.getNotes(),
                    "Invoice pending issue at " + Instant.now() + ": " + ex.getMessage()
            ));
        }
    }

    private CustomerClientResponse getCustomerByDocument(String documentType, String documentNumber) {
        ResponseEntity<CustomerClientResponse> responseEntity = restTemplate.exchange(
                customerServiceUrl + "/api/v1/customers/document?type={type}&number={number}",
                HttpMethod.GET,
                internalSecurityEntity(),
                CustomerClientResponse.class,
                documentType,
                documentNumber
        );

        CustomerClientResponse response = responseEntity.getBody();

        if (response == null) {
            throw new NoSuchElementException("Customer not found with document " + documentType + "-" + documentNumber);
        }

        return response;
    }

    private ProductClientResponse getProduct(Integer productId) {
        ResponseEntity<ProductClientResponse> responseEntity = restTemplate.exchange(
                productServiceUrl + "/api/v1/products/{id}",
                HttpMethod.GET,
                internalSecurityEntity(),
                ProductClientResponse.class,
                productId
        );
        ProductClientResponse response = responseEntity.getBody();

        if (response == null) {
            throw new NoSuchElementException("Product not found: " + productId);
        }

        return response;
    }

    private <T> HttpEntity<T> internalSecurityEntity(T body) {
        HttpHeaders headers = internalSecurityHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private <T> HttpEntity<T> internalSecurityEntity(T body, Purchase purchase) {
        HttpHeaders headers = internalSecurityHeaders(purchase);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> internalSecurityEntity() {
        return new HttpEntity<>(internalSecurityHeaders());
    }

    private HttpHeaders internalSecurityHeaders() {
        return internalSecurityHeaders(null);
    }

    private HttpHeaders internalSecurityHeaders(Purchase purchase) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            INTERNAL_SECURITY_HEADERS.forEach(headerName -> {
                String value = request.getHeader(headerName);
                if (!isBlank(value)) {
                    headers.set(headerName, value);
                }
            });
        }

        boolean hasUserContext = headers.getFirst("X-User-Id") != null && headers.getFirst("X-Username") != null;
        if (purchase != null && !hasUserContext && !isBlank(internalServiceToken)) {
            headers.set(INTERNAL_SERVICE_HEADER, PURCHASE_MICROSERVICE);
            headers.set(INTERNAL_SERVICE_TOKEN_HEADER, internalServiceToken);
        }

        if (headers.getFirst("X-Tenant-Id") == null && purchase != null && !isBlank(purchase.getTenantId())) {
            headers.set("X-Tenant-Id", purchase.getTenantId());
        }

        return headers;
    }

    private void validateElectronicMethod(PaymentMethod method) {
        if (method != PaymentMethod.CARD && method != PaymentMethod.NEQUI && method != PaymentMethod.PSE) {
            throw new IllegalArgumentException("Gateway payments only support CARD, NEQUI or PSE");
        }
    }

    private boolean isFinalized(Purchase purchase) {
        return purchase.getStatus() == PurchaseStatus.PAID
                || purchase.getStatus() == PurchaseStatus.INVOICED
                || purchase.getStatus() == PurchaseStatus.COMPLETED;
    }

    private String buildPaymentReference(Long purchaseId) {
        return "PAY-" + purchaseId + "-" + Instant.now().toEpochMilli();
    }

    private boolean isValidWebhookSignature(PaymentWebhookRequest request) {
        String signature = normalize(request.signature());
        if (SANDBOX_SIGNATURE.equals(signature)) {
            return true;
        }

        String expected = sha256(
                normalize(request.reference()) + "|"
                        + normalize(request.providerTransactionId()) + "|"
                        + request.amount().setScale(2, RoundingMode.HALF_UP) + "|"
                        + firstNonBlank(request.currency(), COP).toUpperCase(Locale.ROOT) + "|"
                        + normalize(request.status()).toUpperCase(Locale.ROOT) + "|"
                        + paymentWebhookSecret
        );

        return expected.equalsIgnoreCase(signature);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String normalizeInvoiceNumber(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (normalized.startsWith("NV-")) {
            return "I" + normalized;
        }
        return normalized;
    }

    private String normalize(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private String firstNonBlank(String first, String fallback) {
        return !isBlank(first) ? first.trim() : fallback;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String appendNote(String currentNotes, String note) {
        if (currentNotes == null || currentNotes.isBlank()) {
            return truncate(note, 800);
        }
        return truncate(currentNotes + "\n" + note, 800);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(value.length() - maxLength);
    }

    private PurchaseResponse toResponse(Purchase purchase) {
        PaymentStatus paymentStatus = purchase.getPaymentStatus() != null
                ? purchase.getPaymentStatus()
                : PaymentStatus.PENDING;
        BigDecimal paidAmount = purchase.getPaidAmount() != null
                ? purchase.getPaidAmount()
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        List<PurchaseItemResponse> itemResponses = purchase.getItems().stream()
                .map(i -> new PurchaseItemResponse(
                        i.getProductId(),
                        i.getProductName(),
                        i.getCategoryName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getLineTotal()
                ))
                .toList();

        return new PurchaseResponse(
                purchase.getId(),
                purchase.getCustomerId(),
                purchase.getCustomerDocumentType(),
                purchase.getCustomerDocumentNumber(),
                purchase.getCustomerFullName(),
                purchase.getStatus().name(),
                paymentStatus.name(),
                purchase.getPaymentMethod() != null ? purchase.getPaymentMethod().name() : null,
                purchase.getPaymentProvider() != null ? purchase.getPaymentProvider().name() : null,
                purchase.getPaymentReference(),
                purchase.getProviderTransactionId(),
                purchase.getProviderStatus(),
                purchase.getSubtotal(),
                purchase.getTax(),
                purchase.getTotal(),
                paidAmount,
                purchase.getCashReceived(),
                purchase.getChangeAmount(),
                purchase.getCashRegisterSessionId(),
                purchase.getInvoiceId(),
                purchase.getInvoiceNumber(),
                purchase.getCreatedAt(),
                purchase.getPaidAt(),
                purchase.getNotes(),
                purchase.getPaymentEvidenceNote(),
                itemResponses
        );
    }
}
