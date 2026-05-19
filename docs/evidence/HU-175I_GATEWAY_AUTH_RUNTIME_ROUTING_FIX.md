# HU-175I - Gateway Auth Runtime Routing Fix

## Context

During the final backend + frontend smoke test, the API Gateway was healthy, but login through the gateway failed because the gateway was calling auth-microservice using the wrong internal runtime URL.

## Problem

Gateway attempted to call:

`http://auth-microservice:8080/api/v1/auth/login`

But the auth-microservice container was actually listening on:

`8096`

This caused `Connection refused` during login.

## Fix

The Docker Compose environment for API Gateway was aligned with the actual auth runtime port:

`AUTH_SERVICE_URL=http://auth-microservice:8096`

`SERVICES_AUTH_URL=http://auth-microservice:8096`

## Validation

After applying the fix:

- API Gateway started healthy.
- Auth microservice was reachable from Gateway.
- Admin login succeeded.
- Cashier login succeeded.
- Bearer token authentication worked.
- GET products without token returned 401.
- Cashier product creation returned 403.
- Cashier customer creation worked.
- Admin category creation worked.
- Admin product creation worked.
- Cashier checkout worked.
- Cash payment worked.
- Purchase final status was INVOICED.
- Payment status was APPROVED.
- Invoice generated: INV-20260519-38.
- Stock decreased from 5 to 4.
- Frontend login worked.
- Cashier UI permissions worked.
- Admin UI permissions worked.

## Notes

No secrets were added.
No `.env` files were committed.
No frontend code was modified.
No database destructive command was used.
No volumes were deleted.

## Conclusion

The final local runtime route for API Gateway to Auth is:

`http://auth-microservice:8096`

This fix is required for the full security smoke test and frontend authentication flow to work locally.
