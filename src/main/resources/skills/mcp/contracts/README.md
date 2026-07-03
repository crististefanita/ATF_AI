# MCP YAML Contracts

## Purpose

This folder is the declarative policy surface for the MCP skill. Java rules execute the policy; YAML keeps agent-facing ownership, routing, scoring, package-shape and artifact contracts visible.

## Contract Index

- `agentic-execution-flow.yml`: agentic lifecycle stages and expected flow semantics.
- `ai-task-advisor.yml`: non-blocking INFO/WARN guidance for model tier, scope, acceptance criteria and stop condition.
- `atlas-quality-bridge.yml`: non-blocking bridge from MCP final report evidence to Atlas business-quality lanes.
- `capability-packs.yml`: MCP capability map for orchestration, package governance, report closeout, Extent provenance and candidate promotion.
- `claim-action-contracts.yml`: claim/action/source alignment vocabulary.
- `extent-provenance.yml`: Extent origin, command, hash and immutability policy.
- `package-closeout-artifacts.yml`: primary close-out evidence artifacts.
- `package-policy.yml`: package publishability and protected package rules.
- `package-shape.yml`: MCP skill ownership facade for package shape, README, import and console policies.
- `profiles.yml`: TestNG execution profile taxonomy.
- `protected-roots.yml`: generated-package protected roots and allowed mutation boundaries.
- `report-hyperlinks.yml`: final report hyperlink requirements.
- `report-sections.yml`: MCP final report section requirements.
- `report-template.yml`: final report vocabulary and template contract.
- `root-cause-groups.yml`: blocker grouping and remediation vocabulary.
- `rule-profiles.yml`: rule-to-profile routing.
- `score-caps.yml`: score cap and blocker cap policy.
- `score-truth-sync.yml`: score synchronization contracts.
- `size-budgets.yml`: numeric package, class and YAML byte caps.
- `source-evolution-ledger.yml`: source-evolution ledger structure and KPI expectations.
- `candidate/html-ex-corpus.yml`: html-ex corpus classifier policy.
- `checks/mcp-checks-candidate.yml`: candidate/html-ex check metadata.
- `checks/mcp-checks-contracts.yml`: YAML/contract check metadata.
- `checks/mcp-checks-governance.yml`: governance, package-shape and advisor check metadata.
- `checks/mcp-checks-package.yml`: generated package audit check metadata.
- `checks/mcp-checks-prompt.yml`: prompt ownership and mirror check metadata.
- `checks/mcp-checks-report.yml`: final report check metadata.
- `checks/mcp-checks-run.yml`: run evidence check metadata.
- `checks/mcp-checks-source-ui-business.yml`: source/UI/business check metadata.
- `kpi/source-evolution.yml`: source-evolution KPI formulas and evidence rules.
- `suites/mcp-suites.yml`: MCP suite and check reference routing.

## Do Not

- Do not add a YAML file without listing it here.
- Do not duplicate numeric caps outside `size-budgets.yml`.
- Do not turn advisory YAML into hard blockers; integrity failures belong in executable MCP rules.

## Verification

- Run `AtfSkillArchitectureContractTest` after adding, moving or deleting contract YAML resources.
- Run `McpYamlContractSchemaTest` and `McpPackageShapeContractTest` after MCP contract changes.
