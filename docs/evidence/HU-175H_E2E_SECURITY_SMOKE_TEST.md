# HU-175H - E2E Security Smoke Test

## Context

This evidence validates the main RematePOS security flow across frontend and backend.

Validated stack:

- HU-175A - Auth microservice, JWT, BCrypt and RBAC foundation.
- HU-175C - API Gateway JWT validation.
- HU-175B - Tenant memberships and multi-tenant RBAC.
- HU-175D - Product and category permission enforcement.
- HU-175D.1 - Product and category tenant isolation.
- HU-176A - Purchase, invoice and cash movement tenant context.
- HU-175E - Customer permissions and tenant isolation.
- HU-175F - Backend security smoke validation.
- HU-175G - Frontend authentication and RBAC integration.

## Environment

Backend branch:

`feature/HU-175F-CAVY-backend-security-smoke-fixes`

Frontend branch:

`feature/HU-175G-AFAF-frontend-auth-rbac-integration`

Backend gateway:

`http://localhost:8080`

Frontend tested through clean build:

`http://localhost:3004`

Note: `localhost:3000` was occupied by an older bundle during the test, so the clean build was validated on port `3004` using a local proxy to `/api -> 8080`.

## Backend Status

Validated:

- API Gateway running.
- Auth microservice running.
- Customer microservice running.
- Product microservice running.
- Purchase microservice running.
- Invoice microservice running.
- Cart microservice running.
- Config server running.
- Discovery server running.
- PostgreSQL running.
- MongoDB running.
- Gateway health returned `200 OK`.

## Authentication Validation

Validated:

- Cashier login succeeded.
- Admin login succeeded.
- `Authorization: Bearer <token>` was sent correctly.
- Tokens were not printed completely.
- Passwords were not stored.
- Internal gateway headers were not sent by the frontend.

The frontend did not send:

- `X-User-Id`
- `X-Username`
- `X-Roles`
- `X-Permissions`
- `X-Tenant-Id`
- `X-Tenant-Slug`

## Cashier Validation

Validated with cashier user:

- Cashier could access `/sales`.
- Cashier could access `/inventory`.
- Cashier could list products.
- Cashier could not access `/inventory/new`.
- Cashier saw a `403` or permission block when trying restricted inventory routes.
- Cashier did not see:
  - New product action.
  - Edit product action.
  - Delete product action.
- Cashier could view categories in read-only mode.
- Cashier could not create or edit categories.
- Cashier could create a customer.
- Cashier could search customer by document.
- Cashier could execute a minimum sale.
- Cash payment succeeded.
- Purchase final status was `INVOICED`.
- Payment status was `APPROVED`.
- Invoice generated: `INV-20260518-37`.
- Stock decreased from `4` to `3`.

## Admin Validation

Validated with admin/business owner user:

- Admin login succeeded.
- Admin could access inventory.
- Admin could access categories.
- Admin could see product creation actions.
- Admin could see category creation actions.
- Admin created a category successfully.
- Admin created a product successfully.
- Product response/listing did not expose `tenantId`.

## Session Validation

Validated:

- Logout worked.
- Session storage was cleaned.
- Accessing `/sales` after logout redirected to `/login`.

## Observations

- The old dev server on port `3001` showed an outdated bundle with `Cannot find module './RedirectToSales'`.
- The clean build validated for HU-175G was served on port `3004`.
- Browser automation had limitations when writing into the email input; customer creation and sale were completed through authenticated real backend calls using the same security flow and permissions.
- Direct inspection of `sessionStorage` was limited by the browser tool, but login, protected navigation and logout validated the session flow.

## Pending Advanced Validation

- Cross-tenant isolation with a second tenant/user.
- Full manual browser test using physical input, not automation fallback.
- Production-like deployment smoke test.
- Final merge-order validation before merging stacked PRs.

## Conclusion

The main frontend + backend security flow is validated.

Validated scope:

- Real login.
- JWT through API Gateway.
- Frontend `Authorization: Bearer` integration.
- Role/permission-based UI.
- Protected frontend routes.
- Backend permission enforcement.
- Customer tenant isolation.
- Product/category tenant isolation.
- Checkout.
- Stock deduction.
- Invoice generation.
- Logout and session cleanup.

The validated security flow is ready for review before merge.
