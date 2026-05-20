# HU-180A Billing Provider Sandbox Integration

## Objective

Add a configurable sandbox billing provider integration for electronic invoicing tests, without breaking the existing `MOCK_DIAN` behavior.

## Provider Added

- Provider key: `ALANUBE_SANDBOX`
- Adapter: `AlanubeSandboxBillingProvider`
- Payload mapper: `AlanubeSandboxInvoiceMapper`
- Default provider remains: `MOCK_DIAN`
- Reserved provider kept: `FACTUS_SANDBOX`

## Configuration Variables

Use placeholders only. Real sandbox credentials must be provided through local ignored environment files or a secret manager.

```env
BILLING_PROVIDER=MOCK_DIAN
BILLING_PROVIDER_BASE_URL=https://sandbox-api.example.com/e-provider/co/v1
BILLING_PROVIDER_USERNAME=example@example.com
BILLING_PROVIDER_TOKEN=
BILLING_PROVIDER_TIMEOUT_MS=10000
```

## Security

- No provider token is hardcoded.
- The token is sent only as an HTTP authorization header.
- The token is not logged.
- Provider failures return controlled `PROVIDER_FAILED` responses.
- Invoice DTOs do not expose provider credentials.
- Sandbox credentials are not production credentials.

## Fallback

If `BILLING_PROVIDER` is empty or set to `MOCK_DIAN`, the service uses the simulated DIAN provider.

If `ALANUBE_SANDBOX` is selected but required credentials are missing, the invoice flow remains controlled and returns provider status `PROVIDER_FAILED`.

## Payload

The sandbox payload is built from `InvoiceGenerateRequest` through `BillingProviderRequest` and includes:

- internal invoice number;
- purchase id;
- issued date;
- customer id and document data;
- customer full name;
- item product id, description, quantity, unit price, and line total;
- subtotal, tax, and total.

Pending provider-schema validation:

- final endpoint path;
- final tax structure;
- final customer identification catalog values;
- final response field names for CUFE, CUDE, QR, XML, and PDF.

## Tests

Expected test coverage:

- `MOCK_DIAN` still returns `VALIDATED_SIMULATED`.
- `ALANUBE_SANDBOX` fails safely when token/configuration is missing.
- Provider failure messages do not expose tokens.
- Resolver selects `MOCK_DIAN`, `ALANUBE_SANDBOX`, and reserved `FACTUS_SANDBOX`.
- HTTP errors are mapped to `PROVIDER_FAILED`.
- `InvoiceResponse` does not expose provider secrets.

## Smoke

Recommended local smoke:

- With `BILLING_PROVIDER=MOCK_DIAN`: create sale, payment, and invoice; expect provider `MOCK_DIAN` and status `VALIDATED_SIMULATED`.
- With `BILLING_PROVIDER=ALANUBE_SANDBOX` and no token: create invoice; expect controlled status `PROVIDER_FAILED`.

Do not print real provider tokens or full provider responses that may contain sensitive data.

## Risks and Debt

- The sandbox endpoint path and exact payload format must be validated with the provider documentation.
- This is a sandbox integration, not production billing.
- Production enablement requires credential management, provider certification, retry strategy, audit logs, and operational monitoring.
