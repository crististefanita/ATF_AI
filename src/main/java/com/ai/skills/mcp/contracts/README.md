# MCP Contracts

## Purpose

Typed contract metadata and YAML-backed policy live here. This package should explain what MCP enforces without forcing agents to inspect every rule implementation.

## Entry points

- `catalog.ContractCatalog`: typed facade over YAML contract resources.
- `catalog`: resource constants, lazy loading and package-private catalog slices.
- `model`: generic YAML contract models such as `ContractSpec`, `KpiSpec` and `RuleSpec`.
- `policy`: score caps, close-out artifacts, protected roots, html-ex and package policies.
- `provenance`: Extent provenance contract tokens and thresholds.
- `execution`: close-out evidence, execution profiles and artifact family vocabulary.
- `advisor`: non-blocking INFO/WARN guidance for prompt scope, model tier, acceptance criteria, stop conditions and value rationale.
- `atlas-quality-bridge.yml`: non-blocking calibration between MCP final-report closeout surfaces and Atlas business-quality lanes.
- `McpBusinessUnderstandingConsumerContractTest`: MCP sanity proof that final-report graph/proof/score semantics consume the shared Business Understanding kernel instead of importing Atlas.
- `BusinessGraphLayoutAdapter`: shared Reporting adapter to use when MCP final reports render a `BusinessGraphConstruction` into the Business Flow Graph layout; MCP must not decide Atlas business quality through this adapter.
- `checks` and `suites`: annotation references and rendered CheckRef evidence for concise MCP tests.
- `package-shape.yml`: MCP skill package-shape ownership facade; it points to the numeric `size-budgets.yml` source of truth.
- `size-budgets.yml`: anti-bloat caps for packages, broad recursive surfaces, large classes and large YAML resources.

## Reading Path

1. Start here to decide whether the change belongs to MCP governance, contracts or generated-package validation.
2. Check `src/main/resources/skills/mcp/contracts/capability-packs.yml` for the capability lane.
3. Check `src/main/resources/skills/mcp/contracts/agentic-execution-flow.yml` for the stage order and stop condition.
4. Run `src/test/resources/skills/mcp/testng-mcp-sanity.xml` before trusting MCP changes.

## Change here when

- A rule needs agent-facing expected/why/repair text.
- A profile, close-out artifact or score cap changes.
- The framework should advise the next agentic run without turning guidance into a blocker.
- MCP final-report closeout needs to point agents toward Atlas business-quality validation without becoming an Atlas gate.
- A string list is becoming a repeated policy and should become typed metadata.
- A YAML file grows past its byte ratchet and must split by cohesive contract family.
- A broad rules/validation/contracts/tests surface grows and needs an explicit recursive cap decision.
- MCP package ownership, README coverage, import boundaries or console gateway policy changes.

## Do not

- Do not expose raw `Map<String, String>` as public contract API.
- Do not add a YAML key without schema/version validation.
- Do not duplicate artifact names that already exist in `McpArtifactPaths` or `McpArtifactFamily`.
- Do not put integrity failures in `ai-task-advisor.yml`; fake evidence, broken links, protected-root drift and score mismatch belong in executable MCP blockers.
- Do not turn `atlas-quality-bridge.yml` into a blocker; MCP owns artifact mechanics, Atlas owns business-meaning judgment.
- Do not raise a size budget without explaining the ownership split that was considered first.
- Do not duplicate numeric package/class caps in `package-shape.yml`; keep numbers in `size-budgets.yml`.

## Verification

- Run `McpYamlContractSchemaTest`.
- Run `McpYamlContractSyncTest`.
- Run `McpRuleRoutingCatalogContractTest` after any profile or rule route change.
- Run `McpPackageShapeContractTest` after changing size budgets or package layout.
- Run `McpBusinessUnderstandingConsumerContractTest` after changing LangGraph final-report business semantics.
- Run Reporting consumer smoke when MCP final-report graph rendering changes so `BusinessGraphLayoutAdapter` stays the shared layout path for `BusinessGraphConstruction`.
- Keep `package-shape.yml` and `size-budgets.yml` aligned: the former explains ownership, the latter owns numeric caps.
- Inspect `target/mcp-governance-map.md` when changing check metadata.
- Run `McpYamlContractMatrixTest` after changing advisor rendering.
