# Phase 0: Research and Architecture

**Overall status:** In progress  
**Started:** 2026-09-21

Phase 0 validates the product and establishes reviewed constraints before application code begins. Draft documents are working proposals until the team records approval.

## Deliverables

| Deliverable | Document | Status | Owner | Reviewers |
| --- | --- | --- | --- | --- |
| Product and open-source research | [RESEARCH.md](RESEARCH.md) | In progress | Unassigned | Unassigned |
| MVP requirements | [REQUIREMENTS.md](REQUIREMENTS.md) | Draft | Unassigned | Team |
| Architecture | [../../ARCHITECTURE.md](../../ARCHITECTURE.md) | Draft | Developer 1 | Team |
| Threat model | [THREAT_MODEL.md](THREAT_MODEL.md) | Draft | Unassigned | Team |
| Initial database model | [DATA_MODEL.md](DATA_MODEL.md) | Draft | Unassigned | Team |
| Initial API design | [API_DESIGN.md](API_DESIGN.md) | Draft | Unassigned | Team |
| License selection | [LICENSE_EVALUATION.md](LICENSE_EVALUATION.md) | Pending decision | Unassigned | Team |
| Contribution rules | [../../CONTRIBUTING.md](../../CONTRIBUTING.md) | Draft | Developer 1 | Team |

Replace `Developer 1` and `Unassigned` with actual names during the Phase 0 kickoff.

## Required Decisions

| ID | Decision | Status | Blocking |
| --- | --- | --- | --- |
| `D-001` | Product differentiation supported by external evidence | Open | Phase 1 scope |
| `D-002` | Open-source license | Open | Public release |
| `D-003` | Java, Spring Boot, Node.js, and frontend tool versions | Open | Phase 1 scaffolding |
| `D-004` | Authentication and session model | Open | Authentication implementation |
| `D-005` | Role-to-permission matrix | Open | Protected APIs |
| `D-006` | GitHub App permissions and webhook events | Open | GitHub integration |
| `D-007` | CycloneDX/SPDX versions and parser approach | Open | SBOM implementation |
| `D-008` | Vulnerability source precedence and update cadence | Open | Vulnerability engine |
| `D-009` | Data retention and deletion policy | Open | Production use |
| `D-010` | MVP scale and recovery targets | Open | Performance acceptance |

## Working Agreement

1. Assign one owner and at least one reviewer to each deliverable.
2. Link research evidence and record confidence and unknowns.
3. Resolve blocking decisions through reviewed decision records under `docs/decisions/`.
4. Update the document status and this tracker when a decision changes.
5. Record each small commit in [../../CHANGELOG.md](../../CHANGELOG.md).

## Phase 0 Exit Criteria

- Research work items have owners, sources, and reviewed recommendations.
- Requirements are approved and open scope questions are resolved or explicitly deferred.
- Architecture, threat model, data model, and API design have team review.
- License and contribution model are approved.
- Critical threats map to planned controls and tests.
- Phase 1 technology choices and repository-foundation acceptance criteria are recorded.

Phase 1 should not begin broadly while decisions that block its scaffolding remain open. Small research spikes may be used to resolve a documented decision.