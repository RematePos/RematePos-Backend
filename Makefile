.PHONY: help build test clean docker-up docker-down logs lint sonar deploy-qa deploy-release deploy-prod

# ============================================
# Makefile for Microservice POS
# ============================================
# Usage: make <target>
# Example: make build

.DEFAULT_GOAL := help

# Variables
MAVEN := ./mvnw
DOCKER_COMPOSE := docker compose
COMPOSE_DIR := infra/docker/compose
ENV_DIR := infra/docker/env
ENV ?= dev

# Colors
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
NC := \033[0m # No Color

# ============================================
# Help Target
# ============================================

help:
	@echo "$(BLUE)╔═══════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)║  Microservice POS - Makefile Commands            ║$(NC)"
	@echo "$(BLUE)╚═══════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(GREEN)BUILD & TEST$(NC)"
	@echo "  make build              Build all microservices"
	@echo "  make build-quick        Build without tests"
	@echo "  make test               Run all unit tests"
	@echo "  make test-coverage      Run tests with coverage report"
	@echo "  make clean              Clean build artifacts"
	@echo ""
	@echo "$(GREEN)DOCKER$(NC)"
	@echo "  make docker-build       Build Docker images"
	@echo "  make docker-up          Start one environment (ENV=dev|qa|release|main)"
	@echo "  make docker-up-dev      Start DEV environment"
	@echo "  make docker-up-qa       Start QA environment"
	@echo "  make docker-up-release  Start RELEASE environment"
	@echo "  make docker-up-main     Start MAIN environment"
	@echo "  make docker-up-all      Start all environments"
	@echo "  make docker-down        Stop one environment (ENV=dev|qa|release|main)"
	@echo "  make docker-down-all    Stop all environments"
	@echo "  make docker-logs        Show Docker logs (follow)"
	@echo "  make docker-ps          Show running containers"
	@echo "  make docker-clean       Stop environment and remove volumes"
	@echo "  make smoke              Run smoke endpoints (ENV=dev|qa|release|main|all)"
	@echo ""
	@echo "$(GREEN)CODE QUALITY$(NC)"
	@echo "  make lint               Run code linting"
	@echo "  make sonar              Run SonarQube analysis"
	@echo "  make format             Format code"
	@echo ""
	@echo "$(GREEN)DEPLOYMENT$(NC)"
	@echo "  make deploy-qa          Deploy to QA environment"
	@echo "  make deploy-release     Deploy to Release environment"
	@echo "  make deploy-prod        Deploy to Production (requires approval)"
	@echo ""
	@echo "$(GREEN)LOCAL DEVELOPMENT$(NC)"
	@echo "  make dev-setup          Setup local development environment"
	@echo "  make run-service        Run specific service (make run-service SERVICE=customer)"
	@echo "  make db-reset           Reset databases"
	@echo ""
	@echo "$(GREEN)UTILITIES$(NC)"
	@echo "  make health             Check health of running services"
	@echo "  make logs               Show service logs"
	@echo "  make version            Show project version"
	@echo "  make install-hooks      Install git pre-commit hooks"
	@echo ""

# ============================================
# BUILD & TEST
# ============================================

build:
	@echo "$(BLUE)Building all microservices...$(NC)"
	$(MAVEN) clean package -U

build-quick:
	@echo "$(BLUE)Building without tests...$(NC)"
	$(MAVEN) clean package -DskipTests

test:
	@echo "$(BLUE)Running tests...$(NC)"
	$(MAVEN) test

test-coverage:
	@echo "$(BLUE)Running tests with coverage...$(NC)"
	$(MAVEN) test
	@echo "$(GREEN)✅ Coverage report: target/site/jacoco/index.html$(NC)"

clean:
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	$(MAVEN) clean
	rm -rf target/
	@echo "$(GREEN)✅ Cleaned$(NC)"

# ============================================
# DOCKER
# ============================================

define compose_cmd
$(DOCKER_COMPOSE) -p pos-$(1) -f $(COMPOSE_DIR)/docker-compose.yml -f $(COMPOSE_DIR)/docker-compose.$(1).yml --env-file $(ENV_DIR)/.env.$(1)
endef

docker-build:
	@echo "$(BLUE)Building Docker images...$(NC)"
	docker build -t microservice-pos/config-server config-server/
	docker build -t microservice-pos/discovery-server discovery-server/
	docker build -t microservice-pos/customer-microservice microservices/customer-microservice/
	docker build -t microservice-pos/product-microservice microservices/product-microservice/
	docker build -t microservice-pos/cart-microservice microservices/cart-microservice/
	@echo "$(GREEN)✅ Docker images built$(NC)"

docker-up:
	@echo "$(BLUE)Starting $(ENV) environment...$(NC)"
	$(call compose_cmd,$(ENV)) up -d --build
	@echo "$(GREEN)✅ $(ENV) environment started$(NC)"

docker-up-qa:
	@$(MAKE) docker-up ENV=qa

docker-up-dev:
	@$(MAKE) docker-up ENV=dev

docker-up-release:
	@$(MAKE) docker-up ENV=release

docker-up-main:
	@$(MAKE) docker-up ENV=main

docker-up-all:
	@$(MAKE) docker-up ENV=dev
	@$(MAKE) docker-up ENV=qa
	@$(MAKE) docker-up ENV=release
	@$(MAKE) docker-up ENV=main

docker-down:
	@echo "$(BLUE)Stopping $(ENV) environment...$(NC)"
	$(call compose_cmd,$(ENV)) down
	@echo "$(GREEN)✅ $(ENV) environment stopped$(NC)"

docker-down-all:
	@$(MAKE) docker-down ENV=main
	@$(MAKE) docker-down ENV=release
	@$(MAKE) docker-down ENV=qa
	@$(MAKE) docker-down ENV=dev

docker-logs:
	@echo "$(BLUE)Showing $(ENV) environment logs...$(NC)"
	$(call compose_cmd,$(ENV)) logs -f

docker-ps:
	@echo "$(BLUE)Running containers for $(ENV):$(NC)"
	$(call compose_cmd,$(ENV)) ps

docker-clean:
	@echo "$(BLUE)Cleaning $(ENV) environment (remove volumes)...$(NC)"
	$(call compose_cmd,$(ENV)) down -v
	@echo "$(GREEN)✅ Cleaned$(NC)"

smoke:
	@echo "$(BLUE)Running smoke checks for ENV=$(ENV)...$(NC)"
	@powershell -ExecutionPolicy Bypass -File scripts/smoke-endpoints.ps1 -Environment $(ENV)

# ============================================
# CODE QUALITY
# ============================================

lint:
	@echo "$(BLUE)Running linting...$(NC)"
	@echo "$(YELLOW)Note: Full linting requires additional tools$(NC)"
	@echo "$(YELLOW)Run: make sonar$(NC)"

sonar:
	@echo "$(BLUE)Running SonarQube analysis...$(NC)"
	$(MAVEN) sonar:sonar \
		-Dsonar.projectKey=microservice-pos \
		-Dsonar.host.url=http://localhost:9000 \
		-Dsonar.login=admin
	@echo "$(GREEN)✅ Analysis complete$(NC)"
	@echo "$(YELLOW)Report: http://localhost:9000$(NC)"

format:
	@echo "$(BLUE)Formatting code...$(NC)"
	$(MAVEN) spotless:apply
	@echo "$(GREEN)✅ Code formatted$(NC)"

# ============================================
# DEPLOYMENT
# ============================================

deploy-qa:
	@echo "$(BLUE)Deploying to QA...$(NC)"
	$(call compose_cmd,qa) pull && \
	$(call compose_cmd,qa) up -d
	@sleep 5
	@$(MAKE) smoke ENV=qa
	@echo "$(GREEN)✅ QA deployment complete$(NC)"

deploy-release:
	@echo "$(RED)⚠️  RELEASE DEPLOYMENT REQUIRES APPROVAL$(NC)"
	@read -p "Continue? [y/N]: " confirm && [ "$$confirm" = "y" ] || exit 1
	@echo "$(BLUE)Deploying to Release...$(NC)"
	$(call compose_cmd,release) pull && \
	$(call compose_cmd,release) up -d
	@sleep 5
	@$(MAKE) smoke ENV=release
	@echo "$(GREEN)✅ Release deployment complete$(NC)"

deploy-prod:
	@echo "$(RED)🚨 PRODUCTION DEPLOYMENT - EXTRA CAUTION 🚨$(NC)"
	@read -p "Are you ABSOLUTELY sure? Type 'DEPLOY_PROD' to confirm: " confirm && [ "$$confirm" = "DEPLOY_PROD" ] || exit 1
	@echo "$(BLUE)Deploying to Production...$(NC)"
	$(call compose_cmd,main) pull && \
	$(call compose_cmd,main) up -d
	@sleep 10
	@$(MAKE) smoke ENV=main
	@echo "$(GREEN)✅ Production deployment complete$(NC)"

# ============================================
# LOCAL DEVELOPMENT
# ============================================

dev-setup:
	@echo "$(BLUE)Setting up development environment...$(NC)"
	@if [ ! -f "$(ENV_DIR)/.env.dev" ]; then \
		echo "$(YELLOW)Creating .env.dev...$(NC)"; \
		cp $(ENV_DIR)/.env.dev.example $(ENV_DIR)/.env.dev; \
	fi
	@if [ ! -f "$(ENV_DIR)/.env.local" ]; then \
		echo "$(YELLOW)Creating .env.local...$(NC)"; \
		cp $(ENV_DIR)/.env.dev.example $(ENV_DIR)/.env.local; \
	fi
	@echo "$(GREEN)✅ Development environment ready$(NC)"
	@echo "$(YELLOW)Next: make docker-up$(NC)"

run-service:
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(RED)Error: SERVICE not specified$(NC)"; \
		echo "Usage: make run-service SERVICE=customer"; \
		exit 1; \
	fi
	@echo "$(BLUE)Running $(SERVICE) microservice...$(NC)"
	$(MAVEN) -pl microservices/$(SERVICE)-microservice spring-boot:run

db-reset:
	@echo "$(YELLOW)Resetting databases...$(NC)"
	@read -p "Continue? [y/N]: " confirm && [ "$$confirm" = "y" ] || exit 1
	$(call compose_cmd,$(ENV)) down -v
	$(call compose_cmd,$(ENV)) up -d
	@echo "$(GREEN)✅ Databases reset$(NC)"

# ============================================
# UTILITIES
# ============================================

health:
	@$(MAKE) smoke ENV=$(ENV)

logs:
	@echo "$(BLUE)Showing logs (Ctrl+C to exit)...$(NC)"
	$(call compose_cmd,$(ENV)) logs -f

version:
	@$(MAVEN) -q -Dexec.executable="echo" -Dexec.args='$${project.version}' exec:exec

# ============================================
# Development shortcuts
# ============================================

.PHONY: restart
restart: docker-down docker-up
	@echo "$(GREEN)✅ Environment restarted$(NC)"

.PHONY: rebuild
rebuild: clean build docker-build docker-down docker-up
	@echo "$(GREEN)✅ Full rebuild complete$(NC)"

.PHONY: check
check: lint test
	@echo "$(GREEN)✅ All checks passed$(NC)"

# Reorganize project structure
.PHONY: reorganize-project
reorganize-project:
	@echo "$(BLUE)Reorganizing project structure...$(NC)"
	@bash scripts/setup/reorganize-project.sh
	@echo "$(GREEN)✅ Project reorganized$(NC)"

.PHONY: setup-hooks
install-hooks:
	@echo "$(BLUE)Installing git hooks...$(NC)"
	@chmod +x scripts/pre-commit.sh
	@cp scripts/pre-commit.sh .git/hooks/pre-commit
	@echo "$(GREEN)✅ Git hooks installed$(NC)"
