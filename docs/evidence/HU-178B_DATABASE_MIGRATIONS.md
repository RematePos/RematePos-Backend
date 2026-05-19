# HU-178B - Formal Database Migrations

## Problem

The RematePOS security and billing stack added tenant isolation, RBAC and provider metadata across several services, but not every schema change was formally versioned. Several PostgreSQL services still depended on `spring.jpa.hibernate.ddl-auto=update`, and MongoDB tenant indexes were not created by the application.

## PostgreSQL Services Migrated

Formal Liquibase changelogs were added or completed for:

- `auth-microservice`
- `product-microservice`
- `purchase-microservice`
- `invoice-microservice`

Each changelog is owned by its microservice under:

`src/main/resources/db/changelog/db.changelog-master.yaml`

## MongoDB Services

`customer-microservice` now creates safe idempotent indexes at startup through `CustomerMongoIndexConfig`.

Indexes:

- `tenantId`
- `tenantId + documentType + documentNumber`
- `tenantId + email`

No unique MongoDB indexes were introduced because existing data may contain duplicates and requires a backfill/cleanup pass first.

`cart-microservice` does not currently expose a persistent cart document model in the backend code reviewed for this HU, so no schema was invented.

## Changelogs Created

Auth:

- `auth_users`
- `roles`
- `permissions`
- `user_roles`
- `role_permissions`
- `tenants`
- `tenant_memberships`

Product:

- Existing changelog master was aligned for relative includes.
- Existing product/category tenant changelog remains included.
- Tenant-aware indexes were added.

Purchase:

- `purchases`
- `purchase_items`
- `cash_movements`
- `tenant_id` on `purchases`
- `tenant_id` on `cash_movements`

Invoice:

- `invoices`
- `invoice_items`
- `tenant_id` on `invoices`
- Billing provider columns for MOCK_DIAN / FACTUS-ready mode.

## Fields Covered

Auth:

- `roles.scope`
- `tenants`
- `tenant_memberships`
- `user_roles`
- `role_permissions`

Product:

- `products.tenant_id`
- `categories.tenant_id`

Purchase:

- `purchases.tenant_id`
- `cash_movements.tenant_id`

Invoice:

- `invoices.tenant_id`
- `provider`
- `provider_environment`
- `provider_status`
- `provider_reference`
- `cufe`
- `cude`
- `qr_code`
- `xml_content`
- `pdf_url`
- `fiscal_valid`
- `validation_message`

Customer:

- `tenantId` indexes for tenant-scoped reads and document lookup.

## Indexes Added

Auth:

- `roles(name)`
- `tenants(slug)`
- `tenant_memberships(user_id, tenant_id)`
- `user_roles(user_id, role_id)`
- `role_permissions(role_id, permission_id)`

Product:

- `products(tenant_id)`
- `products(tenant_id, category_id)`
- `categories(tenant_id)`
- `categories(tenant_id, name)`

Purchase:

- `purchases(tenant_id)`
- `purchases(tenant_id, invoice_number)`
- `purchases(tenant_id, customer_id)`
- `purchases(payment_reference)`
- `cash_movements(tenant_id)`
- `cash_movements(tenant_id, sale_id)`

Invoice:

- `invoices(tenant_id)`
- `invoices(tenant_id, purchase_id)`
- `invoices(tenant_id, invoice_number)`
- `invoices(tenant_id, customer_id)`
- `invoices(provider_status)`

Customer MongoDB:

- `idx_customer_tenant_id`
- `idx_customer_tenant_document`
- `idx_customer_tenant_email`

## ddl-auto Strategy

PostgreSQL services now expose:

- `JPA_DDL_AUTO`
- `LIQUIBASE_ENABLED`

Recommended posture:

- Development: `JPA_DDL_AUTO=update` while the team verifies schema parity.
- QA/production: `JPA_DDL_AUTO=validate` after migrations are applied.
- Liquibase should stay enabled for PostgreSQL services.

This HU does not force a production cutover to `validate` for every environment because existing databases may still require one controlled migration/backfill pass.

## Backfill Pending

The following should be handled in a dedicated, reviewed migration:

- Backfill old products and categories without `tenant_id`.
- Backfill old purchases, invoices and cash movements without `tenant_id`.
- Backfill or quarantine old customers without `tenantId`.
- Decide whether historical demo records belong to a default demo tenant.

## Risks Mitigated

- New tenant and billing fields are now versioned.
- Fresh environments can create the expected tables before app usage.
- Existing environments can receive nullable fields and indexes without deleting data.
- Mongo customer lookups are indexed by tenant.
- QA/prod can move away from silent Hibernate schema mutation.

## Pending

- Replace the global `categories.name` unique constraint with a tenant-scoped unique constraint after duplicate analysis and cleanup.
- Move `tenant_id` columns to `NOT NULL` only after safe backfill.
- Add formal MongoDB migration tracking if customer/cart schema evolves beyond indexes.
- Validate generated SQL in QA before changing shared databases.
- Decide whether `RematePos-db` remains an infra orchestration repo or becomes a central migration runner.

## Legacy Destructive Rollbacks In Existing Product Changelogs

Pre-commit review detected destructive rollback operations in legacy product changelogs that already existed before this HU:

- `dropTable` in the original product and category table changelogs.
- `dropColumn` in the original tenant column changelog.
- `DROP EXTENSION` in the original PostgreSQL extension changelog.

HU-178B does not introduce new destructive rollback operations. New product work for this HU is limited to non-destructive tenant indexes in a separate changelog. The legacy rollback cleanup is intentionally left as a separate technical debt item because changing historical changesets can affect Liquibase checksums and already-applied environments.

No rollback was executed.
No data was deleted.
No volumes were removed.

## How To Test

Recommended non-destructive test flow:

1. Run Maven build.
2. Recreate only the required services with Docker Compose.
3. Verify Gateway health.
4. Login as cashier and admin.
5. Confirm cashier can read products but cannot create products.
6. Create a customer as cashier.
7. Create category/product as admin.
8. Execute checkout and cash payment as cashier.
9. Confirm invoice generation with MOCK_DIAN.
10. Confirm stock decreases.

Do not use `DROP`, `TRUNCATE` or volume deletion for this validation.
