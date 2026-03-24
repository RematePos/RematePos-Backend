# 🚀 Microservice POS - Setup & Deployment Guide

**Last Updated:** March 2026  
**Status:** Production-Ready Pipeline Setup  
**Java Version:** 21 (LTS)  
**Spring Boot:** 4.0.3  
**Spring Cloud:** 2025.1.0

---

## 📋 TABLA DE CONTENIDOS

1. [Quick Start](#quick-start)
2. [Arquitectura](#arquitectura)
3. [Setup Local](#setup-local)
4. [CI/CD Pipeline](#cicd-pipeline)
5. [Monitoreo](#monitoreo)
6. [Deployment](#deployment)
7. [Troubleshooting](#troubleshooting)
8. [Checklist Pre-Producción](#checklist-pre-producción)

---

## 🏃 Quick Start

### Prerequisitos
- Docker & Docker Compose v2.10+
- Java 21 JDK
- Maven 3.8.1+
- Git

### Levantar en Local (5 min)

```bash
# 1. Clonar repo
git clone https://github.com/tu-org/microservice-pos.git
cd microservice-pos

# 2. Copiar .env
cp infra/docker/env/.env.dev.example infra/docker/env/.env.local

# 3. Levantar todo
cd infra/docker/compose
docker-compose -f docker-compose.yml --env-file ../env/.env.local up -d

# 4. Verificar
curl http://localhost:8761/actuator/health
# Respuesta: {"status":"UP"}
```

### URLs Locales
| Servicio | URL | Puerto |
|----------|-----|--------|
| Config Server | http://localhost:8888 | 8888 |
| Eureka Discovery | http://localhost:8761 | 8761 |
| Customer MS | http://localhost:8091 | 8091 |
| Product MS | http://localhost:8092 | 8092 |
| Cart MS | http://localhost:8093 | 8093 |
| MongoDB | localhost:27017 | 27017 |
| PostgreSQL | localhost:5433 | 5433 |

---

## 🏗️ Arquitectura

```
microservice-pos/
├── config-server/              # Configuración centralizada
├── discovery-server/           # Eureka (service discovery)
├── microservices/
│   ├── customer-microservice/  # MongoDB
│   ├── product-microservice/   # PostgreSQL (por definir)
│   ├── cart-microservice/      # PostgreSQL (por definir)
│   └── common-exceptions/      # Shared library
├── infra/
│   ├── docker/
│   │   ├── compose/           # Docker Compose files
│   │   └── env/               # Environment files
│   └── kubernetes/            # (Próximamente)
├── Jenkinsfile                # CI/CD Pipeline
└── .github/workflows/         # GitHub Actions (alternativa)
```

### Flujo de Datos
```
API Client 
  ↓
API Gateway (futura)
  ↓
[Config Server] ← Provides config
  ↓
[Eureka] ← Service Discovery
  ↓
[Customer MS] ← [MongoDB]
[Product MS] ← [PostgreSQL]
[Cart MS] ← [PostgreSQL]
  ↓
[Monitoring: Prometheus + Grafana]
[Logging: ELK Stack]
[Tracing: Jaeger]
```

---

## 🛠️ Setup Local

### 1. Clonar & Preparar

```bash
git clone https://github.com/tu-org/microservice-pos.git
cd microservice-pos

# Crear archivos .env necesarios
cp infra/docker/env/.env.dev.example infra/docker/env/.env.dev
cp infra/docker/env/.env.dev.example infra/docker/env/.env.local

# Editar .env.local si necesitas cambiar credenciales
# nano infra/docker/env/.env.local
```

### 2. Build Local (Sin Docker)

```bash
# Build todo el proyecto
./mvnw clean package -DskipTests

# Output esperado:
# [INFO] Building microservice-pos 0.0.1-SNAPSHOT
# [INFO] BUILD SUCCESS
```

### 3. Levantar Stack Completo

```bash
# Ir al directorio de compose
cd infra/docker/compose

# Levantar en dev
docker-compose -f docker-compose.dev.yml \
  --env-file ../env/.env.dev \
  up -d

# Verificar estado
docker-compose ps

# Logs
docker-compose logs -f config-server
docker-compose logs -f discovery-server
docker-compose logs -f customer-microservice
```

### 4. Validar Salud del Sistema

```bash
# Health checks
curl http://localhost:8761/actuator/health
curl http://localhost:8091/actuator/health
curl http://localhost:8092/actuator/health

# Listar servicios registrados
curl http://localhost:8761/eureka/apps

# Métricas
curl http://localhost:8091/actuator/metrics
```

### 5. Parar Stack

```bash
docker-compose down -v  # -v para borrar volúmenes también
```

---

## 🔄 CI/CD Pipeline

### Opción 1: Jenkins (Recomendado para Enterprise)

#### Setup Jenkins

```bash
# 1. Instalar Jenkins
docker run -d \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts

# 2. Obtener contraseña inicial
docker logs <jenkins-container-id> | grep "Initial Admin"

# 3. Ir a http://localhost:8080
# Instalar plugins sugeridos
```

#### Crear Pipeline en Jenkins

```groovy
1. New Item → Pipeline
2. Name: microservice-pos
3. Pipeline → Definition: Pipeline script from SCM
4. SCM: Git
5. Repository URL: https://github.com/tu-org/microservice-pos.git
6. Credentials: Crear token GitHub
7. Branches: */develop, */release, */main
8. Script Path: Jenkinsfile
9. Save & Build
```

#### Variables de Entorno en Jenkins

**Manage Jenkins → Configure System → Environment variables:**

```
DOCKER_REGISTRY=gcr.io/tu-proyecto
DOCKER_CREDENTIALS=<credenciales-gcloud>
SONAR_HOST_URL=http://sonarqube:9000
SONAR_LOGIN=<token-sonar>
```

#### Credenciales en Jenkins

```groovy
// Manage Jenkins → Manage Credentials → System → Global credentials

1. Docker Registry
   ID: docker-registry-url
   Username: _json_key
   Password: <gcloud-json-key>

2. Docker Credentials
   ID: docker-registry-credentials
   Password: <registry-credentials>

3. SonarQube Token
   ID: sonar-token
   Secret: <token-sonarqube>

4. SonarQube Host
   ID: sonar-host-url
   Secret: http://sonarqube:9000
```

### Opción 2: GitHub Actions (Recomendado para GitHub)

#### Secrets de GitHub

```
Settings → Secrets and variables → Actions → New repository secret

SONAR_HOST_URL = http://sonarqube:9000
SONAR_TOKEN = <token-sonarqube>
QA_DEPLOY_TOKEN = <token-deploy-qa>
RELEASE_DEPLOY_TOKEN = <token-deploy-release>
PROD_DEPLOY_TOKEN = <token-deploy-prod>
```

#### Ejecutar Workflow Manual

```bash
# Ver workflows disponibles
gh workflow list

# Disparar manualmente
gh workflow run ci-cd.yml -r main

# Ver ejecuciones
gh run list
```

### Stages del Pipeline

```
┌─────────────────────────────────────────────────────────┐
│                    CI/CD PIPELINE                       │
├─────────────────────────────────────────────────────────┤
│ ✓ Checkout Code                                         │
│ ✓ Build Maven (clean package)                           │
│ ✓ Unit Tests (JUnit 5)                                  │
│ ✓ Code Quality (SonarQube)                              │
│ ✓ Code Coverage (JaCoCo → 80%+)                         │
│ ✓ Build Docker Images (multi-stage)                     │
│ ✓ Push to Registry (GHCR/GCR)                           │
│ ↓ [IF develop]                                          │
│ ✓ Deploy to QA (automático)                             │
│ ↓ [IF release]                                          │
│ ⚠️  Manual approval → Deploy Release                     │
│ ↓ [IF main]                                             │
│ ⚠️  Manual approval (admins only) → Deploy Prod          │
│ ✓ Health Checks (curl actuator)                         │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 Monitoreo

### SonarQube (Code Quality)

```bash
# 1. Levantar SonarQube
docker run -d \
  -p 9000:9000 \
  -e sonar.jdbc.url=jdbc:postgresql://db:5432/sonar \
  sonarqube:lts

# 2. Ir a http://localhost:9000
# Usuario: admin / Contraseña: admin

# 3. Crear token
# Settings → Security → User Tokens → Generate
# Guardar token

# 4. Ejecutar análisis local
./mvnw sonar:sonar \
  -Dsonar.projectKey=microservice-pos \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<token>
```

### Prometheus + Grafana (Metrics)

```bash
# Las métricas ya están disponibles en:
# http://localhost:8091/actuator/prometheus
# http://localhost:8092/actuator/prometheus
# http://localhost:8093/actuator/prometheus

# Configurar Prometheus para scrapear:
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'customer-microservice'
    static_configs:
      - targets: ['localhost:8091']
    metrics_path: '/actuator/prometheus'

  - job_name: 'product-microservice'
    static_configs:
      - targets: ['localhost:8092']
    metrics_path: '/actuator/prometheus'

  - job_name: 'cart-microservice'
    static_configs:
      - targets: ['localhost:8093']
    metrics_path: '/actuator/prometheus'
```

### Docker Compose con Stack Completo

```yaml
# Próximamente: docker-compose.monitoring.yml
services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin

  jaeger:
    image: jaegertracing/all-in-one:latest
    ports:
      - "6831:6831/udp"
      - "16686:16686"
```

---

## 🚀 Deployment

### A QA (Automático desde develop)

El pipeline hace esto automáticamente cuando haces merge a `develop`:

```bash
# En CI/CD:
cd infra/docker/compose
docker-compose -f docker-compose.qa.yml \
  --env-file ../env/.env.qa \
  pull
docker-compose up -d
```

**URLs QA:**
- Eureka: http://qa.example.com:8761
- Customer: http://qa.example.com:8091
- Product: http://qa.example.com:8092

### A Release (Manual desde release)

```bash
# 1. Crear rama release
git checkout develop
git pull origin develop
git checkout -b release/v1.0.0

# 2. Actualizar versión
./mvnw versions:set -DnewVersion=1.0.0

# 3. Commit & push
git add .
git commit -m "Release v1.0.0"
git push origin release/v1.0.0

# 4. Pipeline detecta rama release
# → Requiere aprobación manual en Jenkins/GitHub
# → Deploy a release environment
```

### A Producción (Manual desde main)

```bash
# 1. Hacer PR de release → main
# 2. Code review & merge
# 3. Tag git
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# 4. Pipeline detecta rama main
# → Requiere aprobación de admins
# → Deploy a producción
# → Notificación a Slack/email
```

### Rollback de Producción

```bash
# Si algo sale mal:

# 1. Revert commit
git revert <commit-hash>
git push origin main

# 2. Pipeline vuelve a ejecutarse
# → Deploy de versión anterior

# O manual:
cd infra/docker/compose
docker-compose -f docker-compose.main.yml \
  --env-file ../env/.env.main \
  pull  # Tira última imagen stable
docker-compose up -d
```

---

## 🔍 Troubleshooting

### Services no se registran en Eureka

```bash
# 1. Verificar que config-server esté up
curl http://localhost:8888/actuator/health

# 2. Verificar logs
docker-compose logs config-server
docker-compose logs customer-microservice

# 3. Limpiar y restart
docker-compose down -v
docker-compose up -d

# 4. Esperar 30s para registro
sleep 30
curl http://localhost:8761/eureka/apps
```

### MongoDB no conecta

```bash
# 1. Verificar MongoDB levantado
docker ps | grep mongo

# 2. Probar conexión
mongosh "mongodb://localhost:27017"

# 3. Si no existe DB
use customer_db
db.createCollection("customers")

# 4. Revisar SPRING_DATA_MONGODB_URI en .env
# mongodb://mongo:27017/customer_db (en docker)
# mongodb://localhost:27017/customer_db (local)
```

### Build Maven falla

```bash
# 1. Limpiar cache Maven
rm -rf ~/.m2/repository/com/corhuila

# 2. Rebuild
./mvnw clean install -U

# 3. Si falla dependencia, revisar:
# - Tienes common-exceptions compilado?
# - mvn clean install -pl common-exceptions -am
```

### Tests fallan en CI/CD

```bash
# 1. Ver resultado en Jenkins/GitHub Actions
# 2. Reproducir localmente
./mvnw test -Dtest=CustomerMicroserviceApplicationTests

# 3. Debug con
./mvnw test -DdebugTests

# 4. Revisar que test requirements:
# - MongoDB levantado
# - Puertos disponibles
# - Memoria suficiente
```

---

## ✅ Checklist Pre-Producción

### ANTES de conectar a Jenkins/GitHub Actions

- [ ] **Código**
  - [ ] Todos los tests pasan localmente
  - [ ] Code coverage > 80%
  - [ ] Sin errores SonarQube críticos
  - [ ] Controllers implementados para cada MS
  - [ ] DTOs validados con @Valid

- [ ] **Infraestructura**
  - [ ] docker-compose levanta sin errores
  - [ ] Health checks responden OK
  - [ ] Eureka registra todos los servicios
  - [ ] Base de datos migra sin problemas

- [ ] **Configuración**
  - [ ] application-*.yml para cada ambiente
  - [ ] .env files correctos por ambiente
  - [ ] Secrets en Jenkins/GitHub Actions setup
  - [ ] Logging configurado por ambiente

- [ ] **Monitoreo**
  - [ ] SonarQube conectando
  - [ ] Prometheus metrics accesibles
  - [ ] Health checks configurados
  - [ ] Actuators expuestos en management.endpoints

- [ ] **Seguridad**
  - [ ] Spring Security implementado (básico)
  - [ ] Passwords en .env (no hardcodeado)
  - [ ] Jenkins/GitHub credentials creadas
  - [ ] CORS configurado si aplica

- [ ] **CI/CD**
  - [ ] Jenkinsfile/GitHub Actions workflow creados
  - [ ] Pipeline variables setadas
  - [ ] Docker registry accesible
  - [ ] Aprobaciones manuales configuradas

### DESPUÉS de conectar a Jenkins/GitHub Actions

- [ ] Primera ejecución en develop (build + test)
- [ ] Deploy automático a QA funciona
- [ ] Verificar health en QA
- [ ] Test de aprobación manual en Jenkins
- [ ] Deploy a Release manual
- [ ] Prueba de rollback
- [ ] Notificaciones Slack configuradas

---

## 📚 Referencias

- [Spring Boot 4.0 Docs](https://spring.io/projects/spring-boot)
- [Spring Cloud 2025.1 Docs](https://spring.io/projects/spring-cloud)
- [Eureka Server](https://github.com/Netflix/eureka)
- [SonarQube Docs](https://docs.sonarqube.org)
- [Docker Compose Docs](https://docs.docker.com/compose)
- [Prometheus Docs](https://prometheus.io/docs)
- [Grafana Docs](https://grafana.com/docs)

---

## 🤝 Support

**Dudas sobre el setup?**
1. Revisar logs: `docker-compose logs <service>`
2. Revisar troubleshooting arriba
3. Ejecutar health checks
4. Revisar variables de entorno

---

**Last Updated:** March 2026 | **Maintainer:** @tu-usuario


