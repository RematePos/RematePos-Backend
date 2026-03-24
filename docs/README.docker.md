# Docker Multi-Environment Runbook

This project uses one compose base file plus one override per environment.

- `infra/docker/compose/docker-compose.yml` (base)
- `infra/docker/compose/docker-compose.dev.yml`
- `infra/docker/compose/docker-compose.qa.yml`
- `infra/docker/compose/docker-compose.release.yml`
- `infra/docker/compose/docker-compose.main.yml`

## Quick validation status

I validated the compose syntax with `docker compose config` for all environments.

- `dev:0`
- `qa:0`
- `release:0`
- `main:0`

`0` means the compose file set is valid for that environment.

## Files to share in Git

Share these files so another developer can run all environments directly:

- `infra/docker/compose/docker-compose.yml`
- `infra/docker/compose/docker-compose.dev.yml`
- `infra/docker/compose/docker-compose.qa.yml`
- `infra/docker/compose/docker-compose.release.yml`
- `infra/docker/compose/docker-compose.main.yml`
- `infra/docker/env/.env.example`
- `infra/docker/env/.env.dev.example`
- `infra/docker/env/.env.qa.example`
- `infra/docker/env/.env.release.example`
- `infra/docker/env/.env.main.example`
- `docs/README.docker.md`

## Security baseline (recommended for final semester deploy)

- Commit only templates (`*.example`), never real `infra/docker/env/.env.*` files.
- Keep real secrets local or in CI/CD secret variables.
- Rotate any password that was ever committed in Git history.
- Do not publish database ports in production (`main` already sets `ports: []` for DBs).
- Prefer strong unique passwords per environment (`dev`, `qa`, `release`, `main`).

If real env files were committed before, untrack them once:

```powershell
git rm --cached .env
git rm --cached infra/docker/env/.env.dev infra/docker/env/.env.qa infra/docker/env/.env.release infra/docker/env/.env.main infra/docker/env/.env.local
git commit -m "Stop tracking sensitive env files"
```

## Local setup for another developer

```powershell
git clone <REPO_URL>
Set-Location "<REPO_FOLDER>"
Copy-Item .\infra\docker\env\.env.dev.example .\infra\docker\env\.env.dev
Copy-Item .\infra\docker\env\.env.qa.example .\infra\docker\env\.env.qa
Copy-Item .\infra\docker\env\.env.release.example .\infra\docker\env\.env.release
Copy-Item .\infra\docker\env\.env.main.example .\infra\docker\env\.env.main
```

Edit `infra/docker/env/.env.main` with real production credentials before using `main`.

## Start environments from terminal (PowerShell)

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml up -d --build
docker compose -p pos-qa --env-file .\infra\docker\env\.env.qa -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.qa.yml up -d --build
docker compose -p pos-release --env-file .\infra\docker\env\.env.release -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.release.yml up -d --build
```

Optional `main`:

```powershell
docker compose -p pos-main --env-file .\infra\docker\env\.env.main -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.main.yml up -d --build
```

## Stop environments

```powershell
docker compose -p pos-dev --env-file .\infra\docker\env\.env.dev -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.dev.yml down
docker compose -p pos-qa --env-file .\infra\docker\env\.env.qa -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.qa.yml down
docker compose -p pos-release --env-file .\infra\docker\env\.env.release -f .\infra\docker\compose\docker-compose.yml -f .\infra\docker\compose\docker-compose.release.yml down
```

## Verify running containers

```powershell
docker ps
```

Expected host ports per environment:

- `dev`: `8888`, `8761`, `8091`, `8092`, `8093`
- `qa`: `18888`, `18761`, `18091`, `18092`, `18093`
- `release`: `28888`, `28761`, `28091`, `28092`, `28093`

## Run with Portainer (no terminal after initial setup)

1. Open Portainer and select environment `local`.
2. Go to `Stacks` -> `Add stack`.
3. Name stack `pos-dev` (then `pos-qa`, `pos-release`).
4. In `Stack file content`, paste `infra/docker/compose/docker-compose.yml`.
5. In `Environment variables`, switch to advanced mode and paste variables from:
   - `infra/docker/env/.env.dev` for `pos-dev`
   - `infra/docker/env/.env.qa` for `pos-qa`
   - `infra/docker/env/.env.release` for `pos-release`
6. Click `Deploy the stack`.

## Notes

- This is Docker Compose multi-environment, not Kubernetes.
- Service-to-service communication uses internal container ports (`8888`, `8761`, `27017`, `5432`).
- `main` hides DB host ports via `ports: []` in `infra/docker/compose/docker-compose.main.yml`.
