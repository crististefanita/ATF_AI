# Iterative Governed Execution Atlas

This file is routing memory only. It is not a package HTML template and it is not the owner of MCP, Atlas or shared report requirements.

## Current Owners

| Lane | Owner | Current source |
| --- | --- | --- |
| MCP agentic governance | MCP skill | `docs/prompts/html_ro/mcp` and `docs/prompts/html_EN/mcp` |
| Atlas business understanding | Atlas skill | `docs/prompts/html_ro/atlas` and `docs/prompts/html_EN/atlas` |
| Shared prompt primitives | Shared prompt assets | `docs/prompts/html_ro/shared`, `docs/prompts/html_EN/shared`, `docs/prompts/assets` |
| Prompt ownership | Executable manifest | `docs/prompts/html-contract-ownership.yml` |
| Core framework/library | ATF Core | `src/main/java/com/ai/core`, `src/main/java/com/ai/api/core`, `src/main/java/com/ai/ui/core` |

## Read Order

1. For package legality and run lifecycle, open `docs/prompts/html_ro/mcp/orchestrate-iterative-runs.html`.
2. For one-run execution and evidence, open `docs/prompts/html_ro/mcp/execute-and-understand-run.html`.
3. For final MCP publication requirements, open `docs/prompts/html_ro/mcp/reporting.html`.
4. For business understanding, source trace, graph proof and score truth, open `docs/prompts/html_ro/atlas/business-test-atlas-reference.html`.
5. For reusable shell/score/graph/link primitives, open `docs/prompts/html_ro/shared`.

## Skill Commands

| Intent | Minimal route |
| --- | --- |
| Use MCP skill for generated-package close-out | Start with `html_ro/mcp/orchestrate-iterative-runs.html`; run MCP sanity first; finish with package-closeout, post-sync-smoke and final-report-closeout. |
| Use Atlas skill for business understanding | Start with `html_ro/atlas/business-test-atlas-reference.html`; run Atlas sanity first; finish with the business-closeout profile. Use the full profile only for diagnostics, including adapter parity. |
| Change Core library behavior | Start with `src/main/java/com/ai/core/README.md`; run Core sanity first; keep MCP/Atlas rules outside Core. |

## Execution Profiles

| Product | Canonical sanity | Deep/final profile | Meaning |
| --- | --- | --- | --- |
| MCP skill | `src/test/resources/skills/mcp/testng-mcp-sanity.xml` | `src/test/resources/skills/mcp/testng-mcp-package-closeout.xml` and `src/test/resources/skills/mcp/testng-mcp-final-report-closeout.xml` | package governance and final report close-out |
| Atlas skill | `src/test/resources/skills/atlas/testng-atlas-sanity.xml` | `src/test/resources/skills/atlas/testng-atlas-business-closeout.xml`; `src/test/resources/skills/atlas/testng-atlas.xml` remains full diagnostics | business understanding, Atlas audit quality, optional adapter parity diagnostics |
| ATF Core library | `src/test/resources/atf/core/suites/core/testng-atf-core-sanity.xml` | `src/test/resources/atf/core/suites/core/testng-atf-core.xml` | generic framework/runtime health |

MCP and Atlas must stay independently runnable. They may exchange evidence through YAML, report links, manifests and published artifacts, but they must not require Java imports from each other.

Candidate/html-ex promotion is not part of generated-package close-out. It runs only after MCP package-closeout and final-report-closeout already pass.

## Historical Note

The old `docs/prompts/iterativ` folder was retired. Current prompt sources live under `docs/prompts/html_ro`, mirrored by `docs/prompts/html_EN`, and governed by `docs/prompts/html-contract-ownership.yml`.
