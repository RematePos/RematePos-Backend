# Project Structure - Best Practices Implemented

microservice-pos/
├── .github/                                    # GitHub specific files
│   ├── workflows/
│   │   └── ci-cd.yml                          # CI/CD pipeline
│   ├── ISSUE_TEMPLATE/                        # Issue templates
│   │   ├── bug_report.md
│   │   └── feature_request.md
│   └── pull_request_template.md                # PR template
│
├── .gitignore                                  # Git ignore (already exists)
├── .editorconfig                               # Editor configuration
├── .env.example                                # Example environment file
│
├── docs/                                       # All documentation
│   ├── INDEX.md                                # Documentation index
│   ├── SETUP_AND_DEPLOYMENT.md                 # Setup guide
│   ├── QUICK_REFERENCE.md                      # Command cheatsheet
│   ├── NEXT_STEPS.md                           # 5-week plan
│   ├── EXECUTIVE_SUMMARY.md                    # For stakeholders
│   ├── ARCHITECTURE.md                         # Architecture decisions
│   ├── CONTRIBUTING.md                         # Contribution guidelines
│   ├── SECURITY.md                             # Security guidelines
│   ├── TROUBLESHOOTING.md                      # Common issues
│   ├── API.md                                  # API documentation
│   └── images/                                 # Documentation images
│       └── architecture-diagram.png
│
├── scripts/                                    # Utility scripts
│   ├── setup/
│   │   ├── verify-setup.sh                     # Verify environment
│   │   ├── jenkins-setup.sh                    # Jenkins setup
│   │   └── local-setup.sh                      # Local dev setup
│   ├── deployment/
│   │   ├── deploy-qa.sh
│   │   ├── deploy-release.sh
│   │   └── deploy-prod.sh
│   ├── git/
│   │   └── pre-commit.sh                       # Pre-commit hook
│   └── maintenance/
│       ├── backup-db.sh
│       └── cleanup.sh
│
├── infra/                                      # Infrastructure files
│   ├── docker/
│   │   ├── compose/
│   │   │   ├── docker-compose.yml              # Main compose
│   │   │   ├── docker-compose.dev.yml
│   │   │   ├── docker-compose.qa.yml
│   │   │   ├── docker-compose.release.yml
│   │   │   └── docker-compose.main.yml
│   │   ├── env/
│   │   │   ├── .env.dev.example
│   │   │   ├── .env.qa.example
│   │   │   ├── .env.release.example
│   │   │   └── .env.main.example
│   │   └── scripts/
│   │       ├── build-images.sh
│   │       └── push-images.sh
│   ├── kubernetes/                            # Future K8s manifests
│   │   ├── README.md
│   │   └── .gitkeep
│   ├── prometheus/                            # Monitoring config
│   │   ├── prometheus.yml
│   │   └── alerts.yml
│   └── terraform/                             # Future IaC
│       ├── README.md
│       └── .gitkeep
│
├── ci-cd/                                      # CI/CD configurations
│   ├── Jenkinsfile                             # Jenkins pipeline
│   ├── sonar-project.properties                # SonarQube config
│   ├── .sonarcloud.properties
│   └── jenkins/
│       ├── credentials-example.json
│       └── shared-libraries/
│           └── .gitkeep
│
├── config/                                     # Application configurations
│   ├── default.yml                             # Default config
│   ├── profiles/
│   │   ├── dev.yml
│   │   ├── qa.yml
│   │   ├── release.yml
│   │   └── main.yml
│   └── templates/
│       └── .gitkeep
│
├── tests/                                      # Integration tests (if needed)
│   ├── e2e/
│   │   └── .gitkeep
│   ├── integration/
│   │   └── .gitkeep
│   └── performance/
│       └── .gitkeep
│
├── config-server/                              # Spring Config Server
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── target/
│
├── discovery-server/                           # Spring Eureka Server
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── target/
│
├── microservices/                              # All microservices
│   ├── pom.xml                                 # Parent POM
│   │
│   ├── customer-microservice/
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   ├── README.md
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/
│   │   │   │   │   └── com/corhuila/...
│   │   │   │   │       ├── entity/
│   │   │   │   │       ├── dto/
│   │   │   │   │       ├── repository/
│   │   │   │   │       ├── service/
│   │   │   │   │       ├── controller/
│   │   │   │   │       ├── exception/
│   │   │   │   │       ├── config/
│   │   │   │   │       └── util/
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       ├── application-qa.yml
│   │   │   │       ├── application-docker.yml
│   │   │   │       ├── application-release.yml
│   │   │   │       ├── application-main.yml
│   │   │   │       ├── messages.properties
│   │   │   │       └── logback-spring.xml
│   │   │   ├── test/
│   │   │   │   ├── java/
│   │   │   │   │   └── com/corhuila/...
│   │   │   │   │       ├── controller/
│   │   │   │   │       ├── service/
│   │   │   │   │       └── integration/
│   │   │   │   └── resources/
│   │   │   │       ├── application-test.yml
│   │   │   │       └── test-data.sql
│   │   │   └── it/
│   │   │       └── java/                      # Integration tests
│   │   └── .gitkeep
│   │
│   ├── product-microservice/
│   │   └── (mismo patrón)
│   │
│   ├── cart-microservice/
│   │   └── (mismo patrón)
│   │
│   └── common-exceptions/
│       ├── pom.xml
│       ├── src/
│       └── README.md
│
├── build/                                      # Build output (gitignored)
│   └── .gitkeep
│
├── logs/                                       # Application logs (gitignored)
│   └── .gitkeep
│
├── tmp/                                        # Temporary files (gitignored)
│   └── .gitkeep
│
├── Makefile                                    # Development commands
├── pom.xml                                     # Root POM
├── README.md                                   # Main project README
├── CHANGELOG.md                                # Version history
├── CODE_OF_CONDUCT.md                          # Team guidelines
├── LICENSE                                     # License file
├── CONTRIBUTING.md                             # Contributing guide
└── VERSION                                     # Version file

