package com.corhuila.microservices.purchase_microservice.service.impl;

import com.corhuila.microservices.purchase_microservice.dto.PurchaseCheckoutRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseItemRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseItemResponse;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseResponse;
import com.corhuila.microservices.purchase_microservice.model.PaymentStatus;
import com.corhuila.microservices.purchase_microservice.model.Purchase;
import com.corhuila.microservices.purchase_microservice.model.PurchaseItem;
import com.corhuila.microservices.purchase_microservice.model.PurchaseStatus;
import com.corhuila.microservices.purchase_microservice.repository.PurchaseRepository;
import com.corhuila.microservices.purchase_microservice.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.19");
    private static final BigDecimal TAX_DIVISOR = new BigDecimal("1.19");

    private final PurchaseRepository repository;

    @Override
    @Transactional
    public PurchaseResponse checkout(PurchaseCheckoutRequest request) {
        Purchase purchase = new Purchase();
        purchase.setCustomerDocumentType(normalize(request.documentType()).toUpperCase());
        purchase.setCustomerDocumentNumber(normalize(request.documentNumber()));
        purchase.setCustomerId(purchase.getCustomerDocumentType() + "-" + purchase.getCustomerDocumentNumber());
        purchase.setCustomerFullName(normalize(request.customerFullName()));
        purchase.setStatus(PurchaseStatus.PENDING_PAYMENT);
        purchase.setPaymentStatus(PaymentStatus.PENDING);
        purchase.setNotes(normalizeNullable(request.notes()));

        request.items().forEach(itemRequest -> addItem(purchase, itemRequest));

        BigDecimal subtotal = purchase.getItems().stream()
                .map(PurchaseItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);

        purchase.setSubtotal(subtotal);
        purchase.setTax(tax);
        purchase.setTotal(subtotal.add(tax).setScale(2, RoundingMode.HALF_UP));
        purchase.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        return toResponse(repository.save(purchase));
    }

    private void addItem(Purchase purchase, PurchaseItemRequest request) {
        BigDecimal unitPrice = request.unitPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal lineTotal = unitPrice
                .multiply(BigDecimal.valueOf(request.quantity()))
                .setScale(2, RoundingMode.HALF_UP);

        PurchaseItem item = new PurchaseItem();
        item.setPurchase(purchase);
        item.setProductId(request.productId());
        item.setProductName(normalize(request.productName()));
        item.setQuantity(request.quantity());
        item.setUnitPrice(unitPrice);
        item.setLineTotal(lineTotal);
        purchase.getItems().add(item);
    }

    private PurchaseResponse toResponse(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getCustomerId(),
                purchase.getCustomerDocumentType(),
                purchase.getCustomerDocumentNumber(),
                purchase.getCustomerFullName(),
                purchase.getStatus().name(),
                purchase.getPaymentStatus().name(),
                purchase.getSubtotal(),
                purchase.getTax(),
                purchase.getTotal(),
                purchase.getPaidAmount(),
                purchase.getCreatedAt(),
                purchase.getNotes(),
                purchase.getItems().stream()
                        .map(item -> new PurchaseItemResponse(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getLineTotal()
                        ))
                        .toList()
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required value cannot be blank");
        }
        return value.trim();
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
