# RematePOS Database Diagrams

## PostgreSQL ER Diagram

```mermaid
erDiagram
  AUTH_USERS ||--o{ TENANT_MEMBERSHIPS : has
  TENANTS ||--o{ TENANT_MEMBERSHIPS : contains
  ROLES ||--o{ TENANT_MEMBERSHIPS : grants
  AUTH_USERS ||--o{ USER_ROLES : has
  ROLES ||--o{ USER_ROLES : assigned
  ROLES ||--o{ ROLE_PERMISSIONS : has
  PERMISSIONS ||--o{ ROLE_PERMISSIONS : included

  TENANTS ||--o{ PRODUCTS : owns
  TENANTS ||--o{ CATEGORIES : owns
  CATEGORIES ||--o{ PRODUCTS : groups

  TENANTS ||--o{ PURCHASES : owns
  PURCHASES ||--o{ CASH_MOVEMENTS : produces
  PURCHASES ||--o| INVOICES : generates
  TENANTS ||--o{ INVOICES : owns
  TENANTS ||--o{ CASH_MOVEMENTS : owns

  AUTH_USERS {
    bigint id PK
    string username
    string password_hash
    boolean enabled
  }

  TENANTS {
    bigint id PK
    string name
    string slug
    string status
  }

  TENANT_MEMBERSHIPS {
    bigint id PK
    bigint user_id FK
    bigint tenant_id FK
    bigint role_id FK
  }

  ROLES {
    bigint id PK
    string name
    string description
  }

  PERMISSIONS {
    bigint id PK
    string name
    string description
  }

  USER_ROLES {
    bigint user_id FK
    bigint role_id FK
  }

  ROLE_PERMISSIONS {
    bigint role_id FK
    bigint permission_id FK
  }

  PRODUCTS {
    bigint id PK
    string name
    decimal price
    int stock
    bigint category_id FK
    string tenant_id
  }

  CATEGORIES {
    bigint id PK
    string name
    string description
    string tenant_id
  }

  PURCHASES {
    bigint id PK
    string invoice_number
    string customer_id
    string customer_document_number
    decimal total
    string status
    string payment_status
    string payment_reference
    string tenant_id
  }

  CASH_MOVEMENTS {
    bigint id PK
    bigint purchase_id FK
    decimal amount
    string movement_type
    string payment_method
    string tenant_id
  }

  INVOICES {
    bigint id PK
    string invoice_number
    bigint purchase_id FK
    decimal total
    datetime issued_at
    string provider
    string provider_status
    string tenant_id
  }
```

## MongoDB Logical Diagram

MongoDB does not enforce SQL foreign keys. This diagram is logical and describes ownership and tenant isolation.

```mermaid
erDiagram
  TENANTS ||--o{ CUSTOMERS : owns
  TENANTS ||--o{ CARTS : owns
  CUSTOMERS ||--o{ PURCHASES_LOGICAL : referenced_by
  CARTS ||--o{ CART_ITEMS : contains

  CUSTOMERS {
    string _id PK
    string documentNumber
    string name
    string email
    string phone
    string tenantId
  }

  CARTS {
    string _id PK
    string userId
    string tenantId
    datetime updatedAt
  }

  CART_ITEMS {
    string productId
    int quantity
    decimal unitPriceSnapshot
  }

  PURCHASES_LOGICAL {
    string purchaseId
    string customerDocumentNumber
    string tenantId
  }
```

## Notes

- `tenant_id` and `tenantId` are nullable during the migration period.
- Backfill is required before old records become visible in tenant-scoped queries.
- Tenant-scoped unique constraints and indexes should be added as follow-up work.
