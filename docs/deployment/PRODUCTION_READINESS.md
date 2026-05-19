# Production Readiness Checklist

## Security

- [ ] Enforce HTTPS.
- [ ] Use a secret manager.
- [ ] Separate environment variables for dev, QA and production.
- [ ] Rotate JWT secrets.
- [ ] Replace shared service token with service-to-service JWT or mTLS.
- [ ] Enforce strict CORS.
- [ ] Add rate limiting.
- [ ] Harden Docker images.
- [ ] Prevent secret logging.
- [ ] Confirm branch protection and code review requirements.

## Database

- [ ] Use Liquibase/Flyway consistently.
- [ ] Backfill old records without tenant fields.
- [ ] Add tenant-scoped indexes.
- [ ] Replace global category name unique constraint with `(tenant_id, name)`.
- [ ] Add MongoDB indexes for `tenantId + documentNumber`.
- [ ] Configure backups.
- [ ] Test restore procedure.

## CI/CD

- [ ] Add automated builds.
- [ ] Add unit tests.
- [ ] Add integration tests.
- [ ] Add smoke tests.
- [ ] Add security checks.
- [ ] Add Docker image scanning.
- [ ] Add deployment approvals.

## Observability

- [ ] Centralized logs.
- [ ] Metrics.
- [ ] Distributed tracing.
- [ ] Health checks.
- [ ] Alerts.
- [ ] Error dashboards.
- [ ] Audit logs for auth, tenant and billing events.

## Deployment

- [ ] Separate dev, QA and production.
- [ ] Use external API Gateway/load balancer in production.
- [ ] Configure domain and SSL certificate.
- [ ] Make services stateless.
- [ ] Configure horizontal scaling.
- [ ] Configure replicas.
- [ ] Define resource limits.
- [ ] Define restart policies.

## Billing And Compliance

- [ ] Confirm real provider requirements.
- [ ] Use sandbox first.
- [ ] Store XML/PDF/QR artifacts.
- [ ] Track provider status and retry attempts.
- [ ] Document tax validity boundaries.

## Frontend

- [ ] Production build pipeline.
- [ ] Strict environment configuration.
- [ ] Route protection smoke tests.
- [ ] Token refresh or re-login strategy.
- [ ] User-friendly 403/401 pages.

## Final Go-Live Gate

- [ ] All required checks pass.
- [ ] Cross-tenant smoke test passes.
- [ ] Admin/cashier browser smoke test passes.
- [ ] Backup/restore tested.
- [ ] Provider strategy approved.
- [ ] Rollback plan documented.
