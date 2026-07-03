# MCP Rules

## Purpose

Executable package validators live here. A rule should answer one cold question about a generated package, then return blockers or warnings with a repair route.

## Entry points

- `McpDefaultRules`: public profile facade.
- `McpRuleRegistry`: resolves YAML-declared profiles into executable rule ids.
- `McpExecutableRuleCatalog`: maps stable rule ids to rule factories.
- `com.ai.skills.mcp.contract.execution`: publishes rule family, artifact family, cost and repair route metadata for matrices.
- `src/main/resources/skills/mcp/contracts/capability-packs.yml`: groups rules/contracts into agent-readable MCP capabilities.
- `target/mcp-governance-map.md`: generated view that connects tests, YAML check metadata, artifacts and final-report evidence.

## Change here when

- A new package-governance invariant needs executable validation.
- A current rule has one clear reason to change.
- A rule family needs a focused collaborator to reduce class size or repeated parsing.
- The governance matrix shows a repair route that is too broad or ambiguous for an agent.
- A capability pack needs to route agents to the right profile, contract and repair surface.

## Do not

- Do not add package-specific business prose to generic MCP rules.
- Do not patch ATF core, MCP core, Atlas, `ui.core` or `api.core` from generated package work.
- Do not add a rule without a stable id, contract metadata, profile route and test coverage.
- Do not grow a broad rule package when a family subpackage already exists.
- Do not raise the recursive `mcp/rules` surface cap without adding or tightening family ownership.

## Verification

- Run `testng-mcp-sanity.xml` for registry/YAML/routing health.
- Run `testng-mcp-package-closeout.xml` for a real package.
- Keep `McpContractMatrix.allProfilesRuleMatrix()` useful for an agent before adding more rules.
- Run `McpYamlCheckRefContractTest` when changing rule/test narratives.
