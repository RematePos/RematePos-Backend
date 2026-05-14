# HU-134 - DEV CORS Origins Fix

## Purpose

This document records the API Gateway CORS fix required to complete the clean DEV environment validation for RematePOS.

## Original Error

The frontend loaded correctly on `http://localhost:3001`, but the browser showed:

```text
Failed to fetch
```

The backend endpoint itself returned HTTP 200, but the API Gateway did not include a matching CORS response header for `http://localhost:3001`.

## Root Cause

The API Gateway CORS configuration was hardcoded in:

```text
api-gateway/src/main/java/com/corhuila/gateway/ApiGatewayApplication.java
```

The previous allowed origins list included `http://localhost:3000`, but did not include additional local DEV frontend ports such as `3001` or `3002`.

As a result:

- `Origin: http://localhost:3000` received `Access-Control-Allow-Origin`.
- `Origin: http://localhost:3001` received no `Access-Control-Allow-Origin`.
- The browser blocked the response even though the API returned HTTP 200.

## Configuration Fix

The API Gateway now reads allowed origins from:

```text
CORS_ALLOWED_ORIGINS
```

The default value remains restricted to:

```text
http://localhost:3000
```

The DEV environment examples explicitly allow:

```text
http://localhost:3000,http://localhost:3001,http://localhost:3002
```

No wildcard CORS configuration is used.

## Files Changed

- `api-gateway/src/main/java/com/corhuila/gateway/ApiGatewayApplication.java`
- `infra/docker/compose/docker-compose.yml`
- `infra/docker/env/.env.dev.example`
- `infra/docker/compose/.env.example`

## DEV Allowed Origins

| Origin | Purpose |
| --- | --- |
| `http://localhost:3000` | Default local frontend port. |
| `http://localhost:3001` | Alternate local frontend port when 3000 is occupied. |
| `http://localhost:3002` | Additional local validation port. |

QA, release and main environments should set `CORS_ALLOWED_ORIGINS` to the real frontend domain for each environment. They should not use wildcard CORS.

## Commands Executed

Docker Compose configuration validation:

```powershell
$env:CORS_ALLOWED_ORIGINS='http://localhost:3000,http://localhost:3001,http://localhost:3002'
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml config --quiet
```

API Gateway rebuild:

```powershell
$env:CORS_ALLOWED_ORIGINS='http://localhost:3000,http://localhost:3001,http://localhost:3002'
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml up -d --build api-gateway
```

Frontend validation:

```powershell
set PORT=3001
npm start
```

## CORS Validation

| Origin | HTTP Result | `Access-Control-Allow-Origin` |
| --- | --- | --- |
| `http://localhost:3000` | 200 | `http://localhost:3000` |
| `http://localhost:3001` | 200 | `http://localhost:3001` |
| `http://localhost:3002` | 200 | `http://localhost:3002` |

All responses included:

```text
Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
Access-Control-Allow-Headers: Authorization,Content-Type,Accept
```

## Backend Endpoint Validation

| Endpoint | Result |
| --- | --- |
| `/api/v1/products` | 200 |
| `/api/v1/customers` | 200 |
| `/api/v1/invoices/recent?limit=8` | 200 |

## Frontend Validation

The clean frontend was started on:

```text
http://localhost:3001
```

Validated routes:

| Route | Result |
| --- | --- |
| `/billing` | 200 |
| `/billing/invoice-copy` | 200 |
| `/billing/returns` | 200 |

The invoice copy page rendered without `Failed to fetch`, and the validated invoice was visible:

```text
INV-20260513-22
```

## Security

No real `.env` files were committed.

No secrets, credentials, logs, dumps, backups, `target/` folders, jars or generated artifacts were committed.

No wildcard CORS origin was introduced.
