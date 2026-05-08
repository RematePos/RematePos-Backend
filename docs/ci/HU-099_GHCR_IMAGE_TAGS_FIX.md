# HU-099 GHCR Image Tags Fix

## Purpose

This document explains the backend CI/CD pipeline failure related to Docker Buildx and GitHub Container Registry image names.

## Failure Summary

After HU-098 was merged into `develop`, SonarQube no longer blocked the pipeline when its configuration was missing. The next failure happened in the Docker Buildx image build step.

The reported error was:

```text
buildx failed with: ERROR: failed to build: invalid tag "ghcr.io/RematePos/RematePos-Backend/customer-microservice:a51bbcbd31c7e0c569564c7edc52f5a48b77e601": repository name must be lowercase
```

## Root Cause

The workflow used `${{ github.repository }}` to build the GHCR image path.

For this repository, that value resolves to:

```text
RematePos/RematePos-Backend
```

Docker image repository names must be lowercase, so Docker Buildx rejected the generated image tag.

## Expected Image Namespace

The normalized GHCR namespace must resolve to:

```text
ghcr.io/rematepos/rematepos-backend
```

The customer microservice image must therefore be published as:

```text
ghcr.io/rematepos/rematepos-backend/customer-microservice:<commit-sha>
```

## Solution Applied

The workflow now includes a dedicated step that normalizes the image namespace:

```bash
IMAGE_NAMESPACE=$(echo "${REGISTRY}/${GITHUB_REPOSITORY}" | tr '[:upper:]' '[:lower:]')
echo "image_namespace=$IMAGE_NAMESPACE" >> "$GITHUB_OUTPUT"
```

Docker metadata and Docker Buildx now use:

```text
${{ steps.image-vars.outputs.image_namespace }}/customer-microservice
```

This keeps the namespace derived from the GitHub repository while making it safe for GHCR and Docker.

## Services Affected

The current workflow builds and pushes:

- `customer-microservice`

If additional backend microservices are added to the Docker publishing step later, they must use the same lowercase namespace output.

Expected future examples:

```text
ghcr.io/rematepos/rematepos-backend/product-microservice:<commit-sha>
ghcr.io/rematepos/rematepos-backend/cart-microservice:<commit-sha>
ghcr.io/rematepos/rematepos-backend/purchase-microservice:<commit-sha>
ghcr.io/rematepos/rematepos-backend/invoice-microservice:<commit-sha>
```

## Validation

Validation expected for HU-099:

1. The workflow YAML remains valid.
2. The generated namespace is lowercase.
3. Docker metadata uses the lowercase namespace.
4. Docker Buildx uses the lowercase namespace.
5. No production code, tests, migrations, secrets or environment files are changed.

Local string validation:

```bash
echo "ghcr.io/RematePos/RematePos-Backend" | tr '[:upper:]' '[:lower:]'
```

Expected output:

```text
ghcr.io/rematepos/rematepos-backend
```

## Relation With HU-098

HU-098 fixed the SonarQube configuration failure by making the analysis conditional when the required configuration is missing.

HU-099 is separate and only fixes Docker/GHCR image tag generation. It does not change the SonarQube behavior added in HU-098.

## Pending Work

- Review whether Docker image publishing should run on `develop`, `qa`, `release` and `main`, or only selected branches.
- Review the Node.js 20 deprecation warning in a separate CI maintenance user story.
- Add the same lowercase image namespace pattern if more backend microservices are published in the workflow.
