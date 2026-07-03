# MCP Skill Facades

## Purpose

This package gives agents small entry doors into the MCP skill. Each facade maps one `capability-packs.yml` row to the primary TestNG suite and repair target.

## Entry points

- `McpOrchestrationSkill`
- `McpPackageAuditSkill`
- `McpFinalReportSkill`
- `McpEvidenceProvenanceSkill`
- `McpCandidatePromotionSkill`

## Do not

- Do not put rule logic here.
- Do not put validation/assertion helpers here.
- Do not import Atlas.

## Verification

Run `AtfSkillYamlPackArchitectureContractTest` and `testng-mcp-sanity.xml`.
