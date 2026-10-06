# 0001. Open-Source License

- Status: Accepted
- Date: 2026-10-06
- Owner: Manav Gupta
- Reviewers: Team review pending
- Resolves: `D-002`

## Context

The project must be usable and contributable by external organizations. The [license evaluation](../phase-0/LICENSE_EVALUATION.md) compared Apache-2.0, MIT, and AGPL-3.0.

## Options

1. Apache-2.0: permissive with an explicit patent grant.
2. MIT: short, permissive, and broadly compatible.
3. AGPL-3.0: strong network copyleft.

## Decision

License the repository under the MIT License. The copyright line is `Security Intelligence Supply Chain contributors`.

## Consequences

- Organizations may use, modify, self-host, and redistribute the software, including in proprietary or hosted form, without publishing changes.
- MIT has no explicit patent grant. This tradeoff was accepted for simplicity and adoption.
- Contributions are accepted under the same license (inbound = outbound). No CLA is required; adopting a Developer Certificate of Origin remains open.
- New dependencies must have MIT-compatible licenses.
