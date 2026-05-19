# Final Deliverables Index

This index links the final technical documentation package for RematePOS.

## Architecture

- [Architecture diagrams](../architecture/REMATEPOS_ARCHITECTURE_DIAGRAMS.md)

## Security

- [Security model](../security/SECURITY_MODEL.md)
- [Sensitive files audit](../security/SENSITIVE_FILES_AUDIT.md)

## Data And Database

- [Data audit](../data/DATA_AUDIT.md)
- [Data dictionary](../data/DATA_DICTIONARY.md)
- [Database diagrams](../database/DATABASE_DIAGRAMS.md)

## Deployment And Smoke Tests

- [Local deployment and smoke test](../deployment/LOCAL_DEPLOYMENT_AND_SMOKE_TEST.md)
- [Production readiness](../deployment/PRODUCTION_READINESS.md)

## Billing

- [Billing provider strategy](../billing/BILLING_PROVIDER_STRATEGY.md)

## Evidence

- [HU-175F backend security smoke test](../evidence/HU-175F_BACKEND_SECURITY_SMOKE_TEST.md)
- [HU-175H E2E security smoke test](../evidence/HU-175H_E2E_SECURITY_SMOKE_TEST.md)
- [HU-175I gateway auth runtime routing fix](../evidence/HU-175I_GATEWAY_AUTH_RUNTIME_ROUTING_FIX.md)

## Related Pull Requests

Backend:

- HU-175A / PR #20: Auth microservice, JWT, BCrypt and RBAC foundation.
- HU-175C / PR #21: API Gateway JWT validation.
- HU-175B / PR #22: Tenant memberships and multi-tenant RBAC.
- HU-175D / PR #23: Product and category permission enforcement.
- HU-175D.1 / PR #24: Product and category tenant isolation.
- HU-176A / PR #25: Purchase, invoice and cash movement tenant context.
- HU-175E / PR #26: Customer permissions and tenant isolation.
- HU-175F / PR #27: Backend security smoke validation.
- HU-175H / PR #28: Frontend/backend E2E security smoke evidence.
- HU-175I: Gateway auth runtime routing fix.

Frontend:

- HU-175G / RematePos-Frontend PR #26: Frontend authentication and RBAC integration.

## Related Branches

- `feature/HU-175A-CAVY-auth-service-jwt-rbac`
- `feature/HU-175C-CAVY-gateway-jwt-validation`
- `feature/HU-175B-CAVY-tenant-membership-rbac`
- `feature/HU-175D-CAVY-microservice-permission-enforcement`
- `feature/HU-175D1-CAVY-tenant-inventory-isolation`
- `feature/HU-176A-CAVY-tenant-sales-invoice-foundation`
- `feature/HU-175E-CAVY-customer-security-tenant-isolation`
- `feature/HU-175F-CAVY-backend-security-smoke-fixes`
- `feature/HU-175H-CAVY-e2e-security-smoke-evidence`
- `feature/HU-175I-CAVY-gateway-auth-runtime-routing-fix`
- `feature/HU-175G-AFAF-frontend-auth-rbac-integration`

## Pending Items

- Cross-tenant smoke test with a second tenant/user.
- Backfill old tenant-less records.
- Liquibase/Flyway migration consistency for purchase/invoice.
- Tenant-scoped unique constraints and indexes.
- Analytics microservice and dashboard.
- Real DIAN/provider integration.
- Production observability and CI/CD hardening.
