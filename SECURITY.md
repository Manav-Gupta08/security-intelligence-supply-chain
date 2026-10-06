# Security Policy

## Supported Versions

The project has not published a release. Security fixes are applied to the `main` branch only.

## Reporting a Vulnerability

Do not open a public issue, discussion, or pull request for a suspected vulnerability.

Report it privately through GitHub's **Report a vulnerability** button on the repository's **Security** tab (private vulnerability reporting). If that option is unavailable, contact a maintainer privately and ask for a private channel before sharing details.

Include only what is needed to reproduce the issue:

- affected component, endpoint, or file;
- impact and preconditions;
- reproduction steps or a minimal proof of concept;
- any suggested mitigation.

Never include real credentials, tokens, private repository data, or production data. Use synthetic values.

## What to Expect

Maintainers will acknowledge a report, assess severity, and coordinate a fix and disclosure timeline with the reporter. Reporters are credited unless they request otherwise.

## Scope

In scope: the backend, frontend, deployment configuration, and CI workflows in this repository.

Out of scope: vulnerabilities in third-party services such as GitHub itself, and findings that require a compromised host or operator account.

The project's security design is described in the [threat model](docs/phase-0/THREAT_MODEL.md).
