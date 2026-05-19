# Local Deployment And Smoke Test

## Requirements

- Docker
- Docker Compose
- Java
- Node.js
- npm

## Backend

Repository:

`C:\Users\carlo\Downloads\microservicios\RematePos-Backend-HU-175A-auth-clean`

Final backend branch:

`feature/HU-175I-CAVY-gateway-auth-runtime-routing-fix`

Start local stack:

```powershell
docker compose --env-file infra\docker\env\.env.dev -f infra\docker\compose\docker-compose.yml -f infra\docker\compose\docker-compose.dev.yml up -d --build config-server discovery-server api-gateway auth-microservice customer-microservice product-microservice purchase-microservice invoice-microservice cart-microservice
```

Check services:

```powershell
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String "auth|gateway|customer|product|purchase|invoice|cart|config|discovery|postgres|mongo"
```

Gateway health:

```powershell
Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:8080/actuator/health"
```

Expected result: `200 OK` and `UP`.

## Frontend

Repository:

`C:\Users\carlo\Downloads\microservicios\RematePos-Frontend-HU-175G-clean`

Frontend branch:

`feature/HU-175G-AFAF-frontend-auth-rbac-integration`

Build and run:

```powershell
npm install
npm run build
$env:PORT=3002
npm start
```

Frontend URL:

`http://localhost:3002`

If the port is occupied, use another free port and open that exact URL.

## Demo Credentials

Demo usernames:

- `admin.demo`
- `cashier.demo`

Passwords are stored in the ignored local file:

`infra/docker/env/.env.dev`

Do not print or commit those values.

## Smoke Tests

### Authentication

- Login as admin.
- Login as cashier.
- Confirm no `passwordHash` is returned.
- Confirm frontend sends `Authorization: Bearer <token>`.
- Confirm frontend does not send internal Gateway headers.

### Security

- `GET /api/v1/products` without token returns `401`.
- Cashier can list products when authorized.
- Cashier cannot create products and receives `403`.
- Cashier can create/search customers.
- Admin can create categories and products.

### POS Flow

- Create or select category.
- Create or select product with stock.
- Create or select customer.
- Execute checkout.
- Confirm cash payment.
- Confirm purchase status is `INVOICED`.
- Confirm payment status is `APPROVED`.
- Confirm invoice is generated.
- Confirm stock decreases.

## Known Local Issues

- Port `3001` may be occupied by an old dev server.
- Port `3000` may be occupied by another local process or Docker-related workflow.
- Gateway must route auth calls to `http://auth-microservice:8096` in the current local Docker runtime.
- If local PostgreSQL is missing product/category tenant columns and Liquibase did not apply, smoke-only fallback may be:

```sql
ALTER TABLE products ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(255);
ALTER TABLE categories ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(255);
```

This fallback is local, non-destructive, and must not be committed as a replacement for the real Liquibase changelog.
