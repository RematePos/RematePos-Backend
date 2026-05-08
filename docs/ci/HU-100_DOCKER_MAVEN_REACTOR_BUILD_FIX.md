# HU-100 Docker Maven Reactor Build Fix

## Purpose

This document explains the Docker build failure for `customer-microservice` and the fix applied for Maven multimodule builds in the backend CI/CD pipeline.

## Failure Summary

After HU-099 was merged into `develop`, GHCR image names were normalized correctly and Docker Buildx no longer failed because of uppercase repository names.

The next failure happened inside the Docker build:

```text
Build and push Docker images: failure

Dockerfile:6
RUN mvn clean package -DskipTests -pl microservices/customer-microservice -am

Non-resolvable parent POM for com.corhuila.microservices:customer-microservice:
Could not find artifact com.corhuila.microservices:microservices:pom:0.0.1-SNAPSHOT
and 'parent.relativePath' points at wrong local POM
```

## Root Cause

The workflow used this Docker build context:

```yaml
context: microservices/customer-microservice
```

That context only made the customer microservice directory available inside Docker.

However, `customer-microservice` is part of a Maven reactor:

```text
pom.xml
microservices/pom.xml
microservices/customer-microservice/pom.xml
microservices/common-exceptions/pom.xml
```

The `customer-microservice` POM has a parent declared with:

```xml
<relativePath>../pom.xml</relativePath>
```

When Docker only received `microservices/customer-microservice` as context, the parent `microservices/pom.xml` was not available. Maven therefore could not resolve the reactor parent POM.

## Docker Build Context vs Dockerfile Path

The Docker build context defines which files Docker can copy during image build.

The Dockerfile path only tells Docker which Dockerfile to execute.

For this project, the correct setup is:

```yaml
context: .
file: microservices/customer-microservice/Dockerfile
```

This allows the Dockerfile to access the root POM, the Maven reactor POM and the required microservice modules.

## Solution Applied

The workflow now builds from the repository root context while keeping the customer Dockerfile:

```yaml
context: .
file: microservices/customer-microservice/Dockerfile
```

The customer Dockerfile now copies the Maven reactor files explicitly:

```dockerfile
COPY pom.xml pom.xml
COPY microservices/pom.xml microservices/pom.xml
COPY microservices/cart-microservice/pom.xml microservices/cart-microservice/pom.xml
COPY microservices/common-exceptions/pom.xml microservices/common-exceptions/pom.xml
COPY microservices/customer-microservice/pom.xml microservices/customer-microservice/pom.xml
COPY microservices/product-microservice/pom.xml microservices/product-microservice/pom.xml

COPY microservices/common-exceptions/src microservices/common-exceptions/src
COPY microservices/customer-microservice/src microservices/customer-microservice/src
```

The Docker build compiles only the required reactor path:

```bash
mvn -f microservices/pom.xml clean package -DskipTests -pl customer-microservice -am
```

This builds:

- `microservices`
- `common-exceptions`
- `customer-microservice`

## Relation With HU-099

HU-099 fixed GHCR image tag generation by normalizing the Docker image namespace to lowercase.

HU-100 keeps that behavior unchanged and only fixes the Maven reactor build inside Docker.

The expected image namespace remains:

```text
ghcr.io/rematepos/rematepos-backend/customer-microservice:<commit-sha>
```

## Validation

Validation performed or expected:

1. Workflow YAML syntax remains valid.
2. Maven reactor build works with:

```powershell
.\microservices\customer-microservice\mvnw.cmd -f microservices\pom.xml clean package -DskipTests -pl customer-microservice -am
```

3. Docker build should be validated with:

```bash
docker build -f microservices/customer-microservice/Dockerfile -t rematepos-customer-test .
```

4. GitHub Actions must validate the real Docker Buildx and GHCR publishing flow after the PR is opened and again after merge to `develop`.

## Scope Control

This HU does not modify:

- Production business logic.
- Controllers.
- Services.
- Repositories.
- DTOs.
- Entities.
- Migrations.
- Tests from HU-097.
- Security files from HU-096.
- SonarQube behavior from HU-098.
- GHCR lowercase namespace behavior from HU-099.

## Pending Work

- Apply the same Docker/Maven reactor pattern to other backend microservices when they are added to image publishing.
- Review whether Docker image publishing should run on all protected branches or only selected deployment branches.
- Review the Node.js 20 deprecation warning in a separate CI maintenance user story.
