# Threat Model

**Status:** Phase 0 draft  
**Date:** 2026-09-21  
**Method:** Asset and trust-boundary analysis informed by STRIDE

## Scope

This model covers the MVP web application, Spring Boot API and workers, PostgreSQL, Redis, GitHub App integration, webhook endpoint, vulnerability-data clients, and dependency/SBOM ingestion. CI/CD and maintainer workstation threats require separate operational reviews in Phase 1.

## Security Objectives

1. Prevent cross-organization access and privilege escalation.
2. Protect GitHub credentials, application secrets, and sensitive organization metadata.
3. Accept only authentic webhook deliveries and process each delivery idempotently.
4. Analyze hostile repository and SBOM content without executing it.
5. Preserve the integrity and provenance of findings, policies, and audit events.
6. Remain available under malformed, oversized, cyclic, or high-volume inputs.

## Protected Assets

- GitHub App private key, installation tokens, webhook secrets, and encryption keys
- User identities, sessions, organization memberships, and role assignments
- Private repository metadata, manifests, SBOMs, findings, events, and relationships
- Policy definitions, evaluation results, evidence, and finding status history
- Audit-event integrity and provider-delivery provenance
- Service availability and provider API quota

## Trust Boundaries

1. Browser to API: all client input and identifiers are untrusted.
2. GitHub to webhook endpoint: authenticity is unknown until raw-body signature verification succeeds.
3. Backend to GitHub/OSV: responses remain untrusted data even over authenticated TLS.
4. Repository/SBOM content to analyzers: content may be intentionally malicious.
5. Application to PostgreSQL/Redis: database credentials and organization scoping constrain access.
6. Operator configuration to runtime: configuration may be missing, weak, or accidentally exposed.

## Threat Actors

- Unauthenticated external attacker
- Authenticated user attempting cross-organization access or privilege escalation
- Malicious or compromised repository contributor
- Attacker holding a compromised GitHub installation credential
- Compromised or malicious upstream data source
- Misconfigured or malicious platform operator
- Dependency attacker targeting the platform's own build or runtime supply chain

## Threat Register

| ID | Threat | Impact | Required controls | Validation |
| --- | --- | --- | --- | --- |
| `TM-001` | Forged or replayed GitHub webhook | False events, findings, or state changes | Verify signature over raw bytes, reject before parsing, bind installation to organization, deduplicate delivery ID, limit body size | Invalid-signature, changed-body, replay, and oversized-body tests |
| `TM-002` | Cross-organization object access | Confidentiality breach and unauthorized modification | Derive organization scope from principal, enforce in service and query layers, avoid trusting request tenant alone | Integration tests for read/write IDOR across every resource family |
| `TM-003` | Role or membership escalation | Administrative takeover | Central authorization policy, restricted role assignment, last-owner safeguards, immutable audit record | Permission matrix and privilege-escalation tests |
| `TM-004` | GitHub credential disclosure | Repository or organization compromise | Encrypt at rest, minimize permissions, redact logs/errors, avoid browser exposure, rotate and revoke | Log scanning, secret handling tests, permission review |
| `TM-005` | Malicious manifest or SBOM parser input | Code execution, denial of service, corrupted analysis | No script/build execution, safe parsers, size/count/depth/time limits, isolated temporary storage, reject external entity resolution | Parser fuzzing and fixture tests for archives, cycles, deep trees, and entity expansion |
| `TM-006` | Spreadsheet/formula or stored script payload in evidence | Analyst compromise through stored XSS or export injection | Contextual output encoding, restrictive content types, sanitize export cells, Content Security Policy | Stored-XSS and formula-injection tests |
| `TM-007` | SSRF through repository links or advisory references | Internal network and metadata access | Fixed provider base URLs, allowlisted destinations, redirect restrictions, network egress policy | Private-address, redirect, and alternate-IP representation tests |
| `TM-008` | SQL or query injection | Data disclosure or modification | Parameterized persistence, allowlisted sort/filter fields, no raw user query fragments | Injection tests for filters, searches, and graph traversals |
| `TM-009` | Event or finding tampering | Incorrect investigations and hidden risk | Append-only event facts, status-history records, actor/time/source provenance, restricted correction workflow | Audit mutation and history-integrity tests |
| `TM-010` | Duplicate/out-of-order processing | Incorrect state and duplicate findings | Idempotency keys, source timestamps and sequence data, upsert/reconciliation semantics, transactional job state | Retry, concurrency, and reordered-event tests |
| `TM-011` | Resource exhaustion or API quota abuse | Platform outage and blocked synchronization | Pagination bounds, timeouts, concurrency limits, quotas, backoff, circuit breaking, per-organization rate limits | Load tests with oversized and high-cardinality inputs |
| `TM-012` | Vulnerability-feed poisoning or stale data | False positive/negative decisions | Source provenance, retrieval time, integrity checks where available, source precedence, last-known-good data | Conflicting-source and stale-feed tests |
| `TM-013` | Unsafe error or log content | Secret or private-data disclosure | Structured allowlisted fields, centralized error mapping, payload/token redaction, production stack-trace suppression | Automated secret canaries and error-response tests |
| `TM-014` | Compromised project dependency or build action | Platform or release compromise | Lock dependencies, dependency review, provenance, pinned CI actions, least-privilege workflow tokens, artifact scanning | CI policy checks and release verification |
| `TM-015` | Unauthorized active remediation | Repository or production modification | Advisory-only MVP, explicit authorization for later actions, scoped credentials, preview and audit trail | Tests proving no MVP workflow mutates connected repositories |

## Critical Abuse Cases

### Forged Webhook

An attacker sends a plausible GitHub event for another installation. The request must fail before payload parsing, delivery reservation, job creation, or any observable organization state change.

### Tenant Identifier Substitution

An authenticated member replaces a path or body organization ID with another organization's identifier. Authorization must use the authenticated membership and loaded resource ownership, returning no information that confirms the target exists.

### Hostile Repository Analysis

A repository includes package-manager hooks, decompression bombs, cyclic dependency data, huge manifests, or parser exploits. The platform must use bounded static parsing and must never invoke package installation or repository code.

### Secret Exfiltration Through Diagnostics

A provider returns a crafted error containing credentials or sensitive payload data. Logs and API errors must retain correlation identifiers while removing credentials, headers, payload bodies, and stack traces.

## Security Assumptions

- TLS terminates at a trusted reverse proxy or the application using a documented secure configuration.
- Operators protect host access, database backups, runtime secrets, and encryption keys.
- GitHub and advisory-source compromise cannot be prevented locally; provenance and least privilege reduce impact.
- PostgreSQL is authoritative and backed up; Redis data may be discarded without losing security records.
- The MVP does not clone and execute repositories or automatically alter connected repositories.

## Residual Risks and Open Decisions

| Risk or decision | Required Phase 0 outcome |
| --- | --- |
| Self-hosted key management varies by operator | Select an encrypted credential-storage design and document rotation/recovery |
| Authentication design is undecided | Choose session/token model and complete CSRF/XSS implications |
| Data retention is undecided | Define retention, deletion, evidence minimization, and backup behavior |
| GitHub permissions are undecided | Produce capability-to-permission matrix and degrade safely when access is absent |
| Parser libraries and formats are undecided | Select versions after security and resource-limit evaluation |
| Availability targets are undecided | Set input limits, queue limits, quotas, and recovery objectives |

## Review Triggers

Review this model whenever the system adds an integration, parser, authentication method, active remediation, new deployment topology, public upload endpoint, or material trust-boundary change. Every release should verify that critical threat controls have automated coverage.