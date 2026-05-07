# Sensitive Files Audit - HU-096

## Purpose

This document records the HU-096 security audit for the RematePOS backend repository.

The goal is to identify sensitive, generated, or temporary files that were present in the current Git index or in previous Git history, remove them from current version control when possible, and define the follow-up actions required before any history rewrite.

No full secret values are documented here.

## Summary Of Finding

The audit detected that local troubleshooting artifacts were committed under:

- `.copilot_tmp_db_audit/**`

This folder contained generated audit files, environment snapshots, compose snapshots, execution logs, smoke-test output, Liquibase output, temporary SQL/XML/YAML copies, and local runtime reports.

Some files contained password-related keys or environment variable names. Because these files were already tracked and are present in Git history, this must be treated as a security incident until the team confirms whether the values were only placeholders or real credentials.

## Affected Paths

Current tracked files removed from version control:

- `.copilot_tmp_db_audit/**`

Sensitive or generated file patterns reinforced in `.gitignore`:

- `.copilot_tmp_db_audit/`
- `.env`
- `.env.*`
- `logs/`
- `*.log`
- `hs_err_pid*.log`
- `target/`
- `*.dump`
- `*.backup`
- `*.bak`
- `*.tmp`
- `*.zip`
- `*.rar`
- `.DS_Store`
- `Thumbs.db`

Historical paths detected during the audit:

- `.copilot_tmp_db_audit/**`
- `infra/docker/env/.env.dev`
- `infra/docker/env/.env.qa`
- `infra/docker/env/.env.main`
- `infra/docker/env/.env.release`
- `logs/product-microservice.log`
- `microservice-pos/logs/product-microservice.log`

## Risk

Risk level: Critical.

Reasons:

- Files were already tracked by Git.
- Some files were generated from local execution or environment snapshots.
- Some files may include database credential values or runtime details.
- Removing files in the latest commit does not remove them from Git history.
- Any real credentials that appeared in history must be considered exposed.

## Actions Applied In HU-096

- Strengthened backend `.gitignore` rules for sensitive, temporary, generated, and compressed files.
- Removed `.copilot_tmp_db_audit/**` from the current Git index using `git rm --cached`.
- Preserved local files on disk; no local files were physically deleted.
- Added this audit document under `docs/security/`.
- Did not rewrite Git history.
- Did not run `git filter-repo`.
- Did not run BFG Repo-Cleaner.
- Did not force push.
- Did not modify microservice business logic.

## Related Pull Requests Detected

- RematePos-Backend PR #3: merged platform maturity PR that previously included generated/log artifacts.
- RematePos-Backend PR #4: merged CI/bootstrap hygiene PR using GitHub secret references.
- RematePos-Backend PR #6: open draft CI deploy PR using GitHub secret references.
- RematePos-api PR #1: open draft API Gateway PR with safe `.env.example`.
- RematePos-bd PR #1: open safe database cleanup PR with `.gitignore` and `.env.example`.

## Pending Actions

- Review whether the historical credential-looking values were real or placeholders.
- Merge this HU-096 branch only after review.
- Do not delete local troubleshooting files until the team confirms they are no longer needed.
- Do not force push without explicit approval from the team.
