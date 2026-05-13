# HU-120 - Functional Backend Baseline Evidence

## Purpose

This document preserves the validated local backend baseline for RematePOS.

The branch `feature/HU-120-CAVY-functional-backend-baseline` is intended to upload the backend state that was already validated locally before splitting the implementation into smaller, reviewable user stories.

This is not the final architectural separation by HU. It is a functional preservation baseline created so the team and evaluator can inspect the real working state, the technical decisions, the evidence, and the pending decomposition work.

The work was organized with AI-assisted local development support. The evidence in this document reflects actual local validation and does not attempt to hide that assistance.

## Validated Local Context

The validated backend baseline was executed from:

```text
C:\Users\carlo\Downloads\microservicios\RematePos-Backend-develop
```

The local environment also used:

```text
C:\Users\carlo\Downloads\microservicios\RematePos-db
http://localhost:8080
http://localhost:3000
```

The API Gateway used during validation was the gateway included in the backend Docker Compose stack.

## Functional Scope Preserved

This baseline includes:

- `purchase-microservice` with checkout, payment, cash payment, webhook sandbox, return support, and purchase lookup flows.
- `invoice-microservice` with invoice generation, recent invoices, invoice lookup by number, and invoice response models.
- Backend configuration for purchase and invoice services through config-server.
- Docker Compose support for the validated local backend stack.
- API Gateway source required by the validated backend Compose stack.
- Customer and product adjustments that supported the validated POS flow.
- Evidence from HU-103 local execution validation.

## Endpoints Validated Locally

The following endpoints were validated through the API Gateway:

| Method | Endpoint | Result |
| --- | --- | --- |
| GET | `/api/v1/products` | 200 |
| GET | `/api/v1/customers` | 200 |
| GET | `/api/v1/invoices/recent?limit=8` | 200 |
| GET | `/api/v1/invoices/number/INV-20260513-22` | 200 |
| GET | `/api/v1/purchases/22` | 200 |
| GET | `/api/v1/purchases/invoice/INV-20260513-22` | 200 |
| POST | `/api/v1/purchases/checkout` | 200 |
| POST | `/api/v1/purchases/22/pay` | 200 |

Validated invoice:

```text
INV-20260513-22
```

Validated purchase/payment data:

```text
purchaseId: 22
invoiceId: 21
paymentStatus: PAID
paymentMethod: CASH
```

## Not Final HU Split

This branch intentionally preserves the functional baseline as it ran locally.

After review, the implementation should be split into smaller HUs:

- HU-062: cash payment flow.
- HU-063: cash movement tracking.
- HU-064: sandbox electronic payment webhook.
- HU-065: invoice generation after approved payment.
- HU-078: returns and refunds.

The already open HU-061 PR remains separate and should not be merged automatically because this baseline has a broader scope.

## DIAN Scope

This branch does not include real DIAN integration.

DIAN, CUFE/CUDE, QR, XML, PDF, and electronic invoicing provider integration remain future work. Any references to those topics are architectural roadmap or future evolution notes, not a production-ready legal invoicing implementation.

## Sensitive and Generated Files Excluded

The baseline must not include:

- real `.env` files;
- real credentials;
- tokens;
- generated `target/` folders;
- logs;
- dumps;
- backups;
- compressed artifacts;
- API Gateway generated artifacts;
- microservice generated build outputs.

Only example environment templates are allowed.

## GitHub Traceability

PR URL:

```text
https://github.com/RematePos/RematePos-Backend/pull/15
```

Branch:

```text
feature/HU-120-CAVY-functional-backend-baseline
```

PR status:

```text
Draft, open, base develop, no merge.
```

Commits:

- `feat(HU-120): add validated purchase microservice baseline`
- `feat(HU-120): add validated invoice microservice baseline`
- `chore(HU-120): include backend compose services for validated baseline`
- `docs(HU-120): add local execution evidence and functional baseline notes`

## Validation Executed

Maven reactor validation:

```text
BUILD SUCCESS
```

Command:

```powershell
.\microservices\customer-microservice\mvnw.cmd -f microservices\pom.xml clean test
```

Reactor modules validated:

```text
7 modules, including purchase-microservice and invoice-microservice.
```

Docker Compose configuration validation:

```text
OK
```

Command:

```powershell
docker compose -p pos-dev-hu120 --env-file .\infra\docker\env\.env.dev.example -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml config --quiet
```

Security validation:

- No real `.env` files committed.
- No secrets committed.
- No target folders committed.
- No logs committed.
- No dumps, backups, zips, jars or generated artifacts committed.

## Academic Review Note

This PR is intentionally kept as Draft because it preserves a broad validated functional backend baseline. It is useful for academic and technical review, but it should not be merged automatically before the team decides whether to keep this baseline or split it into smaller user stories.

This baseline supports traceability for the evaluator because it links the working local evidence, GitHub PR, commits, validation commands, and future decomposition plan.

## Pending Risks

- The backend baseline is broader than a single small HU and should be reviewed as a preservation branch.
- The API Gateway also exists in the separate `RematePos-api` repository, so the team must later decide whether the Compose gateway or the separated repository is the definitive gateway source.
- Some configuration templates still contain local placeholder defaults and should be reviewed before any production deployment.
- The frontend functional views are still pending recovery into the official frontend repository by HU.
- Historical local changes still need decomposition into small PRs after the baseline is preserved.

## Recommended Next Steps

1. Review this PR as a functional preservation baseline.
2. Keep PR #14 for HU-061 open until the team decides the merge order.
3. Avoid merging HU-120 directly if the team wants strict HU separation first.
4. Use this branch as the reference to split HU-062, HU-063, HU-064, HU-065, and HU-078.
5. Continue excluding real secrets, generated files, logs, dumps, backups, and local environment files.
