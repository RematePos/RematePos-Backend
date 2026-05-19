# HU-178A – Billing Provider MOCK DIAN

## Context

RematePOS needs a professional billing provider abstraction to support DIAN-compatible providers in the future while keeping the academic demo fully functional without real credentials.

## Problem Solved

Invoice generation previously produced an internal invoice only. HU-178A adds a provider layer so the invoice can store provider state, simulated fiscal metadata and a clear demo warning without depending on real DIAN or Factus credentials.

## BillingProvider

`BillingProvider` is the internal contract used by `invoice-microservice` when an invoice is generated.

The flow is:

1. `InvoiceServiceImpl` creates or reuses the invoice.
2. `BillingProviderResolver` selects the provider from configuration.
3. The provider returns billing metadata.
4. The provider response is stored in the invoice.
5. The invoice response exposes safe provider fields to the client.

## Provider Strategy

- `MOCK_DIAN` by default.
- `FACTUS_SANDBOX` prepared for future credentials.
- `FACTUS_PRODUCTION` reserved for future real production use.

## MOCK_DIAN

Generates:

- Simulated CUFE.
- Simulated QR.
- Demo XML.
- Demo PDF URL/placeholder.
- `providerStatus=VALIDATED_SIMULATED`.
- `fiscalValid=false`.
- Validation message indicating no tax validity.

## Warning

MOCK_DIAN documents do not have tax/legal validity.

The visible demo message is:

`Documento generado en ambiente de demostración, sin validez tributaria.`

## FACTUS_SANDBOX

Prepared through environment variables:

- `FACTUS_API_BASE_URL`
- `FACTUS_API_KEY`
- `FACTUS_CLIENT_ID`
- `FACTUS_CLIENT_SECRET`

If credentials are missing, the provider fails safely with:

`Factus sandbox credentials are not configured.`

No real Factus or DIAN request is made in this HU.

## Flow

Purchase paid → Invoice generated → BillingProvider selected → Provider response stored → Invoice response returned.

## Pending

- Real provider credentials.
- Factus sandbox integration.
- Electronic POS equivalent document support.
- Real CUFE/CUDE validation.
- QR/XML/PDF from provider.
- Production DIAN/provider certification requirements.
- Liquibase/Flyway migration for invoice billing provider fields.
