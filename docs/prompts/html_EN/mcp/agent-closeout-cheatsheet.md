# Agent MCP Close-Out Cheatsheet

Canonical code source: `ContractCatalog.closeoutEvidence().closeoutPlan()`. Keep this checklist aligned with that typed plan.

1. Generate MCP close-out evidence with `testng-mcp-package-closeout.xml`; do not publish `testng-mcp-full.xml` as the final package gate.
2. Run close-out with explicit `-Dmcp.package.root=<package-root>` and `-Dmcp.project.root=<project-root>`.
3. Generate the final MCP Extent through Maven/TestListener with `-Dreporting.engine=extent`; do not patch Extent HTML by hand.
4. Freeze the full `validation/mcp` spine together: Extent, sha256, origin, XML, method ledger, gate result and KPI artifacts.
5. Run `testng-mcp-post-sync-smoke.xml` after copying or regenerating frozen MCP artifacts.
6. Run `testng-mcp-final-report-closeout.xml` after the final report links frozen MCP evidence.
7. Keep candidate/html-ex promotion separate from package close-out.
8. Treat `testng-mcp-full.xml` as diagnostic health evidence; if it has accepted skips, publish the reason and keep score caps honest.
9. Raise score only from real business proof and artifact truth, not from cosmetic report edits.
10. If a protected root needs a change, stop and report it explicitly instead of hiding it in package artifacts.
