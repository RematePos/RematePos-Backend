# RematePos-db

Repositorio de versionamiento de base de datos para RematePos usando Liquibase + PostgreSQL.

## Objetivo

- Centralizar migraciones de esquema y datos.
- Ejecutar despliegues de BD por ambiente (`dev`, `qa`, `release`, `main`).
- Evitar cambios manuales no auditados en producción.

## Estructura principal

- `src/main/resources/db/changelog/00_extensions`
- `src/main/resources/db/changelog/01_tables`
- `src/main/resources/db/changelog/02_views`
- `src/main/resources/db/changelog/03_functions`
- `src/main/resources/db/changelog/04_procedures`
- `src/main/resources/db/changelog/05_triggers`
- `src/main/resources/db/changelog/06_indexes`
- `src/main/resources/db/changelog/07_materialized_views`
- `src/main/resources/db/changelog/08_types`
- `src/main/resources/db/changelog/09_inserts`
- `src/main/resources/db/changelog/10_updates`
- `src/main/resources/db/changelog/11_schema`
- `src/main/resources/db/changelog/12_rollbacks`

El punto de entrada de migraciones es `src/main/resources/db/changelog/db.changelog-master.yaml`.

## Requisitos

- Java 17+
- Maven 3.8+
- Docker + Docker Compose (opcional para entorno local)

## Configuracion segura

1. Copia `.env.example` a `.env`.
2. Cambia todos los placeholders por valores reales locales.
3. No subas `.env` ni secretos al repositorio.

```powershell
Copy-Item .env.example .env -Force
```

## Ejecucion local con Maven

```powershell
mvn liquibase:validate
mvn liquibase:update
mvn liquibase:status
```

## Ejecucion con Docker Compose

```powershell
docker compose --env-file .env -f docker-compose.migration.yml up -d
docker compose --env-file .env -f docker-compose.migration.yml ps
docker compose --env-file .env -f docker-compose.migration.yml logs --tail 120 liquibase
```

## Despliegue por ambiente

```powershell
mvn -P dev liquibase:update
mvn -P qa liquibase:update
mvn -P release liquibase:update
mvn -P main liquibase:update
```

## Reglas de despliegue

- Todo changeset debe incluir `rollback`.
- No modificar changesets ya ejecutados.
- Usar `spring.jpa.hibernate.ddl-auto=validate` en microservicios.
- Ejecutar `mvn liquibase:validate` antes de cada release.

## Integracion con backend

- Referencia operativa: `MICROSERVICES_INTEGRATION.md`
- Checklist de salida: `DEPLOYMENT_CHECKLIST.md`

## Troubleshooting rapido

- `databasechangeloglock`: verificar lock activo antes de relanzar.
- `password authentication failed`: revisar variables de entorno cargadas.
- `relation does not exist`: validar orden en `db.changelog-master.yaml`.
