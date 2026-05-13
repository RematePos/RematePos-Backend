# Docker Compose Files

Los compose internos viven en esta carpeta, pero tambien existen atajos visibles en la raiz del repositorio.

## Atajos visibles desde la raiz

| Ambiente | Archivo raiz | Usa internamente |
| --- | --- | --- |
| DEV | `docker-compose.dev.yml` | `docker-compose.yml` + `docker-compose.dev.yml` |
| QA | `docker-compose.qa.yml` | `docker-compose.yml` + `docker-compose.qa.yml` |
| RELEASE | `docker-compose.release.yml` | `docker-compose.yml` + `docker-compose.release.yml` |
| MAIN | `docker-compose.main.yml` | `docker-compose.yml` + `docker-compose.main.yml` |

## Archivos canonicos internos

| Archivo | Proposito |
| --- | --- |
| `docker-compose.yml` | Servicios base del backend: config-server, discovery-server, gateway y microservicios. |
| `docker-compose.dev.yml` | Perfiles y ajustes para DEV. |
| `docker-compose.qa.yml` | Perfiles y ajustes para QA. |
| `docker-compose.release.yml` | Perfiles y ajustes para RELEASE. |
| `docker-compose.main.yml` | Perfiles y ajustes para MAIN/produccion futura. |
| `db-compose.*.override.yml` | Overrides para conectar cada ambiente con bases de datos independientes. |

## Comandos recomendados

Desde la raiz del repositorio:

```powershell
docker compose -f .\docker-compose.dev.yml up -d --build
docker compose -f .\docker-compose.qa.yml up -d --build
docker compose -f .\docker-compose.release.yml up -d --build
docker compose -f .\docker-compose.main.yml up -d --build
```

Para apagar:

```powershell
docker compose -f .\docker-compose.dev.yml down
docker compose -f .\docker-compose.qa.yml down
docker compose -f .\docker-compose.release.yml down
docker compose -f .\docker-compose.main.yml down
```
