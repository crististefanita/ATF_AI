# Business Understanding Contract Tests

## Purpose

These tests protect the shared semantic kernel used by MCP and Atlas. They
verify business objects, flow stories, graph construction, proof chains, source
trace rows, score rationale, semantic YAML and consumer bridges.

They do not validate report layout, MCP publishability or Atlas generated
artifact rendering.

## Reading Path

1. Start with `BusinessUnderstandingKernelContractTest` for semantic models and value objects.
2. Use `BusinessUnderstandingArtifactParserContractTest` for neutral artifact reduction.
3. Use `BusinessUnderstandingConsumerBridgeContractTest` to keep MCP and Atlas consumer expectations aligned.
4. When graph semantics change, update `BusinessGraphConstruction` and `skills/business/contracts/semantic-model.yml`.

## Ownership Split

| Concern | Owner |
|---|---|
| Business graph semantic recipe | Business Understanding |
| Business graph layout and CSS tokens | Reporting |
| Final report gate and package truth | MCP |
| Business realism, source trace and score maturity | Atlas |

## Verification

- Run `mvn -q "-Dsurefire.suiteXmlFiles=src/test/resources/skills/business/testng-business-understanding-sanity.xml" "-DforkCount=0" test`.
- Keep Business Understanding free of MCP, Atlas and Reporting imports.
