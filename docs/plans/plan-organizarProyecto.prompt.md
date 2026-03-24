# Plan de organizacion segura del repositorio

## Objetivo
Ordenar estructura y flujos sin romper ejecucion actual (Docker, scripts, docs), con migracion gradual y verificable.

## Checklist priorizada

- [x] Fase 0 - Baseline
  - [x] Inventariar referencias activas en `Makefile`, `scripts/verify-setup.sh` y `jenkins-setup.sh`.
  - [x] Confirmar comandos vigentes para levantar ambientes (`docker compose`/`docker-compose`).

- [x] Fase 1 - Documentacion
  - [x] Consolidar como canonicos los documentos dentro de `docs/`.
  - [x] Mantener en raiz solo archivos puente temporales hacia `docs/`.
  - [x] Actualizar referencias que aun apunten a `.md` en raiz.

- [x] Fase 2 - Docker multiambiente
  - [x] Estandarizar `infra/docker/compose/docker-compose.yml` (base).
  - [x] Usar overrides `docker-compose.dev.yml`, `docker-compose.qa.yml`, `docker-compose.release.yml`, `docker-compose.main.yml`.
  - [x] Mantener variables por ambiente en `infra/docker/env/.env.*.example`.

- [ ] Fase 3 - Scripts y automatizacion
  - [x] Alinear rutas en scripts a ubicaciones canonicas (`docs/`, `infra/docker/...`).
  - [x] Mantener compatibilidad temporal donde sea necesario para no romper flujos existentes.

- [ ] Fase 4 - Validacion y retiro controlado
  - [ ] Validar existencia de archivos clave y rutas actualizadas.
  - [x] Validar compose por ambiente (`docker compose config`).
  - [x] Retirar archivos puente de raiz cuando todas las referencias esten migradas.

## Proximos avances para despliegue

- [ ] Alinear promotion flow en `Jenkinsfile` con ramas reales y aprobaciones.
- [ ] Fijar versionado inmutable de imagenes por build/tag para promover entre ambientes.
- [ ] Hacer smoke test gate obligatorio post-deploy en Jenkins.
- [ ] Documentar rollback release->main en `docs/SETUP_AND_DEPLOYMENT.md`.
- [ ] Ejecutar simulacro completo de despliegue y rollback antes de cierre de semestre.

## Criterios de listo

- No hay referencias rotas en scripts ni docs.
- La documentacion canonicamente vive en `docs/`.
- Cada ambiente Docker tiene compose override y env template.
- El repositorio tiene punto de entrada claro (`README.md`) y comandos de arranque conocidos.


