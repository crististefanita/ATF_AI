/**
 * Executable MCP contract suites.
 *
 * <p>The MCP TestNG profiles are split by final-closeout intent:</p>
 * <ul>
 *     <li>MCP self health: {@code testng-mcp-self.xml}, validator health.</li>
 *     <li>Run preflight: {@code testng-mcp-run-preflight.xml}, run evidence plus
 *     runtime/package accounting while calibration is still allowed.</li>
 *     <li>Full sweep: {@code testng-mcp-full.xml}, broad secondary diagnostic.</li>
 *     <li>Package audit: {@code testng-mcp-package-audit.xml}, pre-closeout.</li>
 *     <li>Final close-out: {@code testng-mcp-package-closeout.xml}, basis of
 *     the final report.</li>
 *     <li>Post-sync smoke: {@code testng-mcp-post-sync-smoke.xml}, fast
 *     synchronization check after final artifacts or links are copied.</li>
 *     <li>Final report surface close-out: {@code testng-mcp-final-report-closeout.xml},
 *     <li>LangGraph final report validation: {@code testng-mcp-langgraph-final-report.xml},
 *     last-only verification of final report shape, links, counts and browser
 *     evidence after package close-out artifacts are frozen.</li>
 *     <li>Candidate/html-ex: {@code testng-mcp-candidate.xml}, separate
 *     promotion gate.</li>
 * </ul>
 *
 * <p>The final publishability gate is
 * {@code src/test/resources/skills/mcp/testng-mcp-package-closeout.xml}. It is a
 * concrete subset of package checks and must finish with zero failures, zero
 * errors and zero skipped tests before a package can be closed. Candidate
 * promotion is intentionally isolated in {@code testng-mcp-candidate.xml}.</p>
 *
 * <p>Prefer {@code @McpSuiteRef}, {@code @ValidatesMcpContract},
 * {@code @RequiresArtifacts}, and representative {@code @McpCheckRef}
 * annotations on every new suite so failures stay actionable in reports while
 * narrative metadata remains centralized in YAML.</p>
 */
package com.ai.skills.mcp.tests;
