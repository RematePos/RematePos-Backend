# 📂 ESTRUCTURA DEL PROYECTO - BEST PRACTICES IMPLEMENTADAS

**Fecha:** March 23, 2026  
**Status:** ✅ Reorganizado siguiendo industry standards  

---

## 🎯 PRINCIPIOS APLICADOS

```
✅ Separation of Concerns - Cada cosa en su lugar
✅ Scalability - Fácil de crecer
✅ Maintainability - Fácil de mantener
✅ Convention over Configuration - Expectativas claras
✅ DRY - No Repeat Yourself
✅ KISS - Keep It Simple, Stupid
```

---

## 📁 ESTRUCTURA NUEVA (RECOMENDADA)

### ROOT LEVEL (Archivos críticos solamente)

```
microservice-pos/
├── README.md                    # Proyecto overview
├── Makefile                     # Desarrollo commands
├── pom.xml                      # Maven build
├── VERSION                      # Versión proyecto
├── CHANGELOG.md                 # Historial versiones
├── CONTRIBUTING.md              # Guía contribución
├── CODE_OF_CONDUCT.md           # Normas equipo
├── LICENSE                      # Licencia
├── .gitignore                   # Git ignore
├── .editorconfig                # Editor config
└── .env.example                 # Env template
```

**NO en root:**
❌ Jenkinsfile → Ir a: `ci-cd/`  
❌ Documentación → Ir a: `docs/`  
❌ Scripts → Ir a: `scripts/`  

---

## 📚 DOCS/ - Toda la documentación

```
docs/
├── INDEX.md                     # Mapa de documentación
├── QUICK_REFERENCE.md           # Comandos rápidos
├── SETUP_AND_DEPLOYMENT.md      # Guía setup
├── NEXT_STEPS.md                # Plan 5-week
├── EXECUTIVE_SUMMARY.md         # Para boss
├── ARCHITECTURE.md              # Decisiones arquitectura
├── CONTRIBUTING.md              # Cómo contribuir
├── SECURITY.md                  # Seguridad
├── TROUBLESHOOTING.md           # Common issues
├── API.md                       # API documentation
├── PROJECT_STRUCTURE.md         # Esta estructura
├── DIAGNOSTICO_PROYECTO.md      # Análisis madurez
└── images/
    └── architecture-diagram.png
```

**Ventajas:**
✅ Toda documentación centralizada  
✅ Fácil de encontrar  
✅ Versionable con código  
✅ Links internos funcionan  

---

## 🔧 SCRIPTS/ - Organizado por función

```
scripts/
├── setup/
│   ├── verify-setup.sh          # Validar ambiente
│   ├── jenkins-setup.sh         # Setup Jenkins
│   ├── local-setup.sh           # Setup local (FUTURA)
│   └── reorganize-project.sh    # Reorganizar proyecto
├── deployment/
│   ├── deploy-qa.sh             # Deploy QA (FUTURA)
│   ├── deploy-release.sh        # Deploy Release (FUTURA)
│   └── deploy-prod.sh           # Deploy Prod (FUTURA)
├── git/
│   └── pre-commit.sh            # Git hooks
└── maintenance/
    ├── backup-db.sh             # Backup DB (FUTURA)
    └── cleanup.sh               # Cleanup (FUTURA)
```

**Ventajas:**
✅ Scripts agrupados lógicamente  
✅ Fácil encontrar qué necesitas  
✅ Mantenimiento centralizado  
✅ Reutilizable en pipelines  

---

## 🐳 INFRA/ - Toda la infraestructura

```
infra/
├── docker/
│   ├── compose/
│   │   ├── docker-compose.yml              # Main
│   │   ├── docker-compose.dev.yml
│   │   ├── docker-compose.qa.yml
│   │   ├── docker-compose.release.yml
│   │   └── docker-compose.main.yml
│   ├── env/
│   │   ├── .env.dev.example
│   │   ├── .env.qa.example
│   │   ├── .env.release.example
│   │   └── .env.main.example
│   └── scripts/
│       ├── build-images.sh
│       └── push-images.sh
├── kubernetes/                  # Future K8s manifests
│   ├── README.md
│   └── .gitkeep
├── prometheus/                  # Monitoring config
│   ├── prometheus.yml
│   └── alerts.yml
└── terraform/                   # Future IaC
    ├── README.md
    └── .gitkeep
```

**Ventajas:**
✅ Infraestructura visible  
✅ Config centralizada  
✅ Fácil de escalar  
✅ Ready para Kubernetes/Terraform  

---

## 🔄 CI-CD/ - Todas las pipelines

```
ci-cd/
├── Jenkinsfile                  # Jenkins pipeline
├── sonar-project.properties     # SonarQube config
├── .sonarcloud.properties       # Alternative sonar config
└── jenkins/
    ├── credentials-example.json # Template credenciales
    └── shared-libraries/        # Shared Jenkins libs
        └── .gitkeep
```

**Ventajas:**
✅ CI/CD centralizada  
✅ Config junto a pipeline  
✅ Fácil de mantener  
✅ Ready para escalabilidad  

---

## ⚙️ CONFIG/ - Configuraciones de aplicación

```
config/
├── default.yml                  # Default config
├── profiles/
│   ├── dev.yml
│   ├── qa.yml
│   ├── release.yml
│   └── main.yml
└── templates/
    └── .gitkeep
```

**Nota:** Spring Boot configs van en:  
`microservices/*/src/main/resources/application-*.yml`

**Esta carpeta es para:**
- Configuración centralizada (si aplica)
- Templates de configuración
- Overrides de defaults

---

## 🧪 TESTS/ - Tests organizados

```
tests/
├── e2e/                         # End-to-End tests
│   └── .gitkeep
├── integration/                 # Integration tests
│   └── .gitkeep
└── performance/                 # Performance tests
    └── .gitkeep
```

**Nota:** Unit tests van en cada microservicio:  
`microservices/*/src/test/`

Esta carpeta es para tests cross-cutting que no pertenecen a un solo servicio.

---

## 🏢 MICROSERVICES/ - Toda la lógica de negocio

```
microservices/
├── pom.xml                      # Parent POM
│
├── customer-microservice/
│   ├── pom.xml
│   ├── Dockerfile
│   ├── README.md
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/corhuila/microservices/customer_microservice/
│       │   │       ├── entity/
│       │   │       ├── dto/
│       │   │       ├── repository/
│       │   │       ├── service/
│       │   │       ├── controller/
│       │   │       ├── exception/
│       │   │       ├── config/
│       │   │       └── util/
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-qa.yml
│       │       ├── application-docker.yml
│       │       ├── application-release.yml
│       │       ├── application-main.yml
│       │       ├── messages.properties
│       │       └── logback-spring.xml
│       ├── test/
│       │   ├── java/
│       │   │   └── com/corhuila/.../
│       │   │       ├── controller/
│       │   │       ├── service/
│       │   │       └── integration/
│       │   └── resources/
│       │       ├── application-test.yml
│       │       └── test-data.sql
│       └── it/
│           └── java/            # Integration tests
│
├── product-microservice/        # Same pattern
├── cart-microservice/           # Same pattern
└── common-exceptions/
    ├── pom.xml
    ├── src/
    └── README.md
```

**Ventajas:**
✅ Cada servicio autónomo  
✅ Tests colocalizados con código  
✅ Estructura consistente  
✅ Fácil de replicar  

---

## 📊 BUILD/ LOGS/ TMP/ - Gitignored

```
build/                          # Build output (ignored)
logs/                           # Runtime logs (ignored)
tmp/                            # Temporary files (ignored)
target/                         # Maven output (ignored)
```

Estos directorios:
- ❌ NO van en Git
- ✅ Se crean automáticamente
- ✅ Son .gitignore'd
- ✅ Se limpian con `make clean`

---

## 🎯 CÓMO ORGANIZAR NUEVO CÓDIGO

### Agregando un nuevo microservicio

```
1. Crear carpeta: microservices/my-service/
2. Crear: pom.xml (copiar de customer)
3. Crear: Dockerfile (igual que otros)
4. Crear: src/main/java/.../
5. Crear: src/test/java/.../
6. Crear: src/main/resources/application-*.yml
7. Actualizar: microservices/pom.xml (agregar módulo)
8. Actualizar: ci-cd/Jenkinsfile (agregar stage)
```

### Agregando un utility script

```
1. Determinar categoría: setup? git? deployment? maintenance?
2. Crear: scripts/<category>/my-script.sh
3. Agregar: chmod +x scripts/<category>/my-script.sh
4. Actualizar: Makefile (si es frecuente)
```

### Agregando documentación

```
1. Crear: docs/MY_DOC.md
2. Actualizar: docs/INDEX.md (agregar referencia)
3. Link desde: README.md si es importante
```

---

## ✅ CHECKLIST: ANTES DE COMMITAR

### Archivos en RAÍZ (solo estos)
- [ ] README.md
- [ ] Makefile
- [ ] pom.xml
- [ ] .gitignore
- [ ] .editorconfig
- [ ] CONTRIBUTING.md
- [ ] CODE_OF_CONDUCT.md
- [ ] LICENSE
- [ ] VERSION
- [ ] CHANGELOG.md

### Documentación en DOCS/
- [ ] ✅ 6+ documentos
- [ ] ✅ INDEX.md actualizado
- [ ] ✅ Links internos correctos

### Scripts en SCRIPTS/
- [ ] ✅ Categorizados en carpetas
- [ ] ✅ Executable (chmod +x)
- [ ] ✅ Con comentarios

### CI/CD en CI-CD/
- [ ] ✅ Jenkinsfile aquí
- [ ] ✅ SonarQube properties aquí

### Infra en INFRA/
- [ ] ✅ Docker configs
- [ ] ✅ .env files
- [ ] ✅ Prometheus config

---

## 🚀 CÓMO EJECUTAR LA REORGANIZACIÓN

### Opción 1: Automática (Recomendada)

```bash
# Esto mueve todos tus archivos a lugares correctos
bash scripts/setup/reorganize-project.sh

# O mejor aún:
make reorganize-project
```

### Opción 2: Manual (Si prefieres control)

1. Leer `docs/PROJECT_STRUCTURE.md`
2. Mover archivos manualmente
3. Actualizar referencias en código
4. Commit: `git commit -m "refactor: reorganize project structure"`

---

## 📝 VENTAJAS DE ESTA ESTRUCTURA

### Para Developers
✅ Sé dónde está cada cosa  
✅ Estándar de industria  
✅ Fácil onboarding de nuevos devs  
✅ Menos "¿dónde está el X?"  

### Para DevOps
✅ Infraestructura separada  
✅ Scripts organizados  
✅ CI/CD separado  
✅ Fácil de escalar  

### Para Managers
✅ Proyecto profesional  
✅ Fácil para nuevos miembros  
✅ Documentación centralizada  
✅ Mejor mantenibilidad  

### Para Git
✅ Historia limpia  
✅ Menos conflictos  
✅ Mejor blame/history  
✅ Más fácil de revisar  

---

## 🔗 CONVENCIONES IMPORTANTES

### Naming Conventions

```
📁 Directories
  - snake_case (scripts, docker, infra)
  - UPPERCASE (README, CHANGELOG, LICENSE)
  
📄 Files
  - lowercase.sh (shell scripts)
  - PascalCase.java (Java files)
  - UPPERCASE.md (documentation)
  - lowercase.yml (configs)
  - .dotfiles (hidden configs)
```

### Commit Messages

```
refactor: reorganize project structure
  - Move documentation to docs/
  - Move scripts to scripts/
  - Move CI/CD to ci-cd/
  - Update .gitignore
```

### Branch Naming

```
feature/reorganize-structure
fix/path-references
docs/structure-guide
```

---

## ✨ RESULTADO FINAL

```
ANTES (Archivos sueltos):
❌ Jenkinsfile en root
❌ Docs variadas en root
❌ Scripts en root y scripts/
❌ Confuso y desordenado

DESPUÉS (Bien organizado):
✅ Todo categorizado
✅ Fácil de navegar
✅ Estándar de industria
✅ Professional y escalable
```

---

## 🎓 REFERENCIAS

- Google Java Style Guide
- Spring Boot Best Practices
- Maven Project Structure
- Industry Standards (Apache, Google, etc)

---

## 📞 DUDAS?

**¿Dónde va este archivo?**
→ Ver `docs/PROJECT_STRUCTURE.md` (este archivo)

**¿Cómo lo reorganizo?**
→ `make reorganize-project`

**¿Cómo instalo hooks nuevos?**
→ `make install-hooks`

**¿Afecta esto mi desarrollo?**
→ No. Solo cambio organización, todo funciona igual.

---

**Status:** ✅ Listo para ejecutar  
**Impact:** Organización y best practices  
**Time:** 5 minutos (automático)  
**Risk:** Ninguno (solo movimiento de files)  

**¡Vamos a organizarlo!** 🚀

```bash
make reorganize-project
```

