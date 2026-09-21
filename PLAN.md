# Security Intelligence & Software Supply-Chain Platform

## Project Plan

---

# 1. Project Overview

## Working Description

An open-source security intelligence and software supply-chain platform that helps organizations understand the security state of their software environment.

The platform collects information from development and deployment environments, normalizes it, connects related assets and events, evaluates security posture, identifies vulnerabilities and risks, and eventually provides actionable remediation.

The project combines two major areas:

1. Software Supply-Chain Intelligence
2. Organizational Security Posture & Audit Intelligence

These are connected through a unified security relationship model.

---

# 2. Problem Statement

Modern organizations have many repositories, dependencies, CI/CD workflows, artifacts and deployments.

They also have security controls, users, permissions and security events distributed across multiple systems.

Existing tools often solve individual problems:

* vulnerability scanning
* SBOM generation
* secret detection
* repository security
* audit logging
* dependency analysis
* cloud monitoring

The problem is that these systems are often disconnected.

A security team may know:

> "This package has a critical vulnerability."

But still need to manually determine:

* which repositories use it
* whether the dependency is direct or transitive
* whether the affected application is deployed
* which production service uses it
* when the vulnerable version was introduced
* which build produced the artifact
* who owns the repository
* what remediation is available

Similarly, an organization may know:

> "Branch protection was disabled."

But may not immediately know:

* who changed it
* whether the repository is production-critical
* whether a deployment happened afterward
* whether the change violated organizational policy

The project addresses this fragmentation.

---

# 3. Project Goal

Build an open-source platform that provides a unified security intelligence layer for an organization's software environment.

The platform should eventually allow users to understand:

```text
What do we have?
        ↓
How is it connected?
        ↓
What is vulnerable?
        ↓
What security controls are missing?
        ↓
What changed?
        ↓
What does the change affect?
        ↓
What should we fix?
```

---

# 4. Core Concept

The central technical concept is the:

## Security Graph

The platform represents relationships between:

* users
* teams
* repositories
* dependencies
* packages
* vulnerabilities
* SBOMs
* builds
* artifacts
* deployments
* services
* infrastructure
* policies
* findings
* events

Example:

```text
User
 │
 └── member of
       ↓
     Team
       │
       └── owns
             ↓
         Repository
             │
             ├── depends on → Package
             │                     │
             │                     └── affected by → CVE
             │
             └── builds → Artifact
                              │
                              └── deployed to → Production
```

This graph allows the platform to perform contextual security analysis.

---

# 5. Primary Functional Areas

## 5.1 Repository Inventory

Initial GitHub integration should collect:

* repositories
* repository visibility
* owners
* teams
* branches
* releases
* workflows
* relevant security settings

---

## 5.2 Security Posture

Evaluate repositories for security controls.

Initial controls:

* branch protection
* required pull-request reviews
* CODEOWNERS
* SECURITY.md
* secret scanning
* dependency scanning
* GitHub Actions permissions
* repository visibility

Example:

```text
payments-api

Branch protection       PASS
CODEOWNERS              FAIL
Secret scanning         PASS
Dependency scanning     PASS
SECURITY.md             FAIL
Actions permissions     WARN
```

---

# 6. Software Supply-Chain Intelligence

The system should understand the relationship between applications and their dependencies.

Initial supported ecosystems:

* Python
* Java
* JavaScript/TypeScript

Future ecosystems can include:

* Go
* Rust
* C/C++
* PHP
* Ruby
* .NET

---

# 7. SBOM

The platform should support Software Bills of Materials.

Standards:

* CycloneDX
* SPDX

The platform should be capable of:

* generating SBOM information
* importing SBOMs
* storing SBOM metadata
* connecting SBOM components to repositories
* connecting components to vulnerabilities

---

# 8. Vulnerability Intelligence

Use existing vulnerability sources instead of maintaining an independent vulnerability database.

Potential sources:

* OSV
* GitHub Advisory Database
* NVD
* ecosystem package metadata

The system should correlate:

```text
Package
+
Version
+
Ecosystem
+
Vulnerability
```

and determine whether the organization is affected.

---

# 9. Dependency Analysis

The dependency system should distinguish:

### Direct dependency

```text
Application
   ↓
Library A
```

### Transitive dependency

```text
Application
   ↓
Library A
   ↓
Library B
   ↓
Vulnerable Library C
```

The platform should preserve the dependency path.

This enables impact analysis.

---

# 10. Security Findings

All security issues should be normalized into a common finding model.

Example:

```text
Finding ID:
F-1042

Type:
VULNERABLE_DEPENDENCY

Severity:
HIGH

Repository:
payments-api

Package:
example-lib

Current:
2.1.0

Fixed:
2.1.4

Production:
YES

Status:
OPEN
```

Other finding types:

* VULNERABLE_DEPENDENCY
* EXPOSED_SECRET
* SECURITY_POLICY_VIOLATION
* CONFIGURATION_RISK
* LICENSE_VIOLATION
* SUPPLY_CHAIN_RISK
* PROVENANCE_ISSUE

Additional types can be added later.

---

# 11. Audit & Event Intelligence

Collect security-relevant events from integrations.

Initial GitHub events may include:

* repository creation
* repository deletion
* repository visibility changes
* permission changes
* branch protection changes
* workflow changes
* release events
* deployment events
* security configuration changes

Each event should contain:

```text
Actor
Timestamp
Source
Action
Resource
Previous state
New state
Metadata
```

Where previous/new state is available.

---

# 12. Security Timeline

The system should eventually correlate events.

Example:

```text
19:41
Branch protection disabled

19:43
Code pushed

19:45
GitHub Action executed

19:46
Production deployment

19:48
Cloud resource changed
```

The goal is to provide useful investigation context rather than simply displaying raw logs.

---

# 13. Policy Engine

Organizations should eventually be able to define policies.

Example:

```yaml
production:
  require_branch_protection: true
  require_codeowners: true
  require_security_policy: true
  max_critical_vulnerabilities: 0
```

Policies should produce deterministic results.

Every policy failure should explain:

* what failed
* why it failed
* affected resource
* evidence
* remediation recommendation

---

# 14. Remediation

Initial remediation should be advisory.

Examples:

```text
Upgrade package X from 2.1.0 to 2.1.4
```

```text
Enable branch protection
```

```text
Require two reviewers
```

Later:

* automated GitHub issues
* pull requests
* configuration patches
* dependency update PRs

Potentially much later:

* credential rotation
* infrastructure remediation

Destructive or high-impact actions must require explicit authorization.

---

# 15. Technology Stack

## Frontend

* React
* TypeScript

Responsibilities:

* dashboard
* repository views
* security findings
* posture
* dependency visualization
* SBOM
* audit timeline
* graph visualization
* remediation interface

---

## Backend

### Java

### Spring Boot

Responsibilities:

* REST API
* authentication
* authorization
* organization management
* integrations
* security engine
* posture engine
* policy engine
* finding engine
* event processing
* graph management
* remediation orchestration

---

## Database

### PostgreSQL

Initial database.

Use it for:

* relational application data
* security findings
* events
* dependency relationships
* assets
* graph relationships

A separate graph database is NOT part of the MVP.

---

## Cache / Queue

### Redis

Potential uses:

* caching
* background jobs
* temporary state
* rate limiting
* job coordination

Introduce only where required.

---

## Deployment

### Docker

Primary development and self-hosted deployment method.

### Docker Compose

Initial complete local deployment.

Future:

### Kubernetes

For production-scale/self-hosted deployments.

---

## CI/CD

GitHub Actions.

CI should perform:

* compilation
* tests
* static analysis
* dependency checks
* security checks
* Docker image build
* integration tests where appropriate

---

# 16. Architecture

The initial architecture will be a:

## Modular Monolith

Not microservices.

High-level:

```text
                    React
                      │
                      ▼
               Spring Boot API
                      │
        ┌─────────────┼─────────────┐
        ▼             ▼             ▼
    Security      Supply Chain     Audit
     Modules         Modules       Modules
        │             │             │
        └─────────────┼─────────────┘
                      ▼
                  PostgreSQL
                      │
                    Redis
```

Suggested modules:

```text
auth
organization
user
team
repository
integration
dependency
sbom
vulnerability
posture
policy
finding
event
audit
graph
notification
remediation
```

---

# 17. Why Modular Monolith?

Microservices are intentionally postponed.

Reasons:

* simpler development
* easier debugging
* easier local setup
* simpler deployment
* lower infrastructure requirements
* easier contribution for open-source developers
* fewer distributed-system problems

Modules should have clear boundaries.

If a module later requires independent scaling or deployment, it can be extracted.

---

# 18. GitHub Integration

The first external integration will be GitHub.

Prefer:

## GitHub App

instead of personal access tokens.

The integration should support:

* repository discovery
* organization information
* permissions
* security settings
* webhooks
* dependency information where available
* workflow information
* release/deployment information

Webhook authenticity must be validated.

GitHub credentials must be stored securely.

---

# 19. Security Architecture

Security is a core requirement.

The platform itself must use:

* secure authentication
* RBAC
* least privilege
* input validation
* output validation
* secure secret storage
* webhook signature validation
* rate limiting where appropriate
* audit logging
* secure headers
* database access controls
* safe error handling
* structured logging without secrets

Never log:

* passwords
* access tokens
* API keys
* private keys
* credentials

---

# 20. Authentication & Authorization

Initial system should support:

* user authentication
* organization membership
* roles
* permissions

Possible roles:

```text
OWNER
ADMIN
SECURITY_ANALYST
DEVELOPER
VIEWER
```

The exact RBAC model should be finalized during implementation.

---

# 21. Repository Structure

Initial repository:

```text
project/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── README.md
│
├── docs/
│
├── deployments/
│   └── docker-compose/
│
├── scripts/
│
├── tests/
│
├── .github/
│   ├── workflows/
│   ├── ISSUE_TEMPLATE/
│   └── pull_request_template.md
│
├── README.md
├── ARCHITECTURE.md
├── CONTRIBUTING.md
├── DEVELOPMENT.md
├── SECURITY.md
├── CODE_OF_CONDUCT.md
├── LICENSE
└── PLAN.md
```

The exact structure may evolve.

---

# 22. MVP

The first usable MVP should provide:

## GitHub

* organization connection
* repository discovery
* repository inventory
* webhook/event ingestion

## Security Posture

* branch protection checks
* CODEOWNERS checks
* SECURITY.md checks
* dependency scanning status
* secret scanning status
* GitHub Actions permission checks

## Supply Chain

* dependency extraction
* SBOM generation/ingestion
* vulnerability matching
* direct/transitive dependency relationships

## Findings

* normalized finding model
* severity
* affected resource
* evidence
* remediation recommendation
* status

## Audit

* security events
* actor
* timestamp
* resource
* action
* event timeline

## Dashboard

* overview
* repositories
* findings
* vulnerabilities
* posture
* events

This is the first target.

---

# 23. MVP Demo Scenario

The team should eventually be able to demonstrate:

```text
1. User installs platform.

2. User connects GitHub organization.

3. Platform discovers repositories.

4. Platform analyzes repository security posture.

5. Platform extracts dependencies.

6. Platform generates/imports SBOM.

7. Platform checks vulnerabilities.

8. Platform discovers a vulnerable dependency.

9. Platform identifies affected repositories.

10. Platform determines whether affected repositories are
    production-related where deployment information exists.

11. Platform records the finding.

12. User views the finding in the dashboard.

13. User can see:
       dependency
       vulnerability
       affected repository
       dependency path
       severity
       fixed version
       evidence

14. User can view related security events.

15. User receives a remediation recommendation.
```

This should be the core demonstration.

---

# 24. Development Phases

## Phase 0 — Research

Before coding:

* research existing products
* research open-source alternatives
* study relevant GitHub issues
* identify project differentiation
* finalize requirements
* finalize architecture
* choose license
* establish contribution rules

Deliverables:

* requirements document
* architecture document
* threat model
* initial database model
* initial API design

---

# Phase 1 — Repository Foundation

Build:

* repository structure
* Java/Spring Boot project
* React/TypeScript project
* PostgreSQL
* Redis
* Docker Compose
* CI pipeline
* code quality tooling
* test infrastructure

---

# Phase 2 — Authentication & Organization

Implement:

* users
* organizations
* teams
* roles
* permissions
* authentication
* organization isolation

---

# Phase 3 — GitHub Integration

Implement:

* GitHub App
* repository discovery
* organization discovery
* webhook processing
* event normalization
* GitHub API client

---

# Phase 4 — Security Posture

Implement:

* branch protection checks
* CODEOWNERS checks
* SECURITY.md checks
* secret scanning configuration checks
* dependency scanning configuration checks
* GitHub Actions permission checks

Create normalized findings.

---

# Phase 5 — Dependency Intelligence

Implement:

* dependency extraction
* package models
* direct dependencies
* transitive dependencies
* dependency relationships

---

# Phase 6 — SBOM

Implement:

* CycloneDX support
* SPDX support
* SBOM ingestion
* SBOM generation where practical
* SBOM-to-repository relationships

---

# Phase 7 — Vulnerability Engine

Implement:

* vulnerability ingestion
* OSV integration
* advisory matching
* version range handling
* severity
* fixed-version information
* vulnerability-to-dependency relationships

---

# Phase 8 — Security Graph

Connect:

```text
Organization
User
Team
Repository
Dependency
Package
SBOM
Vulnerability
Finding
Event
```

Then progressively add:

```text
Build
Artifact
Deployment
Service
Infrastructure
```

---

# Phase 9 — Dashboard

Build:

* organization overview
* repository inventory
* finding dashboard
* vulnerability details
* dependency graph
* security posture
* audit timeline
* security graph

---

# Phase 10 — CLI

Initial commands:

```bash
secintel login
secintel connect github
secintel repositories
secintel scan
secintel findings
secintel events
secintel sbom
secintel policy check
```

---

# Phase 11 — Remediation

Add:

* remediation recommendations
* fixed-version suggestions
* GitHub issues
* dependency update PRs where safe
* security configuration PRs where practical

---

# Phase 12 — Self-Hosted Release

Provide:

```bash
docker compose up
```

as the easiest local/self-hosted installation.

Documentation should cover:

* installation
* configuration
* GitHub App setup
* database setup
* security configuration
* upgrades
* backups

---

# 25. Future Roadmap

After the MVP is stable:

## Integrations

* GitLab
* Bitbucket
* Docker registries
* Kubernetes
* AWS
* Azure
* GCP

## Supply Chain

* provenance
* artifact attestations
* malicious package intelligence
* package maintenance analysis
* release anomaly detection
* artifact tracking

## Security

* advanced secret detection
* credential exposure analysis
* attack-path analysis
* security risk correlation
* advanced policy engine

## Operations

* notifications
* Slack
* email
* webhooks
* scheduled reports

## Remediation

* automated pull requests
* policy fixes
* controlled secret rotation
* infrastructure remediation

---

# 26. Open-Source Strategy

The project should be open-source from the beginning.

Development should happen publicly.

Use:

* GitHub Issues
* GitHub Discussions
* Pull Requests
* Releases
* Roadmap
* Documentation

External contributors should be able to:

1. clone the project
2. start it locally
3. understand the architecture
4. run tests
5. make a change
6. submit a PR

The developer experience is part of the product.

---

# 27. Licensing

The team should explicitly evaluate licenses before the first public release.

Candidate licenses include:

* Apache 2.0
* MIT
* AGPLv3

The final choice should consider:

* commercial use
* self-hosting
* contribution model
* modifications
* SaaS usage
* long-term project goals

Do not choose a license casually.

---

# 28. Testing Strategy

Testing should exist at multiple levels.

## Unit tests

For:

* vulnerability matching
* version comparison
* policy evaluation
* dependency analysis
* risk logic
* event normalization

## Integration tests

For:

* PostgreSQL
* Redis
* GitHub API integration
* webhook processing

## API tests

For:

* authentication
* authorization
* organizations
* repositories
* findings
* events

## Security tests

Test:

* unauthorized access
* privilege escalation
* invalid tokens
* webhook forgery
* injection
* secret leakage
* insecure configuration

---

# 29. Observability

The platform should use:

* structured logs
* health endpoints
* metrics
* traceability where appropriate

Eventually support:

* OpenTelemetry
* Prometheus
* Grafana

Do not make these mandatory dependencies for the MVP unless required.

---

# 30. Performance Philosophy

Do not prematurely optimize.

Initial goals:

* reliable scanning
* correct vulnerability matching
* efficient database queries
* asynchronous processing for expensive operations
* caching where useful

Later optimize:

* large organizations
* thousands of repositories
* millions of dependencies
* large event streams
* graph traversal

Correctness comes before extreme scale.

---

# 31. Security Threat Model

Threat modeling should consider:

### External attacker

Attempts to:

* compromise the platform
* steal organization data
* access credentials
* forge webhooks
* bypass authorization

### Malicious repository

Contains:

* malicious dependencies
* malicious files
* crafted manifests
* payloads intended to exploit scanners

### Compromised integration

A GitHub/cloud credential becomes compromised.

### Malicious organization user

Attempts to access resources they should not access.

### Supply-chain attack

A dependency or artifact is compromised.

The platform must treat external repository/package data as untrusted input.

---

# 32. Important Security Rule

The platform will analyze potentially malicious content.

Therefore:

**Never execute arbitrary repository code during scanning unless a future isolated sandbox architecture explicitly supports it.**

For example, dependency analysis should not blindly execute:

```text
package installation scripts
build scripts
repository code
```

The scanner should prefer static analysis and metadata extraction.

If execution becomes necessary later, it should happen in an isolated environment with a defined threat model.

---

# 33. Project Success Criteria

The project should not measure success only by lines of code.

Important indicators:

### Technical

* reliable detection
* low false-positive rate
* reproducible results
* secure architecture
* good test coverage
* maintainable code

### Open Source

* external contributors
* issues
* pull requests
* documentation quality
* releases
* community adoption

### Product

A user should be able to connect an organization and obtain meaningful security information without manually configuring dozens of systems.

---

# 34. What We Are NOT Trying to Prove

We are not trying to prove:

> "We can build a dashboard."

We are trying to prove:

> **We can design and implement a real security system that collects, models, correlates and analyzes software-security data in a way that is useful to organizations.**

---

# 35. Long-Term Vision

The eventual platform should be capable of answering questions such as:

> Which production applications are affected by this vulnerability?

> Which repositories can deploy to production?

> Which dependencies introduce the greatest security exposure?

> Who can modify this production application?

> What security controls are missing?

> What changed before this incident?

> Where did this production artifact originate?

> Which systems are connected to this compromised credential?

> What remediation should be performed?

The long-term goal is:

```text
                ORGANIZATION
                     │
                     ▼
             SECURITY GRAPH
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
  Supply Chain    Security       Audit &
  Intelligence    Posture       Events
       │             │             │
       └─────────────┼─────────────┘
                     ▼
             Security Findings
                     │
                     ▼
               Remediation
```

The project should evolve from a GitHub-focused MVP into a broader open-source security intelligence platform.

---

# 36. Guiding Principle

Build a system that answers:

> **"What do we have, how is it connected, what is wrong, why does it matter, what changed, and what should we do about it?"**

Everything added to the project should contribute meaningfully toward answering that question.
