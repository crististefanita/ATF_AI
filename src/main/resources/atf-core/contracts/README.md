# ATF Core YAML Contracts

## Purpose

This folder is the declarative policy surface for ATF Core. Core owns reusable framework/runtime/reporting-library contracts and skill routing. MCP and Atlas-specific business/package rules stay outside Core.

## Contract Index

- `architecture/architecture-boundaries.yml`: Core/MCP/Atlas dependency boundary policy.
- `architecture/test-architecture.yml`: executable test-lane ownership map for product API/UI tests, ATF Core tests and MCP/Atlas/Reporting/Business skill tests.
- `narratives/contract-annotations.yml`: contract annotation governance.
- `narratives/contract-narratives.yml`: ATF contract narrative catalog.
- `profiles/profiles.yml`: Core TestNG profile taxonomy.
- `profiles/core-advisor.yml`: non-blocking Core advisor guidance.
- `runtime/runtime-options.yml`: runtime option governance.
- `skills/skill-architecture.yml`: Core/Reporting/Business Understanding/MCP/Atlas skill routing plus prompt-asset ownership and use-case contract; `AtfSkillArchitectureContractTest` publishes the generated `target/skill-boundary-matrix.md` from this manifest plus the package-shape contracts.
- `skills/skill-clean-sanity.yml`: clean compile plus sequential skill sanity chain for stale/corrupted target output.
- `skills/skill-review-loop.yml`: executable Signal/Explore/Govern/Validate loop for skill/OOP architecture calibration.
- `ui/ui-engine-contracts.yml`: UI engine support contracts.

Skill-owned contract packs live outside this Core root so their ownership stays explicit:

- `src/main/resources/skills/codequality/contracts/`: Code Quality package, size and assertion policies.
- `src/main/resources/skills/reporting/contracts/`: Reporting shell, ownership, package-shape and shared HTML report policies.

## Do Not

- Do not add MCP package close-out, html-ex candidate or Atlas business graph policy here.
- Do not add a YAML file without listing it here.

## Verification

- Run `AtfSkillArchitectureContractTest` after adding, moving or deleting Core contract YAML resources.
- Use `target/skill-boundary-matrix.md` as the generated human/agent routing map; do not edit it manually.
- Run Core sanity after Core contract changes.
