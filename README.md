# Microservice POS

Repositorio de microservicios con soporte multiambiente (dev, qa, release, main) y documentación centralizada en `docs/`.

## Inicio rapido recomendado (Docker)

1. Abre `docs/README.docker.md`.
2. Sigue el flujo de `dev` (setup -> up -> smoke test).
3. Cuando `dev` funcione, pasa a `qa` y `release`.

## Estructura principal

- `docs/`: documentación funcional y técnica (fuente canónica)
- `infra/docker/compose/`: `docker-compose` base y overrides por ambiente
- `infra/docker/env/`: plantillas `.env.*.example` por ambiente
- `microservices/`: servicios de negocio
- `config-server/`, `discovery-server/`: infraestructura Spring Cloud
- `scripts/`: automatizaciones de setup y verificación

## Documentación recomendada para empezar

1. `docs/README.docker.md` (arranque en Docker paso a paso)
2. `docs/postman/README.md` (coleccion + environments)
3. `docs/INDEX.md` (indice general)

## Comandos frecuentes

```bash
make help
make dev-setup
make docker-up
make docker-ps
make docker-down
```

Si no usas `make`, puedes ejecutar `docker compose` directamente con los archivos en `infra/docker/compose/`.

