# Customer Cloud Deployment

## Free Infrastructure

- Render web service: `rematepos-customer`, Free plan.
- Public hostname: `https://tematepos-customer.onrender.com` (the initial hostname contains `tematepos`).
- Atlas project: `RematePOS-Cloud-Demo`.
- Atlas cluster: `RematePOS-Customer`, Free/M0, 512 MB, AWS N. Virginia.
- Database: `customer_db`.

Free plans have quotas, sleep/pause behavior, and no always-on service guarantee. Do not enable paid backups, Flex, Dedicated, or paid network features. PostgreSQL services use Neon Free, not the time-limited Render Free PostgreSQL product. Existing historical data is not restored by provisioning a new database.

## Database Access

Create a dedicated application database user with `readWrite` on `customer_db` only. Restrict the user to `RematePOS-Customer`; do not grant `atlasAdmin` or `readWriteAnyDatabase`. Store its generated password only in Render secrets, not in documentation, Git, local environment files, or logs.

In Atlas Network Access, allow the customer service's current Render outbound CIDRs. Read them from Render > rematepos-customer > Connect > Outbound or `GET /v1/services/{serviceId}/outbound-ips`. The October 2026 service reports `74.220.48.0/24` and `74.220.56.0/24`; verify the live values before reusing them. These are shared Render ranges. Do not open `0.0.0.0/0` for the permanent setup.

## Render Variables

```text
SPRING_PROFILES_ACTIVE=cloud
CUSTOMER_DB_NAME=customer_db
CORS_ALLOWED_ORIGINS=https://rematepos-frontend.onrender.com
CONFIG_SERVER_URL=https://rematepos-config-server.onrender.com
SPRING_CONFIG_IMPORT=optional:configserver:https://rematepos-config-server.onrender.com
SPRING_CLOUD_CONFIG_FAIL_FAST=false
EUREKA_CLIENT_ENABLED=false
SPRING_CLOUD_DISCOVERY_ENABLED=false
SPRING_CLOUD_SERVICE_REGISTRY_AUTO_REGISTRATION_ENABLED=false
SPRING_DATA_MONGODB_URI=<private Atlas connection URI>
SPRING_MONGODB_URI=<same private Atlas connection URI, optional alias>
```

`SPRING_DATA_MONGODB_URI` takes precedence. `SPRING_MONGODB_URI` is a compatibility fallback. The application aligns both Spring Boot Mongo property namespaces to the chosen value. If both variables are configured, use exactly the same secret. A missing URI fails early in the cloud profile without printing credentials. The active profile must be exactly `cloud`, never a connection URI.

Use Atlas's driver connection string with percent-encoded credentials, `customer_db` as the database, and `authSource=admin`. Keep TLS and certificate validation enabled. Do not substitute a local or Docker-only hostname.

Update Render variables individually or preserve the full existing set. Save changes before triggering a single deploy of the intended cloud feature-branch commit.

## Routing and Validation

Gateway and purchase require:

```text
CUSTOMER_SERVICE_URL=https://tematepos-customer.onrender.com
```

- Render health check path: `/actuator/health`.
- Customer collection: `/api/v1/customers` (`GET`, `POST`).
- Document lookup: `/api/v1/customers/document?type=CC&number=<demo-document>`.
- Gateway collection: `https://rematepos-api-gateway.onrender.com/api/v1/customers`.

Customer endpoints require the existing authenticated user and tenant context. Use the gateway for browser calls. Purchase forwards internal user/tenant headers; do not weaken authentication to make direct checks pass.

The current cloud health configuration does not check Mongo connectivity. An `UP` response alone does not prove database readiness. Verify create/list/document lookup through the authenticated gateway, then checkout, payment, invoice, stock, and analytics. Keep `BILLING_PROVIDER=MOCK_DIAN`; never enable a real billing provider for these demo tests.

## References

- [Atlas Free cluster](https://www.mongodb.com/docs/atlas/tutorial/deploy-free-tier-cluster/)
- [Render outbound IPs](https://render.com/docs/outbound-ip-addresses)
- [Neon Free plan](https://neon.com/pricing)
