# HU-181A Tenant Billing Provider Settings

## Objective

Allow each business tenant to configure its own electronic billing provider settings instead of forcing every tenant to share one global provider token.

This is required for a multi-tenant product because one tenant may use `ALANUBE_SANDBOX` while another tenant keeps the safe `MOCK_DIAN` fallback.

## Scope

- Adds tenant-scoped billing provider settings in `invoice-microservice`.
- Adds endpoints for the current tenant only.
- Adds encrypted token storage using `BILLING_SETTINGS_ENCRYPTION_KEY`.
- Keeps `MOCK_DIAN` as the safe fallback when tenant settings do not exist.
- Keeps production billing and provider certification out of scope.

## Endpoints

- `GET /api/v1/billing/settings`
- `PUT /api/v1/billing/settings`
- `POST /api/v1/billing/settings/test`

The frontend does not send `tenantId`. The invoice service resolves the tenant from the trusted gateway context.

## Permissions

- `BUSINESS_OWNER` can read, update, and test billing settings.
- `BUSINESS_ADMIN` can read, update, and test billing settings when it has `BUSINESS_SETTINGS_UPDATE`.
- `CASHIER` is blocked.
- Platform users without tenant context cannot configure a business provider.

## Security

- Tokens are never returned in API responses.
- Responses only expose `tokenConfigured` and `tokenMasked`.
- Tokens are encrypted before storage.
- Token updates preserve the previous token when the request token is empty.
- No real provider credentials are stored in code, docs, or env examples.
- Missing or incomplete settings return controlled errors instead of raw stack traces.

## Provider Resolution

1. Resolve the current tenant from gateway headers.
2. Look up `tenant_billing_settings` by `tenant_id`.
3. If enabled settings exist, use the tenant provider.
4. For `ALANUBE_SANDBOX`, use tenant `baseUrl`, `username`, encrypted token, and timeout.
5. If no tenant settings exist, use the global provider configuration.
6. `MOCK_DIAN` remains the safe fallback.

Tenant A never reads Tenant B settings because lookups are scoped by the current tenant id.

## Configuration

Placeholder only:

```env
BILLING_SETTINGS_ENCRYPTION_KEY=
```

The key must be provided from a local ignored env file or a secret manager. Production should use managed secrets and rotation.

## Tests

Coverage added for:

- Tenant without settings uses fallback.
- Tenant with `ALANUBE_SANDBOX` resolves tenant settings.
- Token is not exposed in responses.
- Token masking.
- Empty token update preserves the previous token.
- `CASHIER` is blocked.
- `BUSINESS_OWNER` is allowed.
- `BUSINESS_ADMIN` with `BUSINESS_SETTINGS_UPDATE` is allowed.
- Platform user without tenant context is blocked.
- Tenant A does not use Tenant B settings.
- Settings test without token returns controlled `PROVIDER_FAILED`.
- `MOCK_DIAN` continues working.
- `ALANUBE_SANDBOX` incomplete settings fail controlled.

## Risks and Debt

- Use a real secret manager for production.
- Add token rotation and audit history.
- Add UI for tenant billing settings.
- Run sandbox smoke with credentials stored only in local/cloud secrets.
- Validate final provider payload against official provider documentation.
- Complete production DIAN certification separately.
