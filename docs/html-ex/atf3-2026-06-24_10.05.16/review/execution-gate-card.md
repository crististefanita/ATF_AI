# Execution Gate Card

Decision: stop same-package after final MCP gate is green; otherwise repair and rerun.

UI run gate: 5 / 5 runs, 25 / 25 UI tests passed.
MCP gate: PASS full MCP contract suite plus candidate contract; 100 tests, 0 failures, 0 errors, 0 skipped.
Fresh root trigger: only if artifact truth, source truth, report truth, XML, Extent or link audit cannot be repaired in this package.
