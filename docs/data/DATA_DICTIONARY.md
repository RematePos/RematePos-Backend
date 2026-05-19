# RematePOS Data Dictionary

## User

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | Long | Si | User identifier | 1 | No | Si | Internal/public id |
| username | String | Si | Login username | admin.demo | No | Si | Unique |
| passwordHash | String | Si | BCrypt password hash | `$2a...` | Si | No | Never expose |
| enabled | Boolean | Si | Account enabled flag | true | No | Si | Used by auth |

## Tenant

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | Long/String | Si | Tenant identifier | 100 | No | Si | Used in JWT context |
| name | String | Si | Business name | Demo Store | No | Si | Display name |
| slug | String | Si | Tenant slug | demo-store | No | Si | Should be unique |
| status | String | Si | Tenant status | ACTIVE | No | Si | Used for access control |

## TenantMembership

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | Long | Si | Membership id | 10 | No | No | Internal |
| userId | Long | Si | User reference | 1 | No | No | Auth-owned |
| tenantId | Long/String | Si | Tenant reference | 100 | No | Si | Active tenant context |
| roleId | Long | Si | Role assigned in tenant | 2 | No | No | Drives RBAC |

## Role

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | Long | Si | Role id | 1 | No | No | Internal |
| name | String | Si | Role name | BUSINESS_OWNER | No | Si | Used by UI |
| description | String | No | Role description | Business owner | No | Si | Optional |

## Permission

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | Long | Si | Permission id | 1 | No | No | Internal |
| name | String | Si | Permission key | PRODUCTS_READ | No | Si | Used by frontend/backend |
| description | String | No | Permission description | Read products | No | Si | Optional |

## Product

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | Long | Si | Product id | 12 | No | Si | Tenant-scoped lookup |
| name | String | Si | Product name | Coffee | No | Si | Search/display |
| price | Decimal | Si | Sale price | 4500 | No | Si | Business data |
| stock | Integer | Si | Current stock | 4 | No | Si | Updated by sales/restock |
| categoryId | Long | No | Category reference | 5 | No | Si | Tenant-scoped category |
| tenantId | String | No | Tenant owner | tenant-demo | No | No | Comes from Gateway context |

## Category

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | Long | Si | Category id | 5 | No | Si | Tenant-scoped lookup |
| name | String | Si | Category name | Beverages | No | Si | Global unique should become tenant-scoped |
| description | String | No | Category description | Drinks | No | Si | Optional |
| tenantId | String | No | Tenant owner | tenant-demo | No | No | Comes from Gateway context |

## Customer

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | String | Si | Mongo document id | 64f... | No | Si | Tenant-scoped lookup |
| documentNumber | String | Si | Customer document | 222222222222 | Si | Si | Index with tenantId |
| name | String | Si | Customer name | Consumidor Final | Si | Si | Personal data |
| email | String | No | Customer email | demo@example.com | Si | Si | Optional |
| phone | String | No | Customer phone | 3000000000 | Si | Si | Optional |
| tenantId | String | No | Tenant owner | tenant-demo | No | No | Comes from Gateway context |

## Purchase

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | Long | Si | Purchase id | 44 | No | Si | Tenant-scoped |
| invoiceNumber | String | No | Generated invoice number | INV-20260519-38 | No | Si | Query with tenantId |
| customerId | String | No | Customer reference | 64f... | No | Si | Logical cross-service ref |
| customerDocumentNumber | String | No | Customer document snapshot | 222222222222 | Si | Si | Query with tenantId |
| total | Decimal | Si | Purchase total | 4500 | No | Si | Financial data |
| status | String | Si | Purchase status | INVOICED | No | Si | Flow status |
| paymentStatus | String | Si | Payment status | APPROVED | No | Si | Payment state |
| paymentReference | String | No | Payment reference | ref-123 | Si | No | Webhook lookup |
| tenantId | String | No | Tenant owner | tenant-demo | No | No | Persisted for webhook |

## Invoice

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | Long | Si | Invoice id | 20 | No | Si | Tenant-scoped |
| invoiceNumber | String | Si | Invoice number | INV-20260519-38 | No | Si | Query with tenantId |
| purchaseId | Long | Si | Purchase reference | 44 | No | Si | Tenant-scoped |
| total | Decimal | Si | Invoice total | 4500 | No | Si | Financial data |
| issuedAt | DateTime | Si | Issue date | 2026-05-19T10:00:00 | No | Si | Reporting |
| provider | String | No | Billing provider | MOCK_DIAN | No | Si | Future provider modes |
| providerStatus | String | No | Provider state | ACCEPTED_MOCK | No | Si | Future DIAN/Factus state |
| tenantId | String | No | Tenant owner | tenant-demo | No | No | Comes from Gateway/purchase |

## CashMovement

| Campo | Tipo | Requerido | Descripcion | Ejemplo | Sensible | Expuesto al frontend | Observaciones |
|---|---|---|---|---|---|---|---|
| id | Long | Si | Movement id | 80 | No | Si | Tenant-scoped reports |
| purchaseId | Long | No | Purchase reference | 44 | No | Si | Generated from payment |
| amount | Decimal | Si | Movement amount | 4500 | No | Si | Financial data |
| movementType | String | Si | Movement type | INCOME | No | Si | Cash register |
| paymentMethod | String | Si | Payment method | CASH | No | Si | Reporting |
| tenantId | String | No | Tenant owner | tenant-demo | No | No | Should filter reports |
