package com.corhuila.microservices.invoice_microservice.billing.provider;

public interface BillingProvider {

    BillingProviderResponse issueInvoice(BillingProviderRequest request);

    String getProviderName();

    boolean supportsSandbox();
}
