# License Evaluation

**Status:** Decided: MIT (see [ADR 0001](../decisions/0001-open-source-license.md))  
**Owner:** Manav Gupta  
**Decision date:** 2026-10-06

The project is licensed under the MIT License in the root [LICENSE](../../LICENSE) file. The evaluation below is kept as the decision record's background.

## Decision Drivers

- The platform should be genuinely open source and welcoming to external contributors.
- Organizations and commercial vendors should understand self-hosting and redistribution rights.
- The team must decide whether hosted modifications must be shared.
- Patent protection matters for a security and supply-chain product.
- Contributor expectations and long-term stewardship should be clear.

## Candidates

| License | Advantages for this project | Tradeoffs |
| --- | --- | --- |
| Apache License 2.0 | Permissive use, explicit patent grant, familiar to companies and foundations | Hosted or redistributed proprietary modifications need not be published |
| MIT License | Short, simple, and broadly compatible | No explicit patent grant; proprietary forks and hosted modifications may remain closed |
| GNU AGPL v3 | Strong copyleft, including an obligation to offer source to network users of modified versions | Lower adoption in some organizations and greater compatibility/compliance complexity |

## Recommendation for Discussion

Use Apache-2.0 as the default recommendation if broad adoption, integrations, and commercial participation are the primary goals. Choose AGPL-3.0 only if preventing closed hosted modifications is an explicit product-governance goal the maintainers are prepared to enforce. MIT offers simplicity but weaker patent language than Apache-2.0.

This is a project-strategy decision, not legal advice. Maintainers should review current license texts and seek qualified advice if commercial or contributor arrangements create uncertainty.

## Questions to Resolve

1. May companies offer a modified hosted service without publishing changes?
2. Is an explicit patent grant required?
3. Will the project accept corporate contributions and need a CLA or Developer Certificate of Origin?
4. Are dependencies and planned code-generation tools compatible with the selected license?
5. Who owns the project name and trademarks?
6. Could dual licensing be a future goal?

## Approval Record

| Field | Value |
| --- | --- |
| Selected license | MIT |
| Decision owner | Manav Gupta |
| Reviewers | Team review pending |
| Decision date | 2026-10-06 |
| Rationale | Simplicity and broad adoption; the missing explicit patent grant is an accepted tradeoff |
| Follow-up | Done: root `LICENSE`, README, and contribution documentation updated |