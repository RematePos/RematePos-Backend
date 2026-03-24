#!/bin/bash

# ============================================
# Jenkins Setup Script for Microservice POS
# ============================================
# Usage: bash jenkins-setup.sh
# This script sets up Jenkins with required plugins and credentials

set -e

JENKINS_URL="${JENKINS_URL:-http://localhost:8080}"
JENKINS_USER="${JENKINS_USER:-admin}"
JENKINS_TOKEN="${JENKINS_TOKEN:-}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================
# Helper Functions
# ============================================

log_info() {
    echo -e "${BLUE}â„¹ï¸  $1${NC}"
}

log_success() {
    echo -e "${GREEN}âœ… $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}âš ï¸  $1${NC}"
}

log_error() {
    echo -e "${RED}âŒ $1${NC}"
    exit 1
}

# ============================================
# Checks
# ============================================

check_jenkins_running() {
    log_info "Checking Jenkins connection..."
    if curl -s -o /dev/null -w "%{http_code}" $JENKINS_URL | grep -q "200\|403"; then
        log_success "Jenkins is running"
    else
        log_error "Jenkins not accessible at $JENKINS_URL"
    fi
}

# ============================================
# Plugin Installation
# ============================================

install_plugins() {
    log_info "Installing required Jenkins plugins..."

    PLUGINS=(
        "pipeline:latest"
        "git:latest"
        "docker-plugin:latest"
        "docker-workflow:latest"
        "sonar:latest"
        "junit:latest"
        "jacoco:latest"
        "slack:latest"
        "email-ext:latest"
        "maven-plugin:latest"
        "timestamper:latest"
        "log-parser:latest"
        "htmlpublisher:latest"
        "simple-timestamper:latest"
        "github:latest"
        "credentials-binding:latest"
    )

    # Jenkins CLI approach (requires running Jenkins container)
    log_warning "Please install plugins manually through Jenkins UI:"
    echo "  1. Go to http://localhost:8080/manage/pluginManager/available"
    echo "  2. Search and install:"
    for plugin in "${PLUGINS[@]}"; do
        echo "     - $plugin"
    done
}

# ============================================
# Credentials Setup
# ============================================

create_credentials() {
    log_info "Creating Jenkins credentials..."

    cat << 'EOF'
Create the following credentials in Jenkins:
(Manage Jenkins â†’ Manage Credentials â†’ System â†’ Global credentials â†’ Add Credentials)

1. Docker Registry Credentials
   Type: Username with password
   ID: docker-registry-url
   Username: _json_key
   Password: (your Docker registry password)
   Description: Docker Registry Credentials

2. Docker Credentials
   Type: Secret text
   ID: docker-registry-credentials
   Secret: (your Docker credentials JSON)
   Description: Docker Registry Push Credentials

3. SonarQube Token
   Type: Secret text
   ID: sonar-token
   Secret: (your SonarQube authentication token)
   Description: SonarQube Token

4. SonarQube Host URL
   Type: Secret text
   ID: sonar-host-url
   Secret: http://sonarqube:9000
   Description: SonarQube Server URL

5. GitHub Token (optional)
   Type: Secret text
   ID: github-token
   Secret: (your GitHub personal access token)
   Description: GitHub Token
EOF
}

# ============================================
# Environment Variables Setup
# ============================================

create_env_vars() {
    log_info "Setting up environment variables..."

    cat << 'EOF'
Configure the following in Jenkins:
(Manage Jenkins â†’ Configure System â†’ Environment Variables)

DOCKER_REGISTRY=gcr.io/your-project-id
DOCKER_REGISTRY_URL=gcr.io
SONAR_HOST_URL=http://sonarqube:9000
MAVEN_SETTINGS=/var/jenkins_home/settings.xml
MAVEN_HOME=/usr/local/maven

For docker-compose deployments, also add:
DOCKER_COMPOSE_VERSION=v2.10.0
EOF
}

# ============================================
# Pipeline Creation
# ============================================

create_pipeline_job() {
    log_info "Instructions for creating pipeline job..."

    cat << 'EOF'

CREATE NEW PIPELINE JOB:
========================

1. Jenkins Home â†’ New Item
2. Name: microservice-pos
3. Type: Pipeline
4. Configure:

   Pipeline:
   - Definition: Pipeline script from SCM
   - SCM: Git
   - Repository URL: https://github.com/tu-org/microservice-pos.git
   - Credentials: (select GitHub credentials)
   - Branches:
     - */develop
     - */release
     - */main
   - Script Path: Jenkinsfile

   Build Triggers:
   - âœ“ GitHub hook trigger for GITscm polling
   - Poll SCM: H H * * * (check hourly)

   Properties:
   - âœ“ GitHub project: https://github.com/tu-org/microservice-pos/
   - Discard old builds:
     - Days to keep builds: 30
     - Max # of builds to keep: 50

5. Save

6. In GitHub repository settings:
   - Add webhook:
     - Payload URL: http://jenkins.example.com:8080/github-webhook/
     - Content type: application/json
     - Events: Just the push event
EOF
}

# ============================================
# Docker Agent Setup (Optional)
# ============================================

setup_docker_agent() {
    log_info "Instructions for Docker agent setup..."

    cat << 'EOF'

DOCKER AGENT SETUP (Optional):
=============================

1. Manage Jenkins â†’ Nodes and Clouds â†’ Configure Clouds
2. Add Docker Cloud:
   - Docker URL: unix:///var/run/docker.sock
   - Docker Host URI: unix:///var/run/docker.sock
   - Docker API Version: 1.41
   - Container Cap: 10

3. Docker Agent Templates:
   - Image: ubuntu:latest or gradle:latest
   - Run Container: âœ“
   - Always Pull Image: âœ“
   - Labels: docker
   - Retention Strategy: Keep This Agent Online

Then in Jenkinsfile use:
agent {
  docker {
    image 'maven:3.8.1-openjdk-21'
    args '-v /var/run/docker.sock:/var/run/docker.sock'
  }
}
EOF
}

# ============================================
# Webhook Setup
# ============================================

setup_webhook() {
    log_info "Instructions for webhook setup..."

    cat << 'EOF'

GITHUB WEBHOOK SETUP:
====================

1. Go to GitHub: Settings â†’ Webhooks â†’ Add webhook

2. Configure:
   - Payload URL: http://jenkins.example.com:8080/github-webhook/
   - Content type: application/json
   - Events: Let me select individual events
     âœ“ Pushes
     âœ“ Pull requests
   - Active: âœ“
   - Save

3. In Jenkins: Manage Jenkins â†’ Configure System â†’ GitHub
   - GitHub Server: Add GitHub Server
   - API URL: https://api.github.com
   - Credentials: (select GitHub token)
   - Test connection

4. Each repository:
   - Settings â†’ Webhooks â†’ Test delivery
   - Should return 200 OK
EOF
}

# ============================================
# Health Check
# ============================================

health_check() {
    log_info "Running health checks..."

    log_info "Checking Jenkins..."
    if curl -s $JENKINS_URL/api/json > /dev/null; then
        log_success "Jenkins API responding"
    else
        log_warning "Jenkins API not responding"
    fi

    log_info "Checking for plugins..."
    # Note: This would require authentication
    log_warning "Please verify plugins manually at $JENKINS_URL/manage/pluginManager/installed"
}

# ============================================
# Main Execution
# ============================================

main() {
    echo -e "${BLUE}â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—${NC}"
    echo -e "${BLUE}â•‘  Jenkins Setup for Microservice POS            â•‘${NC}"
    echo -e "${BLUE}â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•${NC}\n"

    check_jenkins_running

    install_plugins
    echo ""

    create_credentials
    echo ""

    create_env_vars
    echo ""

    create_pipeline_job
    echo ""

    setup_docker_agent
    echo ""

    setup_webhook
    echo ""

    health_check
    echo ""

    log_success "Jenkins setup completed!"
    echo -e "\n${BLUE}Next steps:${NC}"
    echo "1. Follow the instructions printed above"
    echo "2. Create credentials in Jenkins"
    echo "3. Create pipeline job"
    echo "4. Setup GitHub webhook"
    echo "5. Make a test commit to trigger build"
    echo ""
    echo "ðŸ“– For more info, see: docs/SETUP_AND_DEPLOYMENT.md"
}

# ============================================
# Run Main
# ============================================

main "$@"



