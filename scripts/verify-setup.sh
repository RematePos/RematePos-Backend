#!/bin/bash

# ============================================
# Setup Verification Script
# ============================================
# Validates all setup is ready for development
# Usage: bash scripts/verify-setup.sh

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

CHECKS_PASSED=0
CHECKS_FAILED=0
CHECKS_WARNING=0

# ============================================
# Helper Functions
# ============================================

check_pass() {
    echo -e "${GREEN}✅${NC} $1"
    ((CHECKS_PASSED++))
}

check_fail() {
    echo -e "${RED}❌${NC} $1"
    ((CHECKS_FAILED++))
}

check_warning() {
    echo -e "${YELLOW}⚠️${NC} $1"
    ((CHECKS_WARNING++))
}

# ============================================
# System Checks
# ============================================

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Setup Verification Script            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}\n"

echo -e "${BLUE}📋 System Checks${NC}"

# Git
if command -v git &> /dev/null; then
    check_pass "Git installed ($(git --version | cut -d' ' -f3))"
else
    check_fail "Git not installed"
fi

# Docker
if command -v docker &> /dev/null; then
    check_pass "Docker installed ($(docker --version | cut -d' ' -f3))"
else
    check_fail "Docker not installed"
fi

# Docker Compose
if command -v docker-compose &> /dev/null; then
    check_pass "Docker Compose installed"
elif docker compose version &> /dev/null; then
    check_pass "Docker Compose (plugin) installed"
else
    check_fail "Docker Compose not installed"
fi

# Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d' ' -f3 | tr -d '"')
    if [[ "$JAVA_VERSION" == *"21"* ]]; then
        check_pass "Java 21 installed ($JAVA_VERSION)"
    else
        check_warning "Java installed but not v21 ($JAVA_VERSION)"
    fi
else
    check_fail "Java not installed"
fi

# Maven
if command -v mvn &> /dev/null; then
    check_pass "Maven installed ($(mvn --version | head -1 | cut -d' ' -f3))"
elif [ -f "./mvnw" ]; then
    check_pass "Maven wrapper available (mvnw)"
else
    check_fail "Maven not installed"
fi

# Make
if command -v make &> /dev/null; then
    check_pass "Make installed"
else
    check_warning "Make not installed (optional, but recommended)"
fi

# ============================================
# Project Files Check
# ============================================

echo -e "\n${BLUE}📁 Project Files${NC}"

files_to_check=(
    "Jenkinsfile:CI/CD Pipeline"
    ".github/workflows/ci-cd.yml:GitHub Actions"
    "pom.xml:Root POM"
    "microservices/pom.xml:Microservices POM"
    "docs/SETUP_AND_DEPLOYMENT.md:Setup Documentation"
    "docs/QUICK_REFERENCE.md:Quick Reference"
    "docs/INDEX.md:Documentation Index"
    "sonar-project.properties:SonarQube Config"
    "Makefile:Make Commands"
    "scripts/pre-commit.sh:Pre-commit Hook"
    "jenkins-setup.sh:Jenkins Setup Script"
)

for file_spec in "${files_to_check[@]}"; do
    file="${file_spec%%:*}"
    desc="${file_spec##*:}"

    if [ -f "$file" ]; then
        check_pass "$desc ($file)"
    else
        check_fail "$desc ($file) - NOT FOUND"
    fi
done

# ============================================
# Environment Files Check
# ============================================

echo -e "\n${BLUE}🔐 Environment Files${NC}"

env_files=(
    "infra/docker/env/.env.dev.example"
    "infra/docker/env/.env.qa.example"
    "infra/docker/env/.env.release.example"
    "infra/docker/env/.env.main.example"
)

for env_file in "${env_files[@]}"; do
    if [ -f "$env_file" ]; then
        check_pass "$env_file exists"
    else
        check_fail "$env_file missing"
    fi
done

# Check for actual .env files (not required)
for profile in dev qa release main; do
    if [ -f "infra/docker/env/.env.$profile" ]; then
        check_pass ".env.$profile configured"
    else
        check_warning ".env.$profile not configured (create from example)"
    fi
done

# ============================================
# Configuration Files Check
# ============================================

echo -e "\n${BLUE}⚙️ Configuration Files${NC}"

config_files=(
    "microservices/customer-microservice/src/main/resources/application.yml"
    "microservices/customer-microservice/src/main/resources/application-qa.yml"
    "microservices/customer-microservice/src/main/resources/application-docker.yml"
    "microservices/customer-microservice/src/main/resources/application-release.yml"
    "microservices/customer-microservice/src/main/resources/application-main.yml"
    "microservices/customer-microservice/src/test/resources/application-test.yml"
)

for config_file in "${config_files[@]}"; do
    if [ -f "$config_file" ]; then
        check_pass "$(basename $config_file)"
    else
        check_fail "$(basename $config_file) missing"
    fi
done

# ============================================
# Dockerfile Check
# ============================================

echo -e "\n${BLUE}🐳 Dockerfiles${NC}"

dockerfiles=(
    "config-server/Dockerfile"
    "discovery-server/Dockerfile"
    "microservices/customer-microservice/Dockerfile"
    "microservices/product-microservice/Dockerfile"
    "microservices/cart-microservice/Dockerfile"
)

for dockerfile in "${dockerfiles[@]}"; do
    if [ -f "$dockerfile" ]; then
        check_pass "$(dirname $dockerfile) Dockerfile"
    else
        check_warning "$(dirname $dockerfile) Dockerfile missing (can be auto-generated)"
    fi
done

# ============================================
# Docker Status Check (Optional)
# ============================================

echo -e "\n${BLUE}🚀 Docker Status${NC}"

if command -v docker &> /dev/null; then
    if docker ps &> /dev/null; then
        check_pass "Docker daemon running"

        # Check if services are running
        if docker-compose -f infra/docker/compose/docker-compose.yml ps 2>/dev/null | grep -q "Up"; then
            check_pass "Some services running"
        else
            check_warning "No services currently running (run: make docker-up)"
        fi
    else
        check_warning "Docker daemon not running (start Docker)"
    fi
fi

# ============================================
# Maven Build Check
# ============================================

echo -e "\n${BLUE}🔨 Maven Status${NC}"

if [ -f "./mvnw" ]; then
    if ./mvnw --version &> /dev/null; then
        check_pass "Maven wrapper working"
    else
        check_fail "Maven wrapper not executable"
    fi

    # Check if dependencies cached
    if [ -d "$HOME/.m2/repository/com/corhuila" ]; then
        check_pass "Local Maven cache populated"
    else
        check_warning "No local Maven cache (will download on first build)"
    fi
fi

# ============================================
# Git Hooks Check
# ============================================

echo -e "\n${BLUE}🔗 Git Hooks${NC}"

if [ -f ".git/hooks/pre-commit" ]; then
    if grep -q "pre-commit.sh" ".git/hooks/pre-commit"; then
        check_pass "Pre-commit hook installed"
    else
        check_warning "Pre-commit hook exists but may not be correct"
    fi
else
    check_warning "Pre-commit hook not installed (run: make install-hooks)"
fi

# ============================================
# Final Summary
# ============================================

echo -e "\n${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Summary                               ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}\n"

echo -e "${GREEN}✅ Passed: $CHECKS_PASSED${NC}"
echo -e "${YELLOW}⚠️ Warnings: $CHECKS_WARNING${NC}"
echo -e "${RED}❌ Failed: $CHECKS_FAILED${NC}\n"

# ============================================
# Recommendations
# ============================================

if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All critical checks passed!${NC}\n"

    if [ $CHECKS_WARNING -gt 0 ]; then
        echo -e "${YELLOW}⚠️ Please address warnings above:${NC}"
        if [ ! -f "infra/docker/env/.env.dev" ]; then
            echo "   1. Setup .env files: cp infra/docker/env/.env.dev.example infra/docker/env/.env.dev"
        fi
        if ! grep -q "pre-commit.sh" ".git/hooks/pre-commit" 2>/dev/null; then
            echo "   2. Install hooks: make install-hooks"
        fi
        echo ""
    fi

    echo -e "${GREEN}Next steps:${NC}"
    echo "1. make docker-up           (Start local environment)"
    echo "2. make test                (Run tests)"
    echo "3. make health              (Check service health)"
    echo "4. make help                (See all commands)"
    echo ""

    exit 0
else
    echo -e "${RED}❌ Please fix the failures above before continuing.${NC}"
    echo -e "\n${YELLOW}Common fixes:${NC}"
    echo "- Install missing tools (Docker, Java, Maven)"
    echo "- Run from project root directory"
    echo "- Check file permissions"
    echo ""

    exit 1
fi

