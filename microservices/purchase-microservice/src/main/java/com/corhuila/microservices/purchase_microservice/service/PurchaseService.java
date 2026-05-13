package com.corhuila.microservices.purchase_microservice.service;

import com.corhuila.microservices.purchase_microservice.dto.PurchaseCheckoutRequest;
import com.corhuila.microservices.purchase_microservice.dto.ElectronicPaymentRequest;
import com.corhuila.microservices.purchase_microservice.dto.PaymentWebhookRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchasePaymentRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseReturnRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseReturnResponse;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseResponse;

import java.util.List;

public interface PurchaseService {

    PurchaseResponse checkout(PurchaseCheckoutRequest request);

    PurchaseResponse registerPayment(Long purchaseId, PurchasePaymentRequest request);

    PurchaseResponse createElectronicPayment(Long purchaseId, ElectronicPaymentRequest request);

    PurchaseResponse processPaymentWebhook(PaymentWebhookRequest request);

    PurchaseResponse cancel(Long id);

    PurchaseReturnResponse registerReturn(PurchaseReturnRequest request);

    PurchaseResponse getById(Long id);

    PurchaseResponse getByInvoiceNumber(String invoiceNumber);

    List<PurchaseResponse> getByCustomerId(String customerId);

    List<PurchaseResponse> getByDocument(String documentType, String documentNumber);
}
