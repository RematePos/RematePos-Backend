# HU-131 - Backend Docker Maven Build Fix

## Purpose

This document records the HU-131 fix and validation for building the RematePOS backend from a clean `develop` checkout.

The goal was to make the Docker/Maven build reproducible without removing backend functionality, business logic, purchase flows, invoice flows or the Compose API Gateway.

## Original Error

The clean Docker build failed while building `customer-microservice`.

The root Maven reactor declared the current backend modules, including:

- `customer-microservice`
- `common-exceptions`
- `product-microservice`
- `cart-microservice`
- `purchase-microservice`
- `invoice-microservice`

However, `microservices/customer-microservice/Dockerfile` copied only part of the module POM tree before running Maven.

Maven loads all modules declared by the parent reactor before applying `-pl`, so missing module POMs caused the Docker build to fail even though the selected module was `customer-microservice`.

## Root Cause

The HU-120 functional backend baseline added the validated purchase and invoice modules, but the optimized `customer-microservice` Docker build was not updated to copy the new module POMs.

A second clean-clone reproducibility issue was also found in the backend Docker environment templates:

- Backend DEV examples did not match the database DEV example values introduced by HU-130.
- This could cause MongoDB or PostgreSQL authentication mismatches for teammates starting the backend against the database Compose stack.

Only Docker/Maven and safe local-development configuration templates were changed.

## Files Corrected

- `microservices/customer-microservice/Dockerfile`
- `infra/docker/env/.env.dev.example`
- `infra/docker/compose/.env.example`
- `infra/docker/compose/docker-compose.yml`

## Fix Applied

The customer Dockerfile now copies the `purchase-microservice` and `invoice-microservice` POMs before Maven evaluates the reactor.

The backend Docker environment examples and Compose fallback values were aligned with the database DEV example values from HU-130.

No Java controllers, services, repositories, DTOs, entities, migrations or business logic were changed.

## Maven Validation

Command executed:

```powershell
.\microservices\customer-microservice\mvnw.cmd -f microservices\pom.xml clean test
```

Result:

- `BUILD SUCCESS`
- Reactor modules validated: 7
- Validated modules included `purchase-microservice` and `invoice-microservice`.

## Docker Validation

Compose configuration validation:

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml config --quiet
```

Result:

- OK

Docker build and startup validation:

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml up -d --build
```

Result:

- All backend images built successfully.
- `discovery-server` started.
- `config-server` started and became healthy.
- `api-gateway` started and became healthy.
- `customer-microservice` started.
- `product-microservice` started.
- `cart-microservice` started.
- `purchase-microservice` started.
- `invoice-microservice` started.

Additional clean-template validation was executed using:

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev.example -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml config --quiet
```

Result:

- OK

## Endpoint Validation

The following endpoints were tested through the Compose API Gateway after the backend stack was rebuilt:

| Endpoint | Result |
| --- | --- |
| `GET /actuator/health` | 200 |
| `GET /api/v1/products` | 200 |
| `GET /api/v1/customers` | 500 in the current local Docker volume state |
| `GET /api/v1/invoices/recent?limit=8` | 200 |
| `GET /api/v1/invoices/number/INV-20260513-22` | 200 |
| `GET /api/v1/purchases/invoice/INV-20260513-22` | 200 |

The `customers` endpoint failure was traced to MongoDB authentication against an already existing local Mongo volume. The backend and database `.env.dev` files had matching values, but the existing Mongo volume appeared to have been initialized previously with different credentials.

No Docker volumes were deleted during HU-131.

HU-132 should validate the complete clean environment again with a fresh database state or an explicitly approved volume reset.

## Security Notes

- No real `.env` files were committed.
- No secrets were committed.
- No target folders were committed.
- No logs were committed.
- No dumps or backups were committed.
- No generated JARs or build artifacts were committed.
- Only safe example values and Compose fallbacks were updated.

## Pending Risks

- `GET /api/v1/customers` still needs validation in a fresh MongoDB DEV volume during HU-132.
- Existing developer machines may need to recreate only their local MongoDB DEV volume if it was initialized with older credentials.
- HU-132 should validate BD + backend + frontend together from clean `develop` after HU-130 and HU-131 are merged.
