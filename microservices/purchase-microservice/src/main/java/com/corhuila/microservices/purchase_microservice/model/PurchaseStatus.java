package com.corhuila.microservices.purchase_microservice.model;

public enum PurchaseStatus {
    PENDING_PAYMENT,
    PAID,
    INVOICED,
    CANCELLED,
    FAILED,
    REFUNDED,
    COMPLETED,
    COMPLETED_WITHOUT_INVOICE
}

