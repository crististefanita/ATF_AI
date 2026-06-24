# ROOT_RUN 2026-06-23_15.52.07

Business object: registered account invoice details page with purchased line item.

| Axis | Score | Verdict |
|---|---:|---|
| Business Substance | 94 | details route plus Products table line item and total |
| Business Graph | 93 | customer -> checkout -> invoice row -> details route -> reopen/relogin |
| Historical Reopenability | 92 | root diff plus focused run diffs and per-run proof |
| Proof Chain | 94 | claim -> source -> diff -> state -> XML/Extent/command -> report |
| Contract Artifact | 93 | 5 / 5 runs, 25 / 25 UI tests |
| Overall package | 93 | capped: no full order history or payment history |

Cold Overall package score: 93 / 100.

Final report: [analysis/langgraph-business-understanding.html](analysis/langgraph-business-understanding.html)

MCP gate result: [validation/mcp/mcp-gate-result.md](validation/mcp/mcp-gate-result.md)

MCP gate output: [validation/mcp/mcp-gate-output.txt](validation/mcp/mcp-gate-output.txt)

MCP Extent report: [validation/mcp/ExtentReport.html](validation/mcp/ExtentReport.html)

new_frontier = registered-account-invoice-products-after-authenticated-checkout-reopen-and-relogin.
weakest_axis = business breadth beyond invoice details.
proof_class = UI Extent + TestNG XML + command + source diff + run state.

Overall: 93





MCP gate: 93 / 93 PASS.

