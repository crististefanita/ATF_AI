# Atlas Validation

## Purpose

Atlas validation turns executable business-quality rules into auditable matrices, diagnostics and published validation artifacts. This package is the reporting/audit layer for Atlas, not the place for MCP package governance.

## Entry points

- `AtlasAuditValidation`: high-level validation facade used by contract tests.
- `AtlasRuleMatrix`: rule-to-result matrix for agent-facing diagnosis.
- `AtlasDiagnostic`: compact diagnostic rendering for violations and repair targets.
- `AtlasCandidateRuleMatrixPublisher`: publishes candidate matrix artifacts.

## Change here when

- Atlas needs a clearer rule matrix, diagnostic output or validation artifact.
- A business-quality rule already exists and its published evidence needs better routing.
- The Atlas skill needs easier agent-facing repair targets.

## Do not

- Do not import MCP validators or package close-out logic.
- Do not add Core reporting mechanics here; use `com.endava.ai.core.reporting` for generic HTML/runtime support.
- Do not hide business-rule decisions in publishers; rule meaning belongs in `rules` and contract YAML.

## Verification

- Run `AtlasRuleCatalogContractTest`.
- Run Atlas candidate/profile tests for generated output changes.
- If MCP/Atlas bridge behavior changes, verify the MCP governance map and Atlas quality lanes remain synchronized.
