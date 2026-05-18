# HU-175F - Backend Security Smoke Test

## Context
Security stack validated on top of:
- HU-175A Auth microservice
- HU-175C Gateway JWT validation
- HU-175B Tenant memberships and RBAC
- HU-175D Product/Category permission enforcement
- HU-175D.1 Product/Category tenant isolation
- HU-176A Purchase/Invoice tenant context
- HU-175E Customer permissions and tenant isolation

## Runtime Fix
The API Gateway must call auth-microservice using the internal Docker port:
- AUTH_SERVICE_URL=http://auth-microservice:8080
- SERVICES_AUTH_URL=http://auth-microservice:8080

The external mapped port 8096 must not be used for service-to-service communication inside Docker.

## Validated Results
- Gateway health returned 200 OK.
- Admin login succeeded.
- Cashier login succeeded.
- Tokens were not printed completely.
- No passwordHash was exposed.
- GET /api/v1/products without token returned 401.
- Cashier POST /api/v1/products returned 403.
- Cashier GET /api/v1/customers returned 200.
- Cashier POST /api/v1/customers returned 200.
- Cashier GET /api/v1/customers/document returned 200.
- Recent invoices returned 200 [].
- Cashier GET /api/v1/products returned 200 [].
- Admin POST /api/v1/categories returned 200.
- Admin POST /api/v1/products returned 200.
- Checkout with cashier returned 200.
- Cash payment returned 200.
- Purchase final status: INVOICED.
- Payment status: APPROVED.
- Generated invoice: INV-20260518-36.
- Stock decreased from 5 to 4.

## Local Schema Note
The local database did not have tenant_id columns for products/categories.
For smoke only, the following non-destructive ALTER TABLE statements were applied locally:

```sql
ALTER TABLE products ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(255);
ALTER TABLE categories ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(255);
```

These statements were not committed.
The real schema path remains the Liquibase changelog from HU-175D.1.

## Pending Advanced Smoke
- Cross-tenant isolation with a second tenant/user.
- Investigate why Liquibase did not auto-apply the changelog in this local stack.
- Backfill/migration strategy for shared environments.
- Frontend AuthContext and route protection.

## Conclusion
Backend security is ready to connect the frontend for the validated main flow:
login, JWT gateway, role permissions, customer tenant isolation, product/category tenant isolation, checkout, stock deduction and invoice generation.
