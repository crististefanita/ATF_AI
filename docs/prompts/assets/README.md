# Prompt Shared Assets

This folder contains language-neutral assets used by prompt requirement documents under `docs/prompts/html_EN` and the internal localized prompt source tree.

## Rules

- Keep assets reusable across MCP, Atlas and shared prompt documents.
- Do not place language-specific files here.
- Keep `shared-report-reference.css` byte-for-byte aligned with `docs/shared-report-reference.css`.
- Final generated packages still publish their own copied CSS at `docs/out/<run>/analysis/shared-report-reference.css`.

## Verification

- Run `McpYamlContractSchemaTest#promptHtmlDocumentsUseLanguageNeutralSharedCssAsset`.
- Run `src/test/resources/skills/mcp/testng-mcp-sanity.xml` after changing assets or prompt HTML links.
