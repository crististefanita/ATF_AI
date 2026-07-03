# Atlas Skill Facades

## Purpose

This package gives agents small entry doors into the Atlas skill. Each facade maps one `rule-packs.yml` row to the primary TestNG suite and repair target.

## Entry points

- `AtlasBusinessSemanticsSkill`
- `AtlasGraphProofSkill`
- `AtlasScoreTruthSkill`
- `AtlasAuditPublicationSkill`

## Do not

- Do not put rule logic here.
- Do not put Reporting shell logic here.
- Do not import MCP.

## Verification

Run `AtfSkillYamlPackArchitectureContractTest` and `testng-atlas-sanity.xml`.
