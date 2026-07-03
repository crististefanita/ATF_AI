# Reporting YAML Contracts

## Purpose

This folder is the declarative policy surface for the Reporting skill. Reporting owns shared report shell, visual primitives, Extent/Spark conventions and neutral report-surface governance. It must not decide MCP package publishability or Atlas business truth.

## Contract Index

- `console-signature.yml`: console output fragments and forbidden noise for report-facing runtime logs.
- `html-report-shared.yml`: shared HTML report shell vocabulary and reusable report primitives.
- `reporting-architecture.yml`: Reporting architecture zones and ownership boundaries.
- `reporting-ownership.yml`: Reporting implementation ownership, expected files and test-side exceptions.
- `reporting-package-shape.yml`: Reporting package shape, import-boundary and legacy-size policy.
- `reporting-shell.yml`: shared report shell, CSS, mode and report-kind policy.
- `reporting-skill-surface.yml`: Reporting skill facade, consumer smoke and BusinessGraph layout surface.

## Do Not

- Do not add MCP close-out rules here.
- Do not add Atlas business scoring or source-trace meaning here.
- Do not add a YAML file without listing it here.

## Verification

- Run `src/test/resources/atf/core/reporting/testng-reporting-sanity.xml`.
- Run `AtfSkillPackageGovernanceContractTest` after adding, moving or deleting Reporting contract YAML resources.
