# HU-178C – Platform Super Admin and Tenant Onboarding

## Problem

RematePOS had a multi-tenant security foundation, but the runtime flow still depended on the seeded demo tenant and demo users.

There was no configurable `PLATFORM_SUPER_ADMIN` demo user and no backend endpoint to create real business tenants with an initial owner.

## Objective

Add backend support for:

- Optional platform administrator seed by environment.
- Tenant/business creation.
- Business owner creation.
- Tenant listing for platform admins and tenant users.
- Tenant activation and suspension.
- A secure base for future business user administration.

## Environment Variables

The platform administrator seed is disabled by default:

- `AUTH_SEED_PLATFORM_ADMIN=false`
- `AUTH_PLATFORM_ADMIN_USERNAME=platform.admin`
- `AUTH_PLATFORM_ADMIN_PASSWORD=change_me_use_secret_store`
- `AUTH_PLATFORM_ADMIN_EMAIL=platform.admin@example.com`

Passwords must be provided through environment configuration or a secret store. Real `.env` files and real secrets must not be committed.

## Platform Admin User

When `AUTH_SEED_PLATFORM_ADMIN=true` and a password is configured, auth creates or updates a platform administrator user with:

- Role: `PLATFORM_SUPER_ADMIN`
- Scope: `PLATFORM`
- No tenant membership by default.

If the seed is enabled but the password is missing, the user is not created and auth logs a safe warning without printing secrets.

## Added Endpoints

All endpoints require a valid JWT.

- `POST /api/v1/tenants`
  - Creates a tenant and its initial `BUSINESS_OWNER`.
  - Requires `PLATFORM_SUPER_ADMIN` or `TENANTS_CREATE`.

- `GET /api/v1/tenants`
  - Platform admins can list all tenants.
  - Tenant users can list only their active tenant memberships.

- `GET /api/v1/tenants/{tenantId}`
  - Platform admins can view any tenant.
  - Tenant users can view only their active tenant.

- `PATCH /api/v1/tenants/{tenantId}/suspend`
  - Suspends a tenant.
  - Requires `PLATFORM_SUPER_ADMIN` or `TENANTS_SUSPEND`.

- `PATCH /api/v1/tenants/{tenantId}/activate`
  - Reactivates a tenant.
  - Requires `PLATFORM_SUPER_ADMIN` or `TENANTS_SUSPEND`.

## Security

The tenant onboarding flow follows these rules:

- Passwords are BCrypt-hashed.
- Passwords and password hashes are never returned in API responses.
- Tenant slug must be unique.
- Owner username and email must be unique.
- Business owners are assigned through `TenantMembership` with role `BUSINESS_OWNER`.
- Tenant creation is not available to regular cashiers.
- Tenant suspension does not delete data or memberships.
- The frontend should continue sending only `Authorization: Bearer <token>`.

## Flow

`PLATFORM_SUPER_ADMIN login -> create tenant -> create owner -> owner login -> tenant active`

The platform administrator uses a platform-scoped token to create a business tenant. The backend creates the tenant as `ACTIVE`, creates the owner user, and links that owner to the tenant through an active `BUSINESS_OWNER` membership.

## Tests

Recommended smoke validation:

1. Login with `admin.demo`.
2. Login with `cashier.demo`.
3. Login with platform admin if enabled in the runtime environment.
4. Call `POST /api/v1/tenants` without a token and expect `401`.
5. Call `POST /api/v1/tenants` with cashier and expect `403`.
6. Call `POST /api/v1/tenants` with business owner and expect `403` unless tenant creation is explicitly granted.
7. Call `POST /api/v1/tenants` with platform admin and expect `201`.
8. Login with the new business owner.
9. Validate that the new owner operates in the new tenant.
10. Validate that demo users remain scoped to the demo tenant.

## Risks And Debts

- Frontend UI for tenant creation is pending.
- Platform admin runtime credentials must be managed through a secret store.
- Tenant onboarding audit logs are pending.
- Commercial activation workflows are pending.
- User and cashier management per business is reserved for HU-178D.

## Pending

- UI for creating businesses.
- User/cashier administration by business.
- Admin action audit trail.
- Commercial approval or activation workflow.
