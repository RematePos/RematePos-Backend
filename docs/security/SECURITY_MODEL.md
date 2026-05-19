# RematePOS Security Model

## Overview

RematePOS uses JWT authentication, API Gateway validation, RBAC permissions and tenant-scoped data access. The frontend sends only an `Authorization: Bearer <token>` header. The API Gateway validates the token and generates trusted internal headers for downstream microservices.

## Authentication

Authentication is handled by `auth-microservice`.

Flow:

1. User submits username and password.
2. Auth loads the user, tenant membership, roles and permissions.
3. Password is validated with BCrypt.
4. Auth returns a JWT and user context.
5. Frontend stores the session in `sessionStorage`.

Passwords are never stored by the frontend and password hashes must not be returned in API responses.

## JWT

The JWT represents the authenticated user and tenant context. It is validated by the API Gateway before requests are forwarded to internal services.

The frontend must not send internal identity or tenant headers directly.

## Gateway Internal Headers

After validating a JWT, the API Gateway adds:

- `X-User-Id`
- `X-Username`
- `X-Roles`
- `X-Permissions`
- `X-Tenant-Id`
- `X-Tenant-Slug`

The Gateway also strips protected internal headers supplied by external clients, including service-to-service headers:

- `X-Internal-Service`
- `X-Internal-Service-Token`

## What The Frontend Does Not Send

The frontend does not send:

- `X-User-Id`
- `X-Username`
- `X-Roles`
- `X-Permissions`
- `X-Tenant-Id`
- `X-Tenant-Slug`

Only `Authorization: Bearer <token>` is sent.

## RBAC

Microservices validate permissions from trusted Gateway headers. Menu hiding in the frontend is not considered a security control.

### Permission Matrix

| Permission | Purpose | Typical roles |
|---|---|---|
| `PRODUCTS_READ` | List/read products | CASHIER, BUSINESS_ADMIN, BUSINESS_OWNER |
| `PRODUCTS_CREATE` | Create products | BUSINESS_ADMIN, BUSINESS_OWNER |
| `PRODUCTS_UPDATE` | Update products or stock | BUSINESS_ADMIN, BUSINESS_OWNER |
| `PRODUCTS_DELETE` | Delete products | BUSINESS_OWNER |
| `CATEGORIES_READ` | List/read categories | CASHIER, BUSINESS_ADMIN, BUSINESS_OWNER |
| `CATEGORIES_CREATE` | Create categories | BUSINESS_ADMIN, BUSINESS_OWNER |
| `CATEGORIES_UPDATE` | Update/delete categories | BUSINESS_ADMIN, BUSINESS_OWNER |
| `CUSTOMERS_READ` | List/read customers | CASHIER, BUSINESS_ADMIN, BUSINESS_OWNER |
| `CUSTOMERS_CREATE` | Create customers | CASHIER, BUSINESS_ADMIN, BUSINESS_OWNER |
| `SALES_READ` | Read sales/purchases | BUSINESS_ADMIN, BUSINESS_OWNER |
| `SALES_CREATE` | Create checkout/sales | CASHIER, BUSINESS_ADMIN, BUSINESS_OWNER |
| `INVOICES_READ` | Read invoices and invoice copies | CASHIER, BUSINESS_ADMIN, BUSINESS_OWNER |
| `RETURNS_CREATE` | Register returns | BUSINESS_ADMIN, BUSINESS_OWNER |
| `CASH_REGISTER_READ` | Read cash register data | BUSINESS_ADMIN, BUSINESS_OWNER |
| `CASH_REGISTER_OPEN` | Open register | BUSINESS_ADMIN, BUSINESS_OWNER |
| `CASH_REGISTER_CLOSE` | Close register | BUSINESS_ADMIN, BUSINESS_OWNER |

## Multi-Tenant Isolation

Tenant context comes from the JWT through Gateway-generated headers. It is not accepted from request bodies.

Tenant-scoped domains:

- Products
- Categories
- Customers
- Purchases
- Invoices
- Cash movements

User-facing queries must include tenant filtering. If a record belongs to a different tenant, services should respond as if it is not visible.

## Microservice Responsibilities

| Service | Security responsibility |
|---|---|
| API Gateway | Validate JWT, generate internal headers, strip spoofed internal headers |
| Auth | Authenticate users, issue JWT, seed RBAC/demo users |
| Product | Enforce product/category permissions and tenant isolation |
| Customer | Enforce customer permissions and tenant isolation |
| Purchase | Enforce sales permissions, tenant-scoped purchase flow |
| Invoice | Enforce invoice permissions and tenant-scoped invoice queries |
| Cart | Should use authenticated user/tenant context where applicable |

## Service-To-Service Token

Webhook finalization may run without an end-user JWT. For that controlled path, `purchase-microservice` can call product/invoice using:

- `X-Internal-Service: purchase-microservice`
- `X-Internal-Service-Token`
- `X-Tenant-Id`

The token comes from environment/config, is not logged, and should reject empty or placeholder values in real environments.

Allowed internal endpoints:

- `POST /api/v1/products/purchase`
- `POST /api/v1/products/restock`
- `POST /api/v1/invoices/generate`

This does not open general CRUD or query endpoints.

Future improvement: replace shared internal token with service-to-service JWT or mTLS.

## Public And Protected Endpoints

Public:

- Auth login.
- Health endpoints, depending on deployment policy.

Protected:

- Product/category CRUD and listing.
- Customer listing/search/create/update.
- Purchase checkout/payment/returns/history.
- Invoice generation/query/copy.
- Any analytics/reporting endpoint.

## 401 vs 403

| Status | Meaning |
|---|---|
| 401 Unauthorized | Missing or invalid authentication |
| 403 Forbidden | Authenticated but missing tenant or permission |

## Risks Mitigated

| Risk | Mitigation |
|---|---|
| Header spoofing | Gateway strips protected headers from external traffic |
| Direct access without token | Microservices require trusted context |
| Cashier creating products | `PRODUCTS_CREATE` required |
| Cross-tenant data access | Tenant-scoped repositories and service checks |
| Webhook without JWT failing | Controlled service-to-service path using persisted purchase tenant |
