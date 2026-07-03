# Atlas Rules

## Reading Path

1. Start here to decide whether the change belongs to Atlas business graph, source trace, score truth or audit publication.
2. Check `src/main/resources/skills/atlas/contracts/rule-packs.yml` for the rule-pack lane.
3. Run `src/test/resources/skills/atlas/testng-atlas-sanity.xml` before trusting Atlas rule changes.

## Purpose

Atlas rules validate the business/audit model: graph meaning, source trace, scoring, skill readiness and generated Atlas output quality.

## Entry points

- `AtlasGeneratedOutputValidator`: executes Atlas rule sets.
- `com.ai.skills.atlas.contract.AtlasRuleCatalog`: resolves rule metadata.
- `com.ai.skills.atlas.validation.AtlasRuleMatrix`: publishes rule status, repair target and matrix artifacts.
- `src/main/resources/skills/atlas/contracts/rule-policies.yml`: owner, repair target, matrix and cap-impact policy.
- `src/main/resources/skills/atlas/contracts/rule-packs.yml`: groups rules/contracts into agent-readable Atlas rule packs.
- `src/main/resources/skills/atlas/contracts/package-shape.yml`: package size, README, import-boundary and console-gateway policy for the Atlas skill.
- `BusinessGraphLayoutAdapter`: shared Reporting adapter to use when Atlas renders a `BusinessGraphConstruction` into the primary Business Flow Graph layout; Atlas must not decide MCP package publishability through this adapter.

## Change here when

- A business graph, proof trace or score formula invariant changes.
- A new Atlas coverage/check needs policy text and a matrix row.
- Atlas needs to explain where to repair generated evidence.
- A rule pack needs to clarify which suite, contracts and package roots own a repair.

## Do not

- Do not turn Atlas into package close-out governance; that belongs in MCP.
- Do not duplicate MCP artifact-spine checks here.
- Do not add narrative-only Atlas rules without source-backed evidence or repair target.
- Do not import MCP from Atlas main or tests; exchange evidence through YAML, matrices, links and published artifacts.
- Do not raise Atlas package caps without first splitting ownership or adding a focused local README.

## Verification

- Run Atlas sanity/smoke profiles, not MCP sanity.
- Run `AtlasPackageShapeContractTest` after changing Atlas package layout, README ownership, imports or console behavior.
- Confirm the Atlas rule matrix exposes owner, repair target and cap impact.
- Keep Atlas focused on business/audit understanding.
- Run Reporting consumer smoke when Atlas graph rendering changes so `BusinessGraphLayoutAdapter` stays the shared layout path for `BusinessGraphConstruction`.
