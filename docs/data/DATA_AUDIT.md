# RematePOS Data Audit

## Scope

This audit summarizes the main data domains used by RematePOS after the security and tenant isolation work.

## Auth Domain

| Entity | Owner | Database | Main fields | Primary key | Relationships | Tenant field | DTO exposure | Sensitive data | Risk | Index recommendation | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| users | auth-microservice | PostgreSQL | username, passwordHash, enabled | id | roles, memberships | via membership | user summary only | passwordHash | leaking password hash | username unique | Implemented |
| roles | auth-microservice | PostgreSQL | name, description | id | permissions | none | role names | no | role drift | name unique | Implemented |
| permissions | auth-microservice | PostgreSQL | name, description | id | roles | none | permission names | no | missing permission checks | name unique | Implemented |
| tenants | auth-microservice | PostgreSQL | name, slug, status | id | memberships | id | active tenant summary | no | missing tenant selection | slug unique | Implemented |
| tenant_memberships | auth-microservice | PostgreSQL | user, tenant, role | id | user, tenant, role | tenant id | derived context | no | cross-tenant role confusion | user_id + tenant_id | Implemented |
| user_roles | auth-microservice | PostgreSQL | user_id, role_id | composite | user, role | none | derived roles | no | global role misuse | user_id, role_id | Implemented |
| role_permissions | auth-microservice | PostgreSQL | role_id, permission_id | composite | role, permission | none | derived permissions | no | stale permissions | role_id, permission_id | Implemented |

## Product Domain

| Entity | Owner | Database | Main fields | Primary key | Relationships | Tenant field | DTO exposure | Sensitive data | Risk | Index recommendation | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| products | product-microservice | PostgreSQL | name, price, stock, category, tenant_id | id | category | tenant_id | tenant hidden | no | old records without tenant_id | tenant_id, category_id, name | Implemented |
| categories | product-microservice | PostgreSQL | name, description, tenant_id | id | products | tenant_id | tenant hidden | no | global unique name blocks same name in different tenants | tenant_id + name unique | Partial |

## Customer Domain

| Entity | Owner | Database | Main fields | Primary key | Relationships | Tenant field | DTO exposure | Sensitive data | Risk | Index recommendation | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| customer collection | customer-microservice | MongoDB | documentNumber, name, email, phone, tenantId | _id | logical purchases/invoices | tenantId | tenant hidden | contact data | duplicate/global document rules | tenantId + documentNumber | Implemented |

## Purchase Domain

| Entity | Owner | Database | Main fields | Primary key | Relationships | Tenant field | DTO exposure | Sensitive data | Risk | Index recommendation | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| purchases | purchase-microservice | PostgreSQL | customer, items, totals, status, paymentStatus, paymentReference, tenantId | id | products, invoices, cash movements | tenantId | tenant hidden | payment reference | webhook lookup must remain controlled | tenantId + invoiceNumber, tenantId + customerDocument | Implemented |
| cash_movements | purchase-microservice | PostgreSQL | amount, type, paymentMethod, purchaseId, tenantId | id | purchase | tenantId | tenant hidden | financial data | reports must filter by tenant | tenantId + createdAt | Partial |

## Invoice Domain

| Entity | Owner | Database | Main fields | Primary key | Relationships | Tenant field | DTO exposure | Sensitive data | Risk | Index recommendation | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| invoices | invoice-microservice | PostgreSQL | invoiceNumber, purchaseId, customer snapshot, total, issuedAt, provider fields, tenantId | id | purchase | tenantId | tenant hidden | fiscal/customer data | invoice number lookup must be tenant-scoped | tenantId + invoiceNumber, tenantId + purchaseId | Implemented |

Provider fields may include mock status now and real provider fields later, such as CUFE/CUDE, QR, XML/PDF references and provider status.

## Cart Domain

| Entity | Owner | Database | Main fields | Primary key | Relationships | Tenant field | DTO exposure | Sensitive data | Risk | Index recommendation | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| cart collection | cart-microservice | MongoDB | user/cart items/product references | _id | products | should use tenant context | cart data | no | tenant rules must be reviewed | userId + tenantId | Pending audit |

## Data Ownership

| Data | Owner microservice |
|---|---|
| Authentication, users, roles, permissions, tenants | auth-microservice |
| Products and categories | product-microservice |
| Customers | customer-microservice |
| Purchases, payment state and cash movements | purchase-microservice |
| Invoices and fiscal/documentary output | invoice-microservice |
| Carts | cart-microservice |

Services should not write another service's owned data directly.

## Tenant Isolation Rules

Tenant-scoped reads and writes:

- Products: filter by `tenant_id`.
- Categories: filter by `tenant_id`.
- Customers: filter by `tenantId`.
- Purchases: filter by `tenantId`.
- Invoices: filter by `tenantId`.
- Cash movements: reports should filter by `tenantId`.

The frontend never supplies tenant data in request bodies.

## Data Quality Risks

- Old products without `tenant_id` do not appear in tenant-scoped product queries.
- Old categories without `tenant_id` do not appear in tenant-scoped category queries.
- Old purchases, invoices and cash movements without `tenantId` need backfill.
- Global unique constraints, especially `categories.name`, should become tenant-scoped.
- MongoDB indexes for customer tenant/document lookup should be confirmed.
- Purchase/invoice schema management should move to Liquibase or Flyway.
- Local smoke tests showed Liquibase may not auto-apply in some stacks and requires investigation.
