# HU-061 Purchase Payment Model

## Purpose

HU-061 introduces the base purchase microservice and the initial purchase/payment status model for RematePOS.

This user story creates the foundation for the POS checkout flow without approving payments, discounting inventory, generating invoices, registering cash movements, processing electronic gateway webhooks, handling returns, or integrating DIAN providers.

## Included Scope

- Adds `purchase-microservice` as a Maven module.
- Adds the base purchase application entry point.
- Adds base purchase entities:
  - `Purchase`
  - `PurchaseItem`
- Adds purchase and payment enums:
  - `PurchaseStatus`
  - `PaymentStatus`
  - `PaymentMethod`
  - `PaymentProvider`
- Adds checkout DTOs:
  - `PurchaseCheckoutRequest`
  - `PurchaseItemRequest`
  - `PurchaseItemResponse`
  - `PurchaseResponse`
- Adds the base checkout endpoint:
  - `POST /api/v1/purchases/checkout`
- Adds the base service rule: every checkout starts as `PENDING_PAYMENT` with payment status `PENDING`.
- Adds unit tests for the initial checkout behavior.

## Excluded Scope

This user story intentionally does not include:

- Cash payment validation.
- Cash movements.
- Cash register sessions.
- Electronic payment gateway transactions.
- Payment webhooks.
- Inventory stock deduction.
- Invoice generation.
- Invoice microservice.
- Returns or refunds.
- Docker Compose changes.
- API Gateway changes.
- DIAN, CUFE, CUDE, QR, XML, or PDF integration.

## Initial States

When a purchase is created through checkout:

- `purchase.status = PENDING_PAYMENT`
- `purchase.paymentStatus = PENDING`
- `purchase.paidAmount = 0.00`

The purchase exists as a pending commercial intent. It is not considered paid, invoiced, or completed.

## Why Inventory Is Not Deducted Yet

Inventory must only change after a payment is approved. HU-061 creates the pending purchase but does not confirm payment. Stock deduction belongs to a later user story after the payment flow has an approved backend state.

## Why Invoice Is Not Generated Yet

An invoice or POS document should be generated only after payment approval. HU-061 does not issue invoices because the checkout is still pending payment.

## Follow-Up User Stories

- HU-062: implement cash payment validation.
- HU-063: register basic cash movement.
- HU-064: add sandbox electronic payment and webhook approval.
- HU-065: synchronize approved payment with inventory and invoice generation.
- HU-078: add backend returns and refund support.
