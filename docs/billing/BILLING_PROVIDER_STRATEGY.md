# Billing Provider Strategy

## Current State

RematePOS currently generates invoices through the invoice microservice. The validated demo flow supports checkout, cash payment, stock deduction and invoice generation.

The current professional demo strategy is to use a mock billing provider mode while keeping the architecture ready for a real DIAN/POS electronic provider.

## MOCK_DIAN Mode

`MOCK_DIAN` represents a professional simulation of the billing provider flow.

It can generate:

- Invoice number.
- Provider status.
- Mock acceptance state.
- Internal evidence that the purchase was invoiced.

## Tax Validity

Mock invoices have no tax validity. They are useful for:

- Academic demonstration.
- End-to-end system validation.
- UI/UX validation.
- Internal invoice workflow validation.

They are not valid for official tax reporting.

## Real Provider Requirements

A real provider integration should define:

- Provider credentials.
- Sandbox access.
- Production access.
- Certificate or resolution requirements, if applicable.
- CUFE/CUDE generation or provider response.
- QR generation.
- XML generation/storage.
- PDF generation/storage.
- Provider webhooks.
- POS electronic equivalent document support.
- Error and retry strategy.
- Audit trail.

## Environment Strategy

Recommended provider modes:

| Variable | Meaning |
|---|---|
| `BILLING_PROVIDER=MOCK_DIAN` | Local/demo mode without tax validity |
| `BILLING_PROVIDER=FACTUS_SANDBOX` | Provider sandbox mode |
| `BILLING_PROVIDER=FACTUS_PRODUCTION` | Provider production mode |

## Questions For Provider

Ask the provider:

- Is there a free sandbox?
- Does the sandbox transmit to DIAN or simulate only?
- Is POS electronic document supported?
- What are the costs?
- What are the monthly limits?
- Are Java/Spring examples available?
- Is there webhook support?
- Are XML, PDF, QR and CUFE/CUDE returned by API?
- What certificates or resolutions are required?
- How are test credentials issued?

## Recommended Roadmap

1. Keep `MOCK_DIAN` for final academic demo.
2. Add provider abstraction in invoice microservice.
3. Add sandbox provider adapter.
4. Add provider status and retry model.
5. Add PDF/XML storage strategy.
6. Add production provider mode only after legal/tax requirements are confirmed.
