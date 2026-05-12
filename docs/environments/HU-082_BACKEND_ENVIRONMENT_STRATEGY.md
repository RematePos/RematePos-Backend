# HU-082 Backend Environment Strategy

## Purpose

HU-082 defines a safe backend environment strategy for DEV, QA, and MAIN before continuing payment, cash register, invoice, returns, or API Gateway feature work.

This user story is configuration and documentation only.

## Scope

- Config Server placeholders and environment-driven values.
- Backend `.env.example` templates.
- Environment documentation for DEV, QA, and MAIN.
- Rules to avoid committing real credentials.

Out of scope for HU-082:

- Functional business logic.
- Payment flow implementation.
- Cash movement logic.
- Invoice generation logic.
- Returns/refunds logic.
- API Gateway functional behavior.

## Environment Strategy

### DEV

- Intended for local integration and developer testing.
- Uses safe example placeholders and local host mappings as needed.
- Real developer secrets must be kept in non-versioned `.env` files.

### QA

- Intended for validation before release.
- Uses dedicated QA ports, DB names, and non-production credentials.
- Secrets must come from secure environment provisioning, not Git.

### MAIN

- Intended for production deployment.
- Uses explicit hostnames and credential placeholders only in example files.
- Real values must be managed by secret manager or deployment platform variables.

## Variables by Service Group

- Platform: Config Server, Eureka.
- MongoDB-backed services: Customer, Cart.
- PostgreSQL-backed services: Product.
- Shared connection variables: DB host, port, username, password, DB name.

Config Server should resolve values through environment placeholders and avoid hardcoded passwords.

## Rules for Real `.env` Files

- Do not version `.env` real files.
- Version only `.env.example` templates with safe placeholders.
- Never commit real tokens, passwords, API keys, or private URLs.

## Relation With Docker Compose

HU-082 provides environment variable baseline and placeholders.

HU-083 will consume this baseline to finalize Docker Compose behavior by environment.

## Relation With Future VPS/Linux Deployment

The same variable contract should be usable in VPS/Linux deployments by injecting real values at runtime through deployment environment variables.

## Validation Checklist (No Secrets)

- Search for hardcoded credentials in config and examples.
- Confirm only placeholders exist in versioned templates.
- Confirm no real `.env` files were staged.
- Confirm no generated artifacts (`target`, logs, dumps, backups, archives) were staged.

## Pending Items for HU-083

- Final Docker Compose profiles per environment.
- Runtime network/service naming conventions.
- Production-ready deployment variable injection process.
