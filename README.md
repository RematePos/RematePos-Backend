# Microservice POS

Repositorio de microservicios con soporte multiambiente (dev, qa, release, main) y documentación centralizada en `docs/`.

## Estructura principal

- `docs/`: documentación funcional y técnica (fuente canónica)
- `infra/docker/compose/`: `docker-compose` base y overrides por ambiente
- `infra/docker/env/`: plantillas `.env.*.example` por ambiente
- `microservices/`: servicios de negocio
- `config-server/`, `discovery-server/`: infraestructura Spring Cloud
- `scripts/`: automatizaciones de setup y verificación

## Documentación recomendada para empezar

1. `docs/INDEX.md`
2. `docs/SETUP_AND_DEPLOYMENT.md`
3. `docs/QUICK_REFERENCE.md`

## Comandos frecuentes

```bash
make help
make dev-setup
make docker-up
make docker-ps
make docker-down
```

Si no usas `make`, puedes ejecutar `docker compose` directamente con los archivos en `infra/docker/compose/`.

