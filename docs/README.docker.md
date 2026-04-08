# Docker Multiambiente - Guia de Arranque Rapido

Esta guia deja un flujo unico para levantar el proyecto en Docker desde Windows/PowerShell.

## Que archivos usa Docker

- Base: `infra/docker/compose/docker-compose.yml`
- Override por ambiente:
  - `infra/docker/compose/docker-compose.dev.yml`
  - `infra/docker/compose/docker-compose.qa.yml`
  - `infra/docker/compose/docker-compose.release.yml`
  - `infra/docker/compose/docker-compose.main.yml`

## Prerrequisitos

- Docker Desktop encendido.
- `docker compose` v2 disponible.
- Puertos libres para el ambiente que vas a usar.
- MongoDB y PostgreSQL levantados por separado (repositorio `RematePos-db`).
- Recomendado: levantar un ambiente a la vez (primero `dev`).

## Nota sobre base de datos externa

Este backend ya no levanta `mongodb`/`postgresql` en su `docker-compose`.
Debes iniciar primero el stack de BD externo y configurar:

- `MONGO_HOST`
- `POSTGRESQL_HOST`

## Setup inicial (solo una vez)

```powershell
git clone <REPO_URL>
Set-Location "<REPO_FOLDER>"
Copy-Item .\infra\docker\env\.env.dev.example .\infra\docker\env\.env.dev
Copy-Item .\infra\docker\env\.env.qa.example .\infra\docker\env\.env.qa
Copy-Item .\infra\docker\env\.env.release.example .\infra\docker\env\.env.release
Copy-Item .\infra\docker\env\.env.main.example .\infra\docker\env\.env.main
```

## Levantar ambiente `dev`

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml up -d --build
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml ps
```

## Validacion minima (recomendada)

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-endpoints.ps1 -Environment dev
```

Notas de validacion:

- `customer` y `product` deben responder `200`.
- `cart` puede responder `404` en este momento (esperado por el script).
- Si el script falla, revisa la seccion de troubleshooting.

## Puertos y endpoints por ambiente

| Ambiente | Config Server | Eureka | Customer API | Product API |
|---|---:|---:|---:|---:|
| `dev` | `8888` | `8761` | `8091` | `8092` |
| `qa` | `18888` | `18761` | `18091` | `18092` |
| `release` | `28888` | `28761` | `28091` | `28092` |
| `main` | `38888` | `38761` | `38091` | `38092` |

Health checks utiles:

- `http://localhost:<config-port>/actuator/health`
- `http://localhost:<eureka-port>/actuator/health`

APIs base:

- `http://localhost:<customer-port>/api/v1/customers`
- `http://localhost:<product-port>/api/v1/products`

Para pruebas en Postman usa `docs/postman/microservice-pos-4-env.postman_collection.json` y el environment del ambiente correspondiente.

## Levantar otros ambientes

`qa`:

```powershell
docker compose -p pos-qa --env-file .\infra\docker\env\.env.qa -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.qa.yml up -d --build
```

`release`:

```powershell
docker compose -p pos-release --env-file .\infra\docker\env\.env.release -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.release.yml up -d --build
```

`main` (solo cuando tengas credenciales reales):

```powershell
docker compose -p pos-main --env-file .\infra\docker\env\.env.main -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.main.yml up -d --build
```

Recomendaciones para `main`:

- Usa `MONGO_HOST` y `POSTGRESQL_HOST` con DNS/IP de infraestructura real (no `host.docker.internal`).
- Usa credenciales de secret manager.
- Mantiene puertos de BD en `27017` y `5432` salvo requisito explícito de red.

## Apagado limpio

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml down
```

Repite cambiando `pos-dev` y el archivo `.env` segun el ambiente.

## Troubleshooting rapido

### Error: `Bind for 0.0.0.0:5433 failed: port is already allocated`

Ese puerto ya lo esta usando otro proceso o contenedor de la BD externa.

1) Ver quien usa el puerto:

```powershell
Get-NetTCPConnection -LocalPort 5433 -State Listen | Select-Object LocalAddress,LocalPort,OwningProcess
```

2) Ver el proceso:

```powershell
Get-Process -Id <PID>
```

3) Liberar el puerto (si aplica):

```powershell
Stop-Process -Id <PID> -Force
```

4) Alternativa segura: cambiar `POSTGRESQL_DB_PORT` en `infra/docker/env/.env.dev` a otro puerto libre (por ejemplo `5533`) y volver a levantar.

### `docker compose ps` sale vacio

- Normalmente significa que `up` fallo antes de crear/arrancar servicios.
- Ejecuta de nuevo `up` y revisa el primer error que aparezca.

### Smoke test falla en customer/product

- Revisa logs:

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml logs --tail 100 customer-microservice
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml logs --tail 100 product-microservice
```

## Seguridad

- Versiona solo plantillas `*.example`, nunca `infra/docker/env/.env.*` reales.
- Mantiene secretos en variables de CI/CD o locales.
- En `main`, el backend se conecta a MongoDB/PostgreSQL externos definidos por `MONGO_HOST` y `POSTGRESQL_HOST`.
