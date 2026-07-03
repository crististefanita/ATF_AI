# Business Understanding Contracts

## Purpose

This folder is the declarative policy surface for the shared Business Understanding kernel. It keeps MCP and Atlas aligned on semantic steps without making either skill depend on the other.

## Contract Index

- `semantic-pipeline.yml`: input-agnostic business-understanding stages shared by MCP and Atlas.
- `semantic-model.yml`: shared semantic model vocabulary extracted from golden Atlas/MCP report patterns.
- `semantic-calibration.yml`: calibration guidance for score honesty, maturity, proof depth and shared semantic limits.
- `semantic-lexicon.yml`: YAML-owned concept tokens used by the neutral artifact reducer.
- `consumer-bridge.yml`: executable bridge declaration that maps MCP and Atlas consumer contracts to the shared semantic models, concepts and stages.
- `package-shape.yml`: package size and forbidden-import policy for the shared semantic kernel.

## Do Not

- Do not add MCP package close-out policy here.
- Do not add Atlas generated artifact ownership here.
- Do not add Reporting shell or Extent style rules here.
- Do not add a YAML file without listing it here.

## Verification

- Run `BusinessUnderstandingKernelContractTest`.
- Run `src/test/resources/skills/business/testng-business-understanding-sanity.xml`.
- Run `McpBusinessUnderstandingConsumerContractTest` from MCP sanity after changing final-report semantic vocabulary.
- Run `AtlasBusinessUnderstandingConsumerContractTest` from Atlas sanity after changing graph/source-trace/score semantic vocabulary.
