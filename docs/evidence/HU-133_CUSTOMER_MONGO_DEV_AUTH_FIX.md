# HU-133 - Customer MongoDB DEV Authentication Fix

## Purpose

This document records the fix for the remaining HU-132 backend validation blocker: `customer-microservice` returned HTTP 500 in the clean DEV Docker environment due to MongoDB authentication failure.

## Original Error

The API Gateway returned:

```text
GET /api/v1/customers -> 500
```

The customer service log showed:

```text
UncategorizedMongoDbException: Exception authenticating MongoCredential{mechanism=SCRAM-SHA-1, userName='admin', source='admin', password=<hidden>}
```

No password value was exposed in this evidence.

## Root Cause

The MongoDB DEV database was healthy and the Mongo migration job finished successfully, but `customer-microservice` was configured to reach MongoDB through `host.docker.internal`.

Validation showed that:

- MongoDB authenticated correctly from the database Docker network using the service name `mongodb`.
- `mongo-migrate` also succeeded using `mongodb` as the Mongo host.
- `customer-microservice` failed when using `host.docker.internal`.

The backend Docker stack and the database Docker stack run in different Compose projects. For MongoDB access in DEV, the backend services that use MongoDB must be attached to the database Compose network and use the Mongo service name.

## Files Reviewed

- `config-server/src/main/resources/config/customer-microservice.yml`
- `microservices/customer-microservice/src/main/resources/application.yml`
- `infra/docker/compose/docker-compose.yml`
- `infra/docker/compose/docker-compose.dev.yml`
- `infra/docker/env/.env.dev.example`
- `infra/docker/env/.env.example`
- `infra/docker/compose/.env.example`
- `RematePos-bd/docker-compose/docker-compose.yml`
- `RematePos-bd/docker-compose/.env.example`
- `RematePos-bd/mongo/run-migrations.sh`

## Files Changed

- `infra/docker/compose/docker-compose.yml`
- `infra/docker/env/.env.dev.example`
- `infra/docker/env/.env.example`
- `infra/docker/compose/.env.example`

## Configuration Fix

The backend Docker Compose configuration now:

- Uses `mongodb` as the default Mongo host in DEV templates.
- Adds an external database network reference named `database`.
- Connects `customer-microservice` and `cart-microservice` to both:
  - the backend `microservices` network;
  - the database network `pos-db-dev_default`.

This allows Mongo-based backend services to resolve the database container by service name while preserving the internal backend service network used by the API Gateway and Eureka.

## Commands Executed

Docker Compose configuration validation:

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml config --quiet
```

Backend restart and rebuild:

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml down
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml up -d --build
```

Maven validation:

```powershell
.\microservices\customer-microservice\mvnw.cmd -f microservices\pom.xml -pl customer-microservice -am test
```

## Docker Result

The backend stack started successfully:

- `api-gateway`: Up / healthy.
- `config-server`: Up / healthy.
- `discovery-server`: Up / healthy.
- `customer-microservice`: Up.
- `product-microservice`: Up.
- `cart-microservice`: Up.
- `purchase-microservice`: Up.
- `invoice-microservice`: Up.

## Endpoints Validated

| Method | Endpoint | Result |
| --- | --- | --- |
| GET | `/actuator/health` | 200 |
| GET | `/api/v1/products` | 200 |
| GET | `/api/v1/customers` | 200 |
| GET | `/api/v1/invoices/recent?limit=8` | 200 |
| GET | `/api/v1/invoices/number/INV-20260513-22` | 200 |
| GET | `/api/v1/purchases/invoice/INV-20260513-22` | 200 |

## Maven Result

The Maven reactor validation for `customer-microservice` and its required module passed:

```text
BUILD SUCCESS
```

Validated modules:

- `microservices`
- `common-exceptions`
- `customer-microservice`

## Security

No real `.env` files were committed.

No secrets, passwords, logs, dumps, backups, `target/` folders, jars or generated artifacts were committed.

## Pending Work

HU-132 should be resumed after HU-133 is merged to validate the complete clean DEV environment including frontend routes.
