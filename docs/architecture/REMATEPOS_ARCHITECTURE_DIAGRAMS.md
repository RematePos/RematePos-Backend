# RematePOS Architecture Diagrams

This document describes the current RematePOS architecture after the security, RBAC, tenant isolation and smoke validation work.

## 1. General Architecture

```mermaid
flowchart LR
  FE["Frontend React"] --> GW["API Gateway"]

  GW --> AUTH["Auth Microservice"]
  GW --> PROD["Product Microservice"]
  GW --> CUST["Customer Microservice"]
  GW --> PUR["Purchase Microservice"]
  GW --> INV["Invoice Microservice"]
  GW --> CART["Cart Microservice"]

  CFG["Config Server"] --> GW
  CFG --> AUTH
  CFG --> PROD
  CFG --> CUST
  CFG --> PUR
  CFG --> INV
  CFG --> CART

  DISC["Discovery Server"] --> GW
  DISC --> AUTH
  DISC --> PROD
  DISC --> CUST
  DISC --> PUR
  DISC --> INV
  DISC --> CART

  AUTH --> PG["PostgreSQL"]
  PROD --> PG
  PUR --> PG
  INV --> PG
  CUST --> MONGO["MongoDB"]
  CART --> MONGO

  INV --> MOCK["MOCK_DIAN"]
  INV -. "future provider" .-> FACTUS["FACTUS / DIAN provider"]
```

## 2. Login and JWT Flow

```mermaid
sequenceDiagram
  participant U as User
  participant FE as Frontend React
  participant GW as API Gateway
  participant AUTH as Auth Microservice
  participant DB as PostgreSQL

  U->>FE: Enter username/password
  FE->>GW: POST /api/v1/auth/login
  GW->>AUTH: Route login request
  AUTH->>DB: Load user, tenant memberships, roles, permissions
  AUTH->>AUTH: Validate BCrypt password
  AUTH-->>GW: JWT, user, roles, permissions, tenant context
  GW-->>FE: Auth response
  FE->>FE: Store token/user/roles/permissions in sessionStorage
  FE->>GW: Request with Authorization Bearer token
  GW->>GW: Validate JWT
  GW->>GW: Generate internal headers
  GW->>PROD: Forward with X-User, X-Roles, X-Permissions, X-Tenant
```

Gateway-generated internal headers:

- `X-User-Id`
- `X-Username`
- `X-Roles`
- `X-Permissions`
- `X-Tenant-Id`
- `X-Tenant-Slug`

The frontend only sends `Authorization: Bearer <token>`.

## 3. Sale and Invoice Flow

```mermaid
sequenceDiagram
  participant C as Cashier
  participant FE as Frontend
  participant GW as API Gateway
  participant PUR as Purchase Microservice
  participant CUST as Customer Microservice
  participant PROD as Product Microservice
  participant INV as Invoice Microservice
  participant BILL as MOCK_DIAN / future DIAN provider
  participant PG as PostgreSQL
  participant MONGO as MongoDB

  C->>FE: Select product and customer
  FE->>GW: Checkout with Authorization Bearer
  GW->>PUR: Forward tenant/user context
  PUR->>CUST: Validate customer by tenant
  CUST->>MONGO: Read customer
  PUR->>PROD: Validate stock by tenant
  PROD->>PG: Read product and stock
  PUR->>PG: Create purchase with tenantId
  C->>FE: Confirm cash payment
  FE->>GW: Approve cash payment
  GW->>PUR: Forward request
  PUR->>PROD: POST /products/purchase
  PROD->>PG: Decrease stock
  PUR->>INV: POST /invoices/generate
  INV->>PG: Create invoice with tenantId
  INV->>BILL: Mock/future tax provider flow
  PUR->>PG: Final status INVOICED / APPROVED
```

## 4. Multi-Tenant Model

```mermaid
flowchart TD
  TENANT["Tenant"] --> TM["TenantMembership"]
  USER["User"] --> TM
  TM --> ROLE["Role"]
  ROLE --> PERM["Permission"]

  TENANT --> PROD["Products.tenant_id"]
  TENANT --> CAT["Categories.tenant_id"]
  TENANT --> CUST["Customers.tenantId"]
  TENANT --> PUR["Purchases.tenantId"]
  TENANT --> INV["Invoices.tenantId"]
  TENANT --> CASH["CashMovements.tenantId"]

  GW["API Gateway"] --> HEADERS["Trusted tenant headers"]
  HEADERS --> PROD
  HEADERS --> CAT
  HEADERS --> CUST
  HEADERS --> PUR
  HEADERS --> INV
```

Tenant isolation rule: user-facing queries must filter by tenant context. Old records without tenant data require backfill before they can appear in tenant-scoped results.

## 5. Roles and Permissions

```mermaid
flowchart LR
  PSA["PLATFORM_SUPER_ADMIN"] --> PALL["Platform operations"]
  OWNER["BUSINESS_OWNER"] --> MALL["Business management"]
  ADMIN["BUSINESS_ADMIN"] --> OPS["Inventory, categories, invoices, users"]
  CASHIER["CASHIER"] --> POS["Sales, customers, invoice copy"]
  QA["QA_SUPPORT"] --> QAOPS["Support and test visibility"]

  MALL --> PRODUCTS["PRODUCTS_*"]
  MALL --> CATEGORIES["CATEGORIES_*"]
  MALL --> CUSTOMERS["CUSTOMERS_*"]
  MALL --> SALES["SALES_*"]
  MALL --> INVOICES["INVOICES_*"]
  OPS --> PRODUCTS
  OPS --> CATEGORIES
  POS --> SALES
  POS --> CUSTOMERS
  POS --> INVOICES
```

Permission checks are enforced in microservices. Frontend menu visibility is a convenience layer, not a security boundary.
