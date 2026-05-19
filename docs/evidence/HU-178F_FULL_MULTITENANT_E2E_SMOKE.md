# HU-178F - Full Multi-Tenant E2E Smoke Validation

## Context
Validacion completa del flujo backend + frontend de RematePOS con seguridad, multi-tenant, usuarios por negocio, ventas y facturacion MOCK_DIAN.

## Stack tested
- Backend branch: `feature/HU-178F-CAVY-full-multitenant-e2e-smoke`
- Backend base branch: `feature/HU-178D-CAVY-tenant-user-management`
- Frontend branch: `feature/HU-178E-AFAF-tenant-onboarding-user-management-ui`
- API Gateway: `http://localhost:8080`
- Frontend URL usada: `http://localhost:3002`
- Evidence timestamp: `1779223119`

## Roles tested
- `PLATFORM_SUPER_ADMIN`
- `BUSINESS_OWNER`
- `CASHIER`

## Flow tested
1. Platform admin login.
2. Tenant creation.
3. Owner creation.
4. Owner login.
5. Cashier creation.
6. Cashier login.
7. Product/category creation by owner.
8. Sale by cashier.
9. MOCK_DIAN invoice generation.
10. Tenant isolation.
11. Cashier disable validation.

## Results
- Backend build: `BUILD SUCCESS`.
- Frontend build: `BUILD SUCCESS`.
- Gateway health: `200 / UP`.
- Local environment variables required for demo users, platform admin, JWT and internal service token were present as `SET`.
- `admin.demo` login: OK.
- `cashier.demo` login: OK.
- `platform.admin` login: OK.
- Login payloads included user, roles and permissions and did not expose `passwordHash`.
- `GET /api/v1/products` without token returned `401`.
- `GET /api/v1/products` with `cashier.demo` returned `200`.
- `POST /api/v1/products` with `cashier.demo` returned `403`.
- `GET /api/v1/products` with platform admin returned `403`.

### Tenant and users
- Tenant created: `smoke-full-store-1779223119`.
- Tenant id: `13`.
- Tenant appeared in platform tenant list.
- Tenant suspend/activate flow completed and final status was `ACTIVE`.
- Owner created and login succeeded.
- Owner tenant id: `13`.
- Cashier created and listed in tenant users.
- Cashier login succeeded.
- Cashier could not create tenant users: `403`.
- Cashier could not create products: `403`.
- Cashier disable validation: disabled cashier login returned `401`.
- Cashier was reactivated after validation to keep the tenant usable for demo.

### Product and sale
- Category id: `31`.
- Product id: `21`.
- Product: `Producto Full Smoke 1779223119`.
- Stock before sale: `5`.
- Stock after sale: `4`.
- Customer creation by cashier: OK.
- Purchase id: `46`.
- Purchase status: `INVOICED`.
- Payment status: `APPROVED`.
- Payment method: `CASH`.
- Payment provider: `INTERNAL`.

### Invoice
- Invoice id: `43`.
- Invoice number: `INV-20260519-46`.
- Provider: `MOCK_DIAN`.
- Provider status: `VALIDATED_SIMULATED`.
- Fiscal valid: `false`.
- CUFE present: yes.
- QR present: yes.
- XML demo present: yes.

### Frontend UI
- Platform admin saw `Negocios`.
- Platform admin saw the created tenant in the UI.
- Owner saw `Usuarios del negocio`.
- Owner did not see `Negocios`.
- Owner saw the created cashier in the users table.
- Cashier saw `Ventas`.
- Cashier did not see `Negocios`.
- Cashier did not see `Usuarios del negocio`.
- Cashier received a forbidden screen when opening `/inventory/new`.

## Security checks
- Cashier cannot manage users.
- Cashier cannot create products.
- Platform admin cannot operate inventory without tenant.
- Demo cashier cannot see the product created in the new tenant.
- New tenant owner did not see demo tenant products.
- Frontend does not send tenant id through internal headers.
- Backend resolves tenant by JWT/context.
- No passwords or full tokens were printed.
- No `.env` files were added.
- No screenshots were added.

## Known warnings
- `BillingCheckoutPage.jsx` has a known Unicode BOM warning during frontend build.
- JWT already issued revocation/session invalidation remains pending for future hardening.
- `npm audit` reports existing dependency advisories in the frontend dependency tree; this HU did not change dependencies.

## Conclusion
The full multi-tenant flow is ready for a functional demo: platform admin creates a business, owner manages users and catalog, cashier performs a sale, MOCK_DIAN invoice data is generated, stock is updated, and tenant isolation is preserved.
