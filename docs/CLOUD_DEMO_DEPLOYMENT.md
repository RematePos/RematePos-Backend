# Cloud Demo Deployment

This document prepares a minimum public RematePOS demo with Vercel for the frontend, Render for backend services, and Render PostgreSQL. It intentionally avoids real secrets, local `.env` files, and production credentials.

## 1. Target

The goal is an academic demo URL that can:

- open the frontend from Vercel;
- log in with demo users;
- show the application menu;
- exercise products and billing demo flows when their services are healthy;
- leave sales/purchase flow documented with its current dependency risk.

## 2. Minimum Services

| Service | Render type | Port | Dockerfile | Required for |
|---|---:|---:|---|---|
| PostgreSQL | Managed database | 5432 | N/A | Auth, product, purchase, invoice persistence |
| config-server | Web service | 8888 or `PORT` | `config-server/Dockerfile` | Centralized config |
| discovery-server | Web service | 8761 or `PORT` | `discovery-server/Dockerfile` | Eureka registration |
| auth-microservice | Web service | 8096 or `PORT` | `microservices/auth-microservice/Dockerfile` | Login and demo users |
| product-microservice | Web service | 8092 or `PORT` | `microservices/product-microservice/Dockerfile` | Products and categories |
| invoice-microservice | Web service | 8095 or `PORT` | `microservices/invoice-microservice/Dockerfile` | MOCK_DIAN demo billing |
| purchase-microservice | Web service | 8094 or `PORT` | `microservices/purchase-microservice/Dockerfile` | Sales/purchases |
| api-gateway | Web service | 8080 or `PORT` | `api-gateway/Dockerfile` | Public backend URL |

Important sales note: `purchase-microservice` calls `customer-microservice` through `CUSTOMER_SERVICE_URL`. A complete sales checkout needs customer service and MongoDB, or an existing reachable customer service. Without that, login/product/invoice smoke can still be demonstrated, but sales may fail at customer lookup.

## 3. Render Strategy

Use Render dashboard for the fastest path:

1. Create a Render PostgreSQL database.
2. Create each backend service as Docker web service from the backend repository branch.
3. Set `SPRING_PROFILES_ACTIVE=cloud` on every Spring service.
4. Keep only `rematepos-api-gateway` as the URL used by the frontend.
5. Configure Vercel to call the public Render API Gateway URL.

The included `render.yaml` is a blueprint draft. It uses `sync: false` for secrets and URLs that must be filled in Render after services exist.

## 4. Service Order

1. PostgreSQL.
2. `rematepos-config-server`.
3. `rematepos-discovery-server`.
4. `rematepos-auth`.
5. `rematepos-product`.
6. `rematepos-invoice`.
7. `rematepos-purchase`.
8. `rematepos-api-gateway`.
9. Frontend in Vercel.

If time is short, deploy through API Gateway with auth and product first. Add purchase only if customer dependency is resolved.

## 5. Shared Variables

Set these without committing values:

| Variable | Where | Notes |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | all Spring services | `cloud` |
| `CONFIG_SERVER_URL` | all except config-server | Render URL for config-server |
| `SPRING_CONFIG_IMPORT` | all except config-server | `configserver:` URL built from config-server URL |
| `EUREKA_SERVER_URL` | all except config-server | Discovery URL ending in `/eureka/` |
| `POSTGRESQL_HOST` | auth/product/purchase/invoice | Render PostgreSQL host |
| `POSTGRESQL_PORT` | auth/product/purchase/invoice | Render PostgreSQL port |
| `POSTGRESQL_DB_USERNAME` | auth/product/purchase/invoice | Render PostgreSQL user |
| `POSTGRESQL_DB_PASSWORD` | auth/product/purchase/invoice | Render PostgreSQL password |
| `JWT_SECRET` | auth and api-gateway | Same value in both services |
| `INTERNAL_SERVICE_TOKEN` | product/purchase/invoice | Same value in internal services |
| `CORS_ALLOWED_ORIGINS` | api-gateway | Vercel URL plus local URLs |
| `BILLING_PROVIDER` | invoice | Use `MOCK_DIAN` for stable demo |
| `BILLING_PROVIDER_BASE_URL` | invoice | Empty for MOCK_DIAN |
| `BILLING_SETTINGS_ENCRYPTION_KEY` | invoice | Required if tenant billing settings are used |

## 6. Service URL Variables

After Render creates the services, set these:

| Variable | Service | Value format |
|---|---|---|
| `SERVICES_AUTH_URL` | api-gateway | Public or internal auth URL |
| `PRODUCT_SERVICE_URL` | api-gateway, purchase | Public or internal product URL |
| `PURCHASE_SERVICE_URL` | api-gateway | Public or internal purchase URL |
| `INVOICE_SERVICE_URL` | api-gateway, purchase | Public or internal invoice URL |
| `CUSTOMER_SERVICE_URL` | api-gateway, purchase | Required for full sales flow |

## 7. Demo Users

Set these only in Render environment variables:

| Variable | Purpose |
|---|---|
| `AUTH_SEED_DEMO_USERS` | Create tenant demo users |
| `AUTH_DEMO_ADMIN_PASSWORD` | Password for `admin.demo` |
| `AUTH_DEMO_CASHIER_PASSWORD` | Password for `cashier.demo` |
| `AUTH_SEED_PLATFORM_ADMIN` | Create platform admin |
| `AUTH_PLATFORM_ADMIN_USERNAME` | Platform admin username |
| `AUTH_PLATFORM_ADMIN_PASSWORD` | Platform admin password |

Do not place demo passwords in source files. Document the final demo credentials in the delivery document after they are configured.

## 8. Frontend in Vercel

In the frontend project, configure:

```txt
REACT_APP_API_GATEWAY_URL=https://URL_PUBLICA_API_GATEWAY_RENDER
REACT_APP_API_URL=https://URL_PUBLICA_API_GATEWAY_RENDER
REACT_APP_DEMO=false
```

Then redeploy in Vercel. The backend `CORS_ALLOWED_ORIGINS` must include:

```txt
http://localhost:3000,http://localhost:3001,http://localhost:3002,https://URL_PUBLICA_FRONTEND_VERCEL
```

## 9. Smoke Test

1. Open `https://URL_PUBLICA_API_GATEWAY_RENDER/actuator/health`.
2. Open the Vercel frontend URL.
3. Log in with `platform.admin` or `admin.demo`.
4. Open menu and tenant/business views.
5. Open products/categories.
6. Create or list products.
7. Try invoice MOCK_DIAN flow if invoice is healthy.
8. Try purchase flow only after `CUSTOMER_SERVICE_URL` points to a working customer service.

## 10. Limitations

- Render free instances can sleep and cold-start slowly.
- Eureka can be fragile with public cloud URLs; direct service URL variables are the safer fallback.
- Full purchase/sales flow depends on customer service and MongoDB.
- DIAN/Alanube real acceptance is not part of the minimum cloud demo.
- Payment providers are outside this deployment.
- No local `.env` files are needed or allowed.

## 11. If A Service Falls

| Failure | Immediate action |
|---|---|
| Config server down | Restart config-server, then restart dependent services |
| Discovery down | Keep direct service URL variables configured and restart gateway |
| Auth down | Check PostgreSQL variables and `JWT_SECRET` |
| Gateway returns CORS error | Update `CORS_ALLOWED_ORIGINS` with the exact Vercel URL |
| Product/invoice DB error | Check database name and Liquibase logs |
| Purchase fails customer lookup | Deploy customer service with MongoDB or skip sales in demo |

## 12. Final Recommendation

For academic delivery, the safest minimum is:

- Vercel frontend;
- Render API Gateway;
- Render auth/product/invoice behind gateway;
- MOCK_DIAN billing;
- documented limitation for purchases until customer service is also cloud-hosted.

Keep the ZIP and final docs as the official fallback if the cloud deployment exceeds the available time.

