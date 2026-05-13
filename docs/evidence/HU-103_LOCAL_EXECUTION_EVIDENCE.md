# Evidencia de ejecucion local - HU-103

## 1. Informacion general

- Historia de usuario: HU-103 - Preparar y validar entorno local de ejecucion completa.
- Fecha y hora de validacion: 2026-05-12 21:25:24 -05:00.
- Objetivo: dejar evidencia tecnica del estado funcional local completo de RematePOS.
- Alcance: validacion local sin cambios de codigo, sin commits, sin push y sin merge.

## 2. Rutas usadas

| Componente | Ruta local / URL |
| --- | --- |
| Base de datos | `C:\Users\carlo\Downloads\microservicios\RematePos-db` |
| Backend funcional completo | `C:\Users\carlo\Downloads\microservicios\RematePos-Backend-develop` |
| Frontend funcional completo | `C:\Users\carlo\OneDrive - corhuila.edu.co\CAV\OneDrive - corhuila.edu.co\Descargas\front-rematepos\RematePos-Frontend` |
| API Gateway activo | `http://localhost:8080` |
| Frontend activo | `http://localhost:3000` |

## 3. Estado de base de datos

Comando ejecutado:

```powershell
docker compose -p pos-db-dev --env-file .\docker-compose\.env.dev -f .\docker-compose\docker-compose.yml ps
```

Resultado:

```text
NAME                   IMAGE                SERVICE      STATUS                 PORTS
rematepos-mongodb      mongo:7              mongodb      Up 2 hours             0.0.0.0:27017->27017/tcp
rematepos-postgresql   postgres:15-alpine   postgresql   Up 2 hours (healthy)   0.0.0.0:5433->5432/tcp
```

Notas:

- PostgreSQL DEV esta arriba y saludable.
- MongoDB DEV esta arriba.
- Liquibase y migraciones Mongo fueron ejecutadas previamente con salida exitosa.

## 4. Estado de backend

Comando ejecutado:

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml ps -a
```

Resultado:

```text
NAME                              SERVICE                 STATUS                 PORTS
pos-dev-api-gateway-1             api-gateway             Up 2 hours (healthy)   0.0.0.0:8080->8080/tcp
pos-dev-cart-microservice-1       cart-microservice       Up 2 hours             0.0.0.0:8093->8093/tcp
pos-dev-config-server-1           config-server           Up 2 hours (healthy)   0.0.0.0:8888->8888/tcp
pos-dev-customer-microservice-1   customer-microservice   Up 2 hours             0.0.0.0:8091->8091/tcp
pos-dev-discovery-server-1        discovery-server        Up 2 hours (healthy)   0.0.0.0:8761->8761/tcp
pos-dev-invoice-microservice-1    invoice-microservice    Up About an hour       0.0.0.0:8095->8095/tcp
pos-dev-product-microservice-1    product-microservice    Up 2 hours             0.0.0.0:8092->8092/tcp
pos-dev-purchase-microservice-1   purchase-microservice   Up 6 minutes           0.0.0.0:8094->8094/tcp
```

Servicios validados:

| Servicio | Puerto | Estado |
| --- | ---: | --- |
| discovery-server | 8761 | Up healthy |
| config-server | 8888 | Up healthy |
| api-gateway | 8080 | Up healthy |
| customer-microservice | 8091 | Up |
| product-microservice | 8092 | Up |
| cart-microservice | 8093 | Up |
| purchase-microservice | 8094 | Up |
| invoice-microservice | 8095 | Up |

## 5. Estado de API Gateway

- Gateway activo: `http://localhost:8080`.
- Gateway usado en esta validacion: API Gateway del compose backend.
- Estado: activo y saludable.
- Observacion: el repo separado `RematePos-api` existe, pero en esta ejecucion local el puerto 8080 lo esta sirviendo el gateway incluido en el compose backend.

## 6. Estado de frontend

Contenedor detectado:

```text
NAMES                            IMAGE                          PORTS                                     STATUS
rematepos-front-frontend-dev-1   rematepos-front-frontend-dev   0.0.0.0:3000->80/tcp, [::]:3000->80/tcp   Up 2 hours
```

Rutas frontend validadas:

| Ruta | Codigo HTTP | Resultado |
| --- | ---: | --- |
| `http://localhost:3000` | 200 | Carga SPA React |
| `http://localhost:3000/billing` | 200 | Ruta servida por React |
| `http://localhost:3000/billing/invoice-copy` | 200 | Ruta servida por React |
| `http://localhost:3000/billing/returns` | 200 | Ruta servida por React |

Observacion:

- El frontend funcional completo esta en la carpeta antigua de OneDrive.
- El contenedor actual fue construido desde esa carpeta.
- El repo oficial frontend en `Downloads\microservicios` todavia debe recuperar esas vistas por HU.

## 7. Endpoints probados por API Gateway

| Metodo | Endpoint | Codigo HTTP | Resultado |
| --- | --- | ---: | --- |
| GET | `http://localhost:8080/api/v1/products` | 200 | Productos disponibles |
| GET | `http://localhost:8080/api/v1/customers` | 200 | Clientes disponibles |
| GET | `http://localhost:8080/api/v1/invoices/recent?limit=8` | 200 | Facturas recientes disponibles |
| GET | `http://localhost:8080/api/v1/invoices/number/INV-20260513-22` | 200 | Factura encontrada |
| GET | `http://localhost:8080/api/v1/purchases/22` | 200 | Compra encontrada |
| GET | `http://localhost:8080/api/v1/purchases/invoice/INV-20260513-22` | 200 | Compra encontrada por numero de factura |

## 8. Flujo checkout y pago efectivo

Flujo validado durante la ejecucion local:

| Paso | Endpoint | Resultado |
| --- | --- | --- |
| Crear compra | `POST /api/v1/purchases/checkout` | 200 |
| Registrar pago efectivo | `POST /api/v1/purchases/22/pay` | 200 |
| Consultar compra pagada | `GET /api/v1/purchases/22` | 200 |
| Consultar factura generada | `GET /api/v1/invoices/number/INV-20260513-22` | 200 |
| Consultar compra por factura | `GET /api/v1/purchases/invoice/INV-20260513-22` | 200 |

Factura generada:

```text
INV-20260513-22
```

Datos relevantes de la compra:

```text
purchaseId: 22
invoiceId: 21
invoiceNumber: INV-20260513-22
paymentStatus: PAID
paymentMethod: CASH
providerStatus: CONFIRMED_BY_CASHIER
total: 5950.00
```

## 9. Observaciones importantes

- El sistema funcional completo esta corriendo localmente.
- El backend funcional completo esta en una carpeta local sucia: `C:\Users\carlo\Downloads\microservicios\RematePos-Backend-develop`.
- `origin/develop` todavia no contiene todo el flujo funcional completo de pagos, caja, facturacion e invoice.
- El frontend funcional completo esta en la carpeta antigua de OneDrive.
- El repo oficial frontend aun debe recuperar las vistas funcionales por HU.
- El API Gateway usado en esta ejecucion es el gateway incluido en el compose backend.
- El repo separado `RematePos-api` debe revisarse despues para decidir si sera el gateway definitivo.
- PR #14 de HU-061 sigue abierto y no fue mergeado.
- La rama `feature/HU-061-CAVY-payment-model` no fue modificada en esta validacion.
- No se hicieron commits, push, merge, `git clean`, `reset --hard`, ni cambios de codigo.

## 10. Proximos pasos recomendados

1. Congelar el estado funcional local como referencia antes de seguir separando HUs.
2. Subir backend por historias de usuario pequenas y revisables.
3. Recuperar el frontend funcional desde OneDrive hacia el repo oficial por HU.
4. Decidir si el gateway final sera el incluido en compose backend o el repo separado `RematePos-api`.
5. Mantener PR #14 abierto hasta que el equipo valide el orden correcto de merge.
6. Evitar subir carpetas generadas, `.env` reales, logs, `target/`, dumps, backups o archivos pesados.

