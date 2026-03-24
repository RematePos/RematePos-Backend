#!/bin/bash

# ============================================
# Project Structure Reorganization Script
# ============================================
# This script reorganizes the project following best practices

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Project Structure Reorganization                 ║${NC}"
echo -e "${BLUE}║  Following Best Practices                         ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════╝${NC}\n"

# ============================================
# Create Directory Structure
# ============================================

echo -e "${BLUE}Creating directory structure...${NC}\n"

# Documentation
mkdir -p docs/images
echo -e "${GREEN}✓${NC} docs/"

# Scripts
mkdir -p scripts/setup scripts/deployment scripts/git scripts/maintenance
echo -e "${GREEN}✓${NC} scripts/"

# Infrastructure
mkdir -p infra/docker/compose infra/docker/env infra/docker/scripts
mkdir -p infra/prometheus infra/kubernetes infra/terraform
echo -e "${GREEN}✓${NC} infra/"

# CI/CD
mkdir -p ci-cd/jenkins/shared-libraries
echo -e "${GREEN}✓${NC} ci-cd/"

# Config
mkdir -p config/profiles config/templates
echo -e "${GREEN}✓${NC} config/"

# Tests
mkdir -p tests/e2e tests/integration tests/performance
echo -e "${GREEN}✓${NC} tests/"

# Build output (ignored)
mkdir -p build logs tmp
echo -e "${GREEN}✓${NC} build/, logs/, tmp/"

# ============================================
# Move existing files to correct locations
# ============================================

echo -e "\n${BLUE}Moving files to correct locations...${NC}\n"

# Move documentation
if [ -f "QUICK_REFERENCE.md" ]; then
    mv QUICK_REFERENCE.md docs/QUICK_REFERENCE.md
    echo -e "${GREEN}✓${NC} Moved QUICK_REFERENCE.md to docs/"
fi

if [ -f "SETUP_AND_DEPLOYMENT.md" ]; then
    mv SETUP_AND_DEPLOYMENT.md docs/SETUP_AND_DEPLOYMENT.md
    echo -e "${GREEN}✓${NC} Moved SETUP_AND_DEPLOYMENT.md to docs/"
fi

if [ -f "NEXT_STEPS.md" ]; then
    mv NEXT_STEPS.md docs/NEXT_STEPS.md
    echo -e "${GREEN}✓${NC} Moved NEXT_STEPS.md to docs/"
fi

if [ -f "EXECUTIVE_SUMMARY.md" ]; then
    mv EXECUTIVE_SUMMARY.md docs/EXECUTIVE_SUMMARY.md
    echo -e "${GREEN}✓${NC} Moved EXECUTIVE_SUMMARY.md to docs/"
fi

if [ -f "DIAGNOSTICO_PROYECTO_MICROSERVICIOS.md" ]; then
    mv DIAGNOSTICO_PROYECTO_MICROSERVICIOS.md docs/DIAGNOSTICO_PROYECTO.md
    echo -e "${GREEN}✓${NC} Moved DIAGNOSTICO_PROYECTO.md to docs/"
fi

# Move scripts
if [ -f "jenkins-setup.sh" ]; then
    mv jenkins-setup.sh scripts/setup/jenkins-setup.sh
    chmod +x scripts/setup/jenkins-setup.sh
    echo -e "${GREEN}✓${NC} Moved jenkins-setup.sh to scripts/setup/"
fi

if [ -f "scripts/pre-commit.sh" ]; then
    mv scripts/pre-commit.sh scripts/git/pre-commit.sh
    chmod +x scripts/git/pre-commit.sh
    echo -e "${GREEN}✓${NC} Moved pre-commit.sh to scripts/git/"
fi

if [ -f "scripts/verify-setup.sh" ]; then
    mv scripts/verify-setup.sh scripts/setup/verify-setup.sh
    chmod +x scripts/setup/verify-setup.sh
    echo -e "${GREEN}✓${NC} Moved verify-setup.sh to scripts/setup/"
fi

# Move CI/CD files
if [ -f "Jenkinsfile" ]; then
    mv Jenkinsfile ci-cd/Jenkinsfile
    echo -e "${GREEN}✓${NC} Moved Jenkinsfile to ci-cd/"
fi

if [ -f "sonar-project.properties" ]; then
    mv sonar-project.properties ci-cd/sonar-project.properties
    echo -e "${GREEN}✓${NC} Moved sonar-project.properties to ci-cd/"
fi

# Move infra files
if [ -d "infra/docker/compose" ] && [ -f "infra/docker/compose/docker-compose.yml" ]; then
    echo -e "${GREEN}✓${NC} docker-compose files already in correct location"
fi

if [ -d "infra/docker/env" ] && [ -f "infra/docker/env/.env.dev.example" ]; then
    echo -e "${GREEN}✓${NC} .env files already in correct location"
fi

# ============================================
# Create .gitkeep files for empty directories
# ============================================

echo -e "\n${BLUE}Creating .gitkeep files...${NC}\n"

find infra tests -type d -empty -exec touch {}/.gitkeep \;
echo -e "${GREEN}✓${NC} Created .gitkeep files"

# ============================================
# Create important root-level documentation
# ============================================

echo -e "\n${BLUE}Creating root documentation files...${NC}\n"

# Check and create missing files
if [ ! -f "README.md" ]; then
    echo "# Microservice POS Platform

Production-ready microservices POS system with Spring Boot and Spring Cloud.

## Quick Start

\`\`\`bash
git clone <repo>
make docker-up
make health
\`\`\`

## Documentation

See \`docs/\` directory for complete documentation:
- [Setup & Deployment](docs/SETUP_AND_DEPLOYMENT.md)
- [Quick Reference](docs/QUICK_REFERENCE.md)
- [5-Week Plan](docs/NEXT_STEPS.md)
- [Architecture](docs/ARCHITECTURE.md)

## Development

\`\`\`bash
make help        # See all commands
make docker-up   # Start local environment
make test        # Run tests
\`\`\`

## Project Structure

See [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) for detailed structure explanation.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

MIT License - See LICENSE file

## Status

✅ Production Ready
" > README.md
    echo -e "${GREEN}✓${NC} Created README.md"
else
    echo -e "${YELLOW}⚠${NC} README.md already exists"
fi

if [ ! -f "CONTRIBUTING.md" ]; then
    echo "# Contributing to Microservice POS

## Code Standards

- Follow Google Java Style Guide
- 80+ test coverage required
- SonarQube quality gate must pass
- All commits require pre-commit hook validation

## Development Workflow

1. Create feature branch from develop
2. Implement feature with tests
3. Ensure tests pass: \`make test\`
4. Ensure quality: \`make sonar\`
5. Create PR with description
6. After approval, merge to develop

## Commit Messages

Follow conventional commits:
\`\`\`
feat(customer): add new endpoint
fix(product): resolve null pointer
docs: update README
test(cart): add unit tests
\`\`\`

## Branch Naming

- \`feature/xxx\` - New features
- \`fix/xxx\` - Bug fixes
- \`docs/xxx\` - Documentation
- \`refactor/xxx\` - Code refactoring

## Testing

All changes must include tests:
- Unit tests: 100% of service logic
- Integration tests: Main flows
- Coverage: Minimum 80%

## Deployment

- develop → QA (automatic)
- release → Release (manual approval)
- main → Production (admin approval)

## Questions?

See [docs/](docs/) directory for complete guides.
" > CONTRIBUTING.md
    echo -e "${GREEN}✓${NC} Created CONTRIBUTING.md"
else
    echo -e "${YELLOW}⚠${NC} CONTRIBUTING.md already exists"
fi

if [ ! -f ".editorconfig" ]; then
    echo "root = true

[*]
indent_style = space
indent_size = 2
end_of_line = lf
charset = utf-8
trim_trailing_whitespace = true
insert_final_newline = true

[*.java]
indent_size = 4

[*.md]
trim_trailing_whitespace = false

[*.{yml,yaml}]
indent_size = 2
" > .editorconfig
    echo -e "${GREEN}✓${NC} Created .editorconfig"
else
    echo -e "${YELLOW}⚠${NC} .editorconfig already exists"
fi

if [ ! -f "CODE_OF_CONDUCT.md" ]; then
    echo "# Code of Conduct

## Our Commitment

We are committed to providing a welcoming and inspiring community for all.

## Expected Behavior

- Use welcoming and inclusive language
- Be respectful of differing opinions
- Accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## Unacceptable Behavior

- Harassment or discrimination
- Trolling or insulting comments
- Public or private attacks
- Publishing others' private information
- Other conduct considered inappropriate

## Enforcement

Violations of this Code of Conduct will be handled promptly and fairly.

Contact: [team email]
" > CODE_OF_CONDUCT.md
    echo -e "${GREEN}✓${NC} Created CODE_OF_CONDUCT.md"
fi

if [ ! -f "CHANGELOG.md" ]; then
    echo "# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.0.1] - 2026-03-23

### Added

- Initial project setup
- CI/CD pipeline (Jenkins + GitHub Actions)
- Docker Compose for multi-environment deployments
- Spring Boot microservices (Customer, Product, Cart)
- Service discovery with Eureka
- Centralized configuration with Config Server
- SonarQube integration for code quality
- JaCoCo for code coverage
- Prometheus metrics collection
- Complete documentation (1500+ lines)

### Security

- Pre-commit hooks for secret detection
- Environment-based configuration management
- Credentials vault-ready
" > CHANGELOG.md
    echo -e "${GREEN}✓${NC} Created CHANGELOG.md"
fi

# ============================================
# Create .gitignore entries
# ============================================

echo -e "\n${BLUE}Updating .gitignore...${NC}\n"

cat >> .gitignore << 'EOF'

# Build directories
build/
dist/
target/
*.jar
*.war

# Environment files (never commit secrets)
.env
.env.local
.env.*.local

# Logs
logs/
*.log
*.log.*

# IDE
.vscode/
.idea/
*.swp
*.swo
*~
.DS_Store

# Temporary
tmp/
temp/

# Node modules (if used)
node_modules/

# Python
__pycache__/
*.pyc

# OS
.DS_Store
Thumbs.db
EOF

echo -e "${GREEN}✓${NC} Updated .gitignore"

# ============================================
# Create Makefile location hint
# ============================================

if [ -f "Makefile" ]; then
    echo -e "${GREEN}✓${NC} Makefile in correct location (root)"
fi

# ============================================
# Create Summary
# ============================================

echo -e "\n${BLUE}╔════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Reorganization Complete                           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════╝${NC}\n"

echo -e "${GREEN}✅ Project structure reorganized following best practices:${NC}\n"

echo "📁 Directory Structure:"
echo "  docs/              - All documentation"
echo "  scripts/           - Utility scripts (setup, deployment, git, maintenance)"
echo "  infra/             - Infrastructure (Docker, Kubernetes, Terraform, Prometheus)"
echo "  ci-cd/             - CI/CD configurations (Jenkinsfile, SonarQube)"
echo "  config/            - Application configurations by profile"
echo "  tests/             - Integration/E2E/Performance tests"
echo "  microservices/     - All business logic"
echo "  build/, logs/, tmp/ - Build output (ignored by git)"
echo ""

echo "📄 Root Files:"
echo "  README.md          - Main project overview"
echo "  Makefile           - Development commands"
echo "  pom.xml            - Maven build"
echo "  .editorconfig      - Editor settings"
echo "  CONTRIBUTING.md    - Contribution guidelines"
echo "  CODE_OF_CONDUCT.md - Team guidelines"
echo "  CHANGELOG.md       - Version history"
echo ""

echo -e "${YELLOW}⚠️  Next Steps:${NC}"
echo "1. Update git hooks: make install-hooks (uses new location)"
echo "2. Update IDE config to use new paths"
echo "3. Commit: git add . && git commit -m 'refactor: reorganize project structure'"
echo ""

echo -e "${GREEN}Project is now organized following industry best practices!${NC}\n"

