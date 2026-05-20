# HU-179C – Analytics Gateway and Docker Integration

## Objetivo
Conectar RematePos-Analytics al API Gateway y al entorno Docker local para que el frontend pueda consumir `/api/v1/analytics/**` sin recibir 404.

## Contexto funcional
El módulo de Analytics no se limita a ejecutar consultas SQL. Se diseñó como un componente de analítica inteligente para apoyar decisiones del dueño del negocio. A partir de datos operativos como ventas, productos, facturas y stock, calcula indicadores descriptivos, predice riesgo de agotamiento y genera recomendaciones accionables, como reponer productos críticos, proteger productos estrella o revisar productos con baja rotación.

Actualmente no se usa machine learning pesado. La versión actual se apoya en reglas de negocio explicables, lo cual es adecuado para una primera entrega porque permite entender por qué el sistema recomienda una acción. En una evolución futura, estas reglas pueden complementarse con modelos de forecasting o detección de anomalías.

## Cambios
- Servicio Analytics en Docker.
- Variables example.
- Ruta Gateway `/api/v1/analytics/**`.
- Validación de seguridad multi-tenant.

## Validaciones
- Analytics health directo.
- Analytics health vía Gateway.
- Dashboard vía Gateway.
- Owner/admin accede.
- Cashier bloqueado.
- Platform admin sin tenant bloqueado.
- Frontend `/analytics` ya no recibe 404.

## Validacion runtime - 2026-05-20

Comandos ejecutados sin borrar volumenes y sin usar `git clean` ni `reset --hard`.

### Compose

- `docker compose --env-file infra\docker\env\.env.dev -f infra\docker\compose\docker-compose.yml -f infra\docker\compose\docker-compose.dev.yml config --services`
- Resultado: `analytics-service` aparece en la lista de servicios.
- `docker compose ... config | Select-String -Pattern "analytics-service|SERVICES_ANALYTICS_URL|ANALYTICS_MODE|ANALYTICS_DATABASE_URL"`
- Resultado: compose no reporta error YAML. Se observa `ANALYTICS_MODE=DEMO` y `SERVICES_ANALYTICS_URL=http://analytics-service:8090`.
- Observacion: existe una linea heredada en `customer-microservice` que aparece como `dockerfile: microservices/customer-microservice/Dockerfile - SERVICES_ANALYTICS_URL=http://analytics-service:8090`. No se modifico en esta validacion.

### Analytics service

- `docker compose ... up -d --build analytics-service`
- Resultado: el build no finalizo dentro de 5 minutos.
- `docker compose ... build --progress plain analytics-service`
- Resultado: el build no finalizo dentro de 10 minutos.
- `docker ps -a --filter "name=analytics-service"`
- Resultado: no se creo contenedor `analytics-service`.
- `docker logs analytics-service --tail 100`
- Resultado: no existe contenedor con ese nombre.

### Smoke directo Analytics

- `GET http://localhost:8090/api/v1/analytics/health`
- Resultado: no hay conexion porque `analytics-service` no quedo corriendo.
- `GET http://localhost:8090/api/v1/analytics/dashboard` con `X-Tenant-Id=demo-tenant`
- Resultado: no ejecutado contra contenedor porque el servicio no quedo corriendo.

### Gateway Analytics

- `docker compose ... up -d --build api-gateway`
- Resultado: el rebuild no finalizo dentro de 5 minutos. El contenedor previo `compose-api-gateway-1` seguia corriendo y healthy.
- `GET http://localhost:8080/api/v1/analytics/health`
- Resultado: `401 Unauthorized`; no es `404`.
- `GET http://localhost:8080/api/v1/analytics/dashboard` sin token
- Resultado: `401 Unauthorized`; no es `404`.

### Login y roles

Se intento login via Gateway para `admin.demo`, `cashier.demo` y `platform.admin`, guardando tokens solo en variable local y sin imprimirlos.

- `admin.demo`: login via Gateway agoto timeout; dashboard no ejecutado.
- `cashier.demo`: login via Gateway agoto timeout; dashboard no ejecutado.
- `platform.admin`: login via Gateway agoto timeout; dashboard no ejecutado.

Logs de `auth-microservice` muestran que el timeout se debe a fallo de conexion JPA/Hikari contra la base de datos (`Connection is not available`, `Could not open JPA EntityManager for transaction`). No se imprimieron tokens ni secretos.

### Alcance

HU-180A no fue tocada durante esta validacion runtime. Solo se actualizo este archivo de evidencia HU-179C.

## Seguridad
- Frontend no envía `X-Tenant-Id`.
- Gateway propaga tenant context de forma controlada.
- No secretos.
- No `.env` reales.

## Riesgos/deudas
- `ANALYTICS_DATABASE_URL` vacío en demo.
- Consultas SQL reales pendientes de validar contra schema final.
- Datos reales read-only pendientes.
- Monitoreo/logs producción pendientes.
