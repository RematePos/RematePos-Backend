# Git Upload Policy (Monorepo Backend)

Esta guia define que SI y NO se debe versionar en el monorepo.

## Si subir a Git

- Codigo fuente: `**/src/**`
- POMs y build config: `pom.xml`, `**/pom.xml`
- Infraestructura declarativa: `infra/docker/compose/**`, `Dockerfile`, `Jenkinsfile`, `sonar-project.properties`, `Makefile`
- Scripts operativos: `scripts/**`
- Documentacion: `README.md`, `docs/**`
- Postman compartible: `docs/postman/*.postman_collection.json`, `docs/postman/*.postman_environment.json` (sin secretos)
- Plantillas de entorno: `infra/docker/env/.env.*.example`, `infra/docker/env/.env.example`

## No subir a Git

- Artefactos compilados: `target/`, `build/`, `dist/`
- Logs locales: `*.log`, `logs/`, `**/logs/`
- Temporales y evidencias locales: `tmp/`, `**/tmp/`
- Configuracion local IDE: `.idea/`, `.vscode/`, `*.iml`
- Secretos y env reales: `infra/docker/env/.env`, `infra/docker/env/.env.*` (excepto `*.example`)

## Reglas de rama (HU)

- Crear rama por historia: `hu/HU-XX-descripcion-corta`
- Commits con referencia HU: `HU-XX: mensaje`
- PR siempre a `develop`
- No mezclar cambios de varias HU en un mismo PR

## Checklist antes de push

1. `git status` limpio de archivos sensibles
2. No incluir `target/`, `logs/`, `tmp/`
3. Ejecutar pruebas minimas del servicio tocado
4. Actualizar docs si cambian rutas, puertos o contratos

