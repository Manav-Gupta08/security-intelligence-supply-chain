# Phase 0 Research Register

**Status:** In progress  
**Started:** 2026-09-21

## Purpose

Validate the product's differentiation and the major technical decisions before implementation. Research findings must link to primary sources and record the decision they influence.

## Product Hypothesis

The platform's value is not another scanner. It is an open-source correlation layer that connects GitHub repositories, security posture, dependency and vulnerability data, findings, events, and deployment context into an explainable security graph.

## Initial Landscape

| Project or category | Relevant strength | Question for this project |
| --- | --- | --- |
| OWASP Dependency-Track | SBOM component analysis and vulnerability management | Which SBOM workflows should be integrated rather than recreated? |
| GUAC | Graph-oriented software supply-chain metadata | Which graph concepts and ingestion patterns can be reused or made interoperable? |
| OpenSSF Scorecard | Automated repository security-health checks | Which checks can be consumed directly while preserving evidence and history? |
| OWASP DefectDojo | Finding aggregation, deduplication, and workflows | How should normalized findings remain simple while supporting imported results? |
| Backstage software catalog | Ownership and software inventory | Where does security correlation add value beyond cataloging? |
| GitHub security features | Native repository posture and advisory data | Which GitHub APIs provide authoritative evidence and which require local evaluation? |

This is a starting map, not a completed competitive analysis.

## Research Work Items

| ID | Work item | Output | Status |
| --- | --- | --- | --- |
| R-001 | Compare open-source alternatives and adjacent products | Capability matrix with overlap, gaps, and integration opportunities | In progress |
| R-002 | Review issue trackers for recurring user pain | Evidence-backed problem list linked to source issues | Not started |
| R-003 | Evaluate GitHub App permissions and webhook coverage | Least-privilege permission matrix | Not started |
| R-004 | Evaluate CycloneDX and SPDX interoperability | Supported-version and field-mapping decision | Not started |
| R-005 | Evaluate OSV query and data models | Vulnerability ingestion and matching decision | Not started |
| R-006 | Compare Apache-2.0, MIT, and AGPL-3.0 | License decision record approved by maintainers | Not started |
| R-007 | Validate PostgreSQL relationship-query approach | Representative schema and query spike | Not started |
| R-008 | Define MVP deployment assumptions | Supported local and self-hosted topology | Not started |

## Differentiation Tests

The team should reject or revise the product hypothesis unless research supports all of these statements:

1. Users need cross-tool correlation that is not adequately served by a single existing open-source project.
2. GitHub repository, posture, dependency, finding, and event data can be normalized without requiring arbitrary code execution.
3. PostgreSQL can support the initial relationship queries at MVP scale.
4. The MVP can provide useful answers before artifact and production deployment integrations are complete.

## Research Evidence Template

```markdown
### R-XXX - Research question

- Researcher:
- Date:
- Sources:
- Observations:
- Decision affected:
- Recommendation:
- Confidence and unknowns:
```

## Exit Criteria

Phase 0 research is complete when every work item above has an owner, evidence, and a reviewed outcome; the differentiation statement is accepted or revised; and unresolved risks have explicit owners.