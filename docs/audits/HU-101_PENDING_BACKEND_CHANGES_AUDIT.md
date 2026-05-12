# HU-101 Pending Backend Changes Audit

## 1. Audit Purpose

This document formalizes the audit of pending local backend changes detected in the main backend working tree before implementing new functionality.

The objective is to avoid mixing unrelated user stories and to separate pending changes safely into dedicated branches.

## 2. Repository State at Audit Time

- Repository analyzed: `RematePos-Backend-develop`
- Branch analyzed: `develop`
- Working tree status: dirty
- Approximate pending changes detected: 92 (`modified_like=48`, `added_like=36`, `untracked=29`)
- Last relevant baseline commits:
  - `5794f2e` `ci(HU-100): fix Docker Maven reactor build`
  - `6be9ac6` `ci(HU-099): normalize GHCR Docker image tags`
  - `a51bbcb` `ci(HU-098): fix backend SonarQube pipeline configuration`
  - `8e00335` `fix(HU-097): fix backend CI test failures`
  - `5a84b7a` `chore(HU-096): remove sensitive backend audit files from version control`

## 3. Risk Summary

- Functional changes from multiple user stories are mixed in one local working tree.
- A temporary `api-gateway` appears inside backend workspace and includes generated build artifacts.
- Generated artifacts detected in `api-gateway/target/` (including a large JAR).
- Root `package.json` exists with uncertain purpose in a Java backend repository context.
- Environment and Docker Compose changes are pending and should be separated from payment logic.
- Purchase and invoice logic changes are mixed with infrastructure and documentation updates.

## 4. Change Groups Overview

| Group | Scope observed | Type | Main risk | Suggested action |
|---|---|---|---|---|
| Documentation | `README.md`, `docs/*` | Documentation | Low | Separate into documentation-only branches when needed |
| Config Server | `config-server/src/main/resources/config/*.yml` | Config/Environment | Medium | Move to HU-082 branch and validate per environment |
| Docker and Environments | `infra/docker/*`, root compose files, compose overrides | Docker/Infrastructure | Medium | Move to HU-083 and HU-082 with split commits |
| Customer Microservice | customer model/repository/service/config updates | Functional | Medium | Review and separate to matching payment-flow HU if required |
| Product Microservice | category/product/cors updates | Functional | Medium | Isolate by use case, avoid mixing with infra |
| Invoice Microservice | new microservice files, service/controller/repository/dto/model/tests | Functional | High | Separate into HU-065 branch |
| Purchase Microservice | payment status, purchase flow, webhooks, returns, cash movement files | Functional | High | Split into HU-061/062/063/064/078 branches |
| Temporary API Gateway | `api-gateway/*` in backend repo | Functional/Infrastructure | High | Review ownership and move to HU-075 strategy |
| Generated/Non-versionable | `target/**`, large jars, temporary artifacts | Generated/Sensitive operational risk | Critical | Do not commit, enforce ignore rules |

## 5. HU Classification Table

| Suggested HU | Scope to isolate |
|---|---|
| HU-082 | Backend environment strategy (`config-server`, env templates) |
| HU-083 | Docker Compose backend orchestration and runtime docs |
| HU-075 | JWT and API Gateway integration concerns |
| HU-061 | Payment domain model and status definitions |
| HU-062 | Payment webhooks handling |
| HU-063 | Cash movement domain and persistence |
| HU-064 | Payment confirmation and sales state update flow |
| HU-065 | Invoice generation after approved payment |
| HU-078 | Returns, refunds, and exchange support |
| HU-066 | Payments/cash/invoicing documentation |

## 6. Files That Must Not Be Uploaded

- `api-gateway/target/**`
- `microservices/*/target/**`
- Real `.env` files
- Logs (`*.log`, `logs/**`)
- Dumps
- Backups
- Zip/rar packages
- Root `package.json` until purpose and ownership are confirmed

## 7. Recommended Work Order

Recommended execution order for separation and implementation:

1. HU-082
2. HU-083
3. HU-075
4. HU-061
5. HU-062
6. HU-063
7. HU-064
8. HU-065
9. HU-078
10. HU-066

This order prioritizes environment and infrastructure baseline before payment flow, invoice flow, returns, and final documentation.

## 8. Rules to Separate Changes Safely

- One HU per branch.
- Use conventional commits in English.
- Do not mix multiple microservices in one commit unless strictly required by contract coupling.
- Do not commit generated artifacts (`target/**`, logs, temporary files).
- Do not work directly on dirty `develop` for feature separation.
- Use clean worktrees from `origin/develop` for each HU separation branch.

## 9. Pending Security and Manual Review Items

- Confirm if temporary `api-gateway` content belongs in this repository or should be isolated.
- Review all new and modified `application.yml` and config files for accidental credentials.
- Confirm ownership and intent of root `package.json` before any commit.
- Validate Docker and environment examples do not expose real endpoints, tokens, or passwords.
- Execute per-HU test validation before integration to avoid regressions after split.
