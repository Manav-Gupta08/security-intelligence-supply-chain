# Contributing

Thank you for helping build the security intelligence and software supply-chain platform. Security, reviewability, and reproducible decisions take priority over change volume.

## Before Starting

1. Find or create a focused GitHub issue with scope and acceptance criteria.
2. Comment that you are taking the issue to avoid duplicate work.
3. Identify the owning module and any cross-module contract before coding.
4. Discuss changes to architecture, public APIs, security boundaries, data ownership, dependencies, or infrastructure before implementation.
5. Never place credentials, tokens, private repository data, or production data in issues, fixtures, logs, or commits.

## Branches and Commits

- Create a short-lived branch from the current default branch.
- Keep each commit focused on one logical change and independently reviewable.
- Separate documentation, scaffolding, schema, behavior, and broad formatting changes where practical.
- Do not mix unrelated cleanup with feature or security work.
- Add one entry at the top of [CHANGELOG.md](CHANGELOG.md) for every commit, following its required template.
- Use an imperative commit message such as `docs(api): define pagination contract` or `fix(webhook): reject invalid signatures`.
- Rebase or otherwise synchronize before review according to the maintainers' chosen Git workflow; do not rewrite shared history without agreement.

Small commits are not partial or broken commits. Every commit should compile and pass the checks relevant to its scope once executable code exists.

## Pull Requests

A pull request should:

- solve one issue or one coherent part of an issue;
- explain the problem, approach, security impact, and verification;
- link the issue and relevant architecture decision;
- call out migrations, API compatibility, configuration, and operational impact;
- include tests proportional to risk;
- avoid secrets and sensitive screenshots or logs;
- be reviewed by at least one developer other than its author.

Changes to authentication, authorization, cryptography, webhook validation, secret handling, organization isolation, policy evaluation, or parsing of untrusted content require an explicit security-focused review.

## Four-Person Coordination

Use areas of primary responsibility to reduce collisions, not to create silos:

| Area | Suggested primary owner |
| --- | --- |
| Backend platform and architecture | Developer 1 |
| Supply-chain and vulnerability engine | Developer 2 |
| Posture, policy, findings, and events | Developer 3 |
| Frontend, GitHub integration, and developer experience | Developer 4 |

Replace placeholders with team names in [docs/phase-0/README.md](docs/phase-0/README.md). Cross-area changes need early coordination with both owners.

## Decision Process

- Record durable architecture choices as short decision records under `docs/decisions/`.
- A decision record states context, options, decision, consequences, owner, reviewers, and date.
- Security-affecting decisions reference the threat model.
- Unapproved proposals stay marked `Proposed`; implementation should not silently settle an open decision.
- Prefer reversible decisions when evidence is incomplete.

## Required Checks

Phase 0 documentation changes must pass link, structure, and consistency review. Once code exists, the repository will define commands for formatting, compilation, unit tests, integration tests, static analysis, dependency scanning, and secret scanning in `DEVELOPMENT.md` and CI.

Before requesting review:

1. Run the narrowest relevant checks.
2. Run the full required suite when changing a shared contract or security boundary.
3. Update documentation and [CHANGELOG.md](CHANGELOG.md).
4. Review the diff for accidental credentials, generated files, and unrelated changes.

## Reporting Security Issues

Do not open a public issue for a suspected vulnerability. Until a private disclosure channel is configured in `SECURITY.md`, contact the repository maintainers privately and share only the information necessary to reproduce the issue.