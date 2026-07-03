# Atlas YAML Contracts

## Purpose

This folder is the declarative policy surface for the Atlas skill. Atlas owns business understanding, source-to-business trace, graph quality, score truth and Atlas output quality.

## Contract Index

- `agentic-quality-lanes.yml`: Atlas quality lanes used for agentic business-understanding calibration.
- `AtlasBusinessUnderstandingConsumerContractTest`: Atlas sanity proof that graph/source-trace/proof/score semantics consume the shared Business Understanding kernel without importing MCP.
- `graph-reporting.yml`: graph presentation, legend and report proof policy.
- `package-shape.yml`: Atlas package shape, README, import-boundary and console-gateway policy.
- `report-sections.yml`: Atlas report section vocabulary.
- `rule-packs.yml`: Atlas rule-pack map for business semantics, graph proof, score truth and audit publication.
- `rule-policies.yml`: Atlas rule ownership, repair target, matrix and cap-impact policy.
- `suites.yml`: Atlas TestNG suite metadata and prompt ownership.

## Do Not

- Do not add MCP package close-out or artifact-spine policy here.
- Do not add a YAML file without listing it here.

## Verification

- Run `AtfSkillArchitectureContractTest` after adding, moving or deleting Atlas contract YAML resources.
- Run `AtlasPackageShapeContractTest` and Atlas sanity after Atlas contract changes.
- Run `AtlasBusinessUnderstandingConsumerContractTest` after changing graph, source-trace, proof-strip or score-truth semantics.
