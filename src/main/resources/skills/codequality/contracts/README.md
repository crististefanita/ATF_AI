# Code Quality YAML Contracts

## Purpose

This folder is the declarative policy surface for the Code Quality skill. Code Quality owns maintainability guardrails for package size, class size, method size, assertion density, resource size, suite count, dependency direction and Java baseline compatibility.

## Contract Index

- `code-quality.yml`: Code Quality policy, source/resource roots, dependency boundaries and legacy ratchets.
- `size-budgets.yml`: shared numeric size budgets consumed by Core, Reporting and skill package-shape checks.

## Do Not

- Do not add MCP publishability rules here.
- Do not add Atlas business-quality rules here.
- Do not add Reporting visual-shell semantics here.
- Do not add a YAML file without listing it here.

## Verification

- Run `src/test/resources/skills/codequality/testng-code-quality-sanity.xml`.
- Run `AtfSkillPackageGovernanceContractTest` after adding, moving or deleting Code Quality contract YAML resources.
