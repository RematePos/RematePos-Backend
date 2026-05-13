package com.corhuila.microservices.purchase_microservice.service;

import com.corhuila.microservices.purchase_microservice.dto.PurchaseCheckoutRequest;
import com.corhuila.microservices.purchase_microservice.dto.PurchaseResponse;

public interface PurchaseService {

    PurchaseResponse checkout(PurchaseCheckoutRequest request);
}
