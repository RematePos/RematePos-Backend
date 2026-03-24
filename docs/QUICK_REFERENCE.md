# 📖 QUICK REFERENCE - Microservice POS

**Guardar como favorito** para consulta rápida

---

## 🚀 QUICK START (5 min)

```bash
# 1. Pull cambios
git pull origin develop

# 2. Setup local
make dev-setup          # Copia .env files
make docker-up          # Levanta todo
make health            # Valida

# 3. Ver logs
make logs

# 4. Tests
make test
```

---

## 📁 ARCHIVOS NUEVOS - UBICACIÓN

```
microservice-pos/
├── Jenkinsfile                              ← Pipeline Jenkins
├── .github/workflows/ci-cd.yml              ← GitHub Actions
├── SETUP_AND_DEPLOYMENT.md                  ← 📘 Documentación completa
├── sonar-project.properties                 ← SonarQube config
├── Makefile                                 ← make <comando>
├── jenkins-setup.sh                         ← bash jenkins-setup.sh
├── scripts/
│   └── pre-commit.sh                        ← Git hooks
└── microservices/customer-microservice/src/main/resources/
    ├── application-qa.yml                   ← Config QA
    ├── application-docker.yml               ← Config Docker
    ├── application-release.yml              ← Config Release
    └── application-main.yml                 ← Config Producción

ACTUALIZADO:
├── pom.xml                                  ← SonarQube + JaCoCo props
├── microservices/pom.xml                    ← Prometheus + JaCoCo plugin
└── microservices/customer-microservice/src/test/
    ├── java/.../CustomerMicroserviceApplicationTests.java
    └── resources/application-test.yml
```

---

## 🎯 MATRIZ DE DECISIONES

### ¿Usar Jenkins o GitHub Actions?

| Aspecto | Jenkins | GitHub Actions |
|---------|---------|---|
| Setup | Complejo | Simple |
| Requiere servidor | ✅ Yes | ❌ No |
| Mejor para | Enterprise | Startups/OSS |
| Precio | Gratis pero $$ infra | Gratis (hasta límite) |
| Skill Requerida | Media | Baja |

→ **Recomendación:** GitHub Actions si estás en GitHub. Jenkins si tienes equipo DevOps.

---

## 📊 ESTRUCTURA PIPELINE

### Rama: **develop**
```
Push → Build → Test → SonarQube → Docker Build → Docker Push → Deploy QA ✅
```

### Rama: **release**  
```
Push → Build → Test → SonarQube → Docker Build → Docker Push → ⚠️ Approval → Deploy Release
```

### Rama: **main**
```
Push → Build → Test → SonarQube → Docker Build → Docker Push → 🔴 Approval (Admin) → Deploy Prod
```

---

## 🗂️ CONFIGURACIÓN POR AMBIENTE

### DEV (Local)
```yaml
# application.yml (default)
mongodb: localhost:27017
logging: DEBUG
metrics: enabled
```

### QA  
```yaml
# application-qa.yml
mongodb: mongo:27017
logging: WARN
metrics: enabled
eureka: discovery-server:8761
```

### RELEASE
```yaml
# application-release.yml
mongodb: mongodb+srv://...
logging: WARN (JSON format)
compression: enabled
metrics: enabled
```

### PRODUCTION (main)
```yaml
# application-main.yml
mongodb: AWS/Azure managed
logging: ERROR (JSON format)
ssl: enabled
metrics: enabled
```

---

## 🔧 COMANDOS FRECUENTES

### Build
```bash
make build              # Full build
make build-quick        # Sin tests
make clean              # Clean artifacts
```

### Local
```bash
make docker-up          # Start DEV
make docker-down        # Stop
make docker-logs        # Logs (Ctrl+C to exit)
make docker-clean       # Stop + remove volumes
```

### Testing
```bash
make test               # Unit tests
make test-coverage      # + JaCoCo report
make sonar              # SonarQube analysis
```

### Deploy
```bash
make deploy-qa          # Auto (develop branch)
make deploy-release     # Manual approval
make deploy-prod        # Requires "DEPLOY_PROD" confirmation
```

### Utils
```bash
make health             # Health checks
make version            # Show version
make install-hooks      # Install pre-commit
```

---

## 🔐 SECRETS & CREDENTIALS

### Jenkins (Manage Jenkins → Manage Credentials)

| ID | Type | What | From |
|---|---|---|---|
| docker-registry-url | Secret text | Registry URL | Your Docker/GCR |
| docker-registry-credentials | Secret text | Push credentials | GCloud/Docker Hub |
| sonar-token | Secret text | SonarQube token | SonarQube UI |
| sonar-host-url | Secret text | SonarQube URL | Your SonarQube |

### GitHub Actions (Settings → Secrets)

```
SONAR_HOST_URL
SONAR_TOKEN
QA_DEPLOY_TOKEN
RELEASE_DEPLOY_TOKEN
PROD_DEPLOY_TOKEN
```

**NUNCA guardes secrets en:**
- ❌ Código
- ❌ .env files committeados
- ❌ Docker images

**SIEMPRE usa:**
- ✅ Jenkins Credentials
- ✅ GitHub Secrets
- ✅ AWS Secrets Manager
- ✅ HashiCorp Vault

---

## 🐛 TROUBLESHOOTING RÁPIDO

### Services no se registran en Eureka
```bash
docker-compose logs discovery-server  # Ver errores
docker-compose logs config-server     # Config server
docker-compose down -v && docker-compose up -d  # Reset
sleep 30  # Wait for registration
curl http://localhost:8761/eureka/apps  # Check
```

### MongoDB no conecta
```bash
# Check si está up
docker-compose ps | grep mongo

# Conectar directamente
mongosh "mongodb://localhost:27017"

# Revisar URI en .env
# mongodb://mongo:27017/customer_db (en Docker)
# mongodb://localhost:27017/customer_db (Local)
```

### Maven build falla
```bash
./mvnw clean -U  # Update all dependencies
./mvnw clean package -DskipTests  # Skip tests
rm -rf ~/.m2/repository/com/corhuila  # Clear cache
```

### Tests fallan
```bash
make test  # Run localmente
# Si pasan local pero fallan en CI:
# → Check environment variables en Jenkins
# → Check que MongoDB levantado en CI
# → Check memory/disk en CI agent
```

---

## 📈 MONITOREO

### Métricas disponibles (sin setup extra)
```
GET http://localhost:8091/actuator/metrics
GET http://localhost:8091/actuator/health
GET http://localhost:8091/actuator/prometheus  # Prometheus scrape
```

### Setup SonarQube local
```bash
docker run -d -p 9000:9000 sonarqube:lts

# http://localhost:9000
# User: admin
# Pass: admin

make sonar  # Run analysis
```

### Setup Prometheus local
```bash
# Create prometheus.yml with:
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'customer-microservice'
    static_configs:
      - targets: ['localhost:8091']
    metrics_path: '/actuator/prometheus'

docker run -d -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:latest

# http://localhost:9090
```

---

## 🚢 DEPLOYMENT CHECKLIST

### Antes de QA
- [ ] `make test` pasa
- [ ] `make sonar` sin errores críticos
- [ ] `make docker-up` funciona local
- [ ] Health checks responden

### Antes de Release
- [ ] QA tests passed
- [ ] Code coverage > 80%
- [ ] Todas features completadas
- [ ] Version tag creado (v1.0.0)

### Antes de Producción
- [ ] Release testing completo
- [ ] Backup DB en lugar seguro
- [ ] Rollback plan documentado
- [ ] Team notificado
- [ ] Approval de manager

### Post-Deployment
- [ ] Health checks OK
- [ ] Logs without errors
- [ ] Metrics normal
- [ ] No customer complaints (primeras 2 horas)
- [ ] Update status page

---

## 📚 DOCUMENTOS IMPORTANTES

| Doc | Para qué | Dónde |
|---|---|---|
| docs/SETUP_AND_DEPLOYMENT.md | Setup completo + troubleshooting | docs/ |
| docs/PROJECT_STRUCTURE.md | Estructura y organización del repo | docs/ |
| Jenkinsfile | Pipeline definition | Root |
| sonar-project.properties | SonarQube config | Root |
| Makefile | Comandos útiles | Root |
| docs/README.docker.md | Docker reference | docs/ |

---

## 🆘 SOPORTE RÁPIDO

**¿No sabes cómo empezar?**
→ `make help` o ver `docs/SETUP_AND_DEPLOYMENT.md`

**¿Docker no funciona?**
→ `make docker-logs` y busca en `docs/SETUP_AND_DEPLOYMENT.md` troubleshooting

**¿Tests fallan?**
→ `make test` local primero, revisar application-test.yml

**¿Jenkins no levanta?**
→ `bash jenkins-setup.sh` para instrucciones paso-a-paso

**¿Deployment falla?**
→ Ver `docs/SETUP_AND_DEPLOYMENT.md` sección Deployment

---

## 🎓 LEARNING PATH

```
Week 1:
├─ Read docs/SETUP_AND_DEPLOYMENT.md
├─ Run make docker-up
├─ Run make test
└─ Explore make commands

Week 2:
├─ Setup Jenkins (bash jenkins-setup.sh)
├─ Create first pipeline job
├─ Test push to develop branch
└─ Validate QA deployment

Week 3:
├─ Start implementing logic
├─ Add controllers
├─ Add tests
└─ Commit & trigger pipeline

Week 4:
├─ Reach 80% test coverage
├─ Setup SonarQube
├─ Fix SonarQube findings
└─ Prepare release branch

Week 5:
├─ Release to QA
├─ Final testing
├─ Release to Production
└─ Monitor & enjoy! 🎉
```

---

## 💡 PRO TIPS

1. **Git flow**
   ```bash
   # Feature branch from develop
   git checkout develop
   git pull
   git checkout -b feature/xyz
   # ... code ...
   git push origin feature/xyz
   # Create PR on GitHub
   ```

2. **Pre-commit validation**
   ```bash
   make install-hooks  # Auto validates before commit
   ```

3. **Fast rebuild**
   ```bash
   make build-quick    # Skip tests, faster
   make test          # Tests separately
   ```

4. **Check everything**
   ```bash
   make check          # lint + test (shortcut)
   ```

5. **Local debugging**
   ```bash
   make docker-down
   mvn spring-boot:run -pl microservices/customer-microservice
   # Connect IDE debugger to localhost:5005
   ```

---

## 🔍 AMBIENTE ESPECÍFICO

### Ver qué ambiente estoy en
```bash
# En logs
docker-compose logs customer-microservice | grep SPRING_PROFILES_ACTIVE

# En config server
curl http://localhost:8888/customer-microservice/docker

# En actuator
curl http://localhost:8091/actuator/env | jq .propertySources
```

### Cambiar ambiente rápido
```bash
# QA
cd infra/docker/compose && \
docker-compose -f docker-compose.qa.yml \
  --env-file ../env/.env.qa up -d

# Release
cd infra/docker/compose && \
docker-compose -f docker-compose.release.yml \
  --env-file ../env/.env.release up -d
```

---

**Last Update:** March 2026 | **Version:** 1.0.0

*Bookmark this page for quick reference!*


