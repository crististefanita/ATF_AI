package com.endava.ai.mcp.validation.run;

import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class McpGateEvidenceAssertions {
    private final McpPackageAuditValidation audit;

    McpGateEvidenceAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

    public void assertFrozenMcpExtentReportContainsReadableContractDetails() throws IOException {
                Path report = audit.context.packageRoot().resolve("validation/mcp/ExtentReport.html");
                List<String> blockers = new ArrayList<>();
                if (!Files.isRegularFile(report)) {
                    blockers.add("Missing frozen MCP Extent report: validation/mcp/ExtentReport.html");
                } else {
                    String html = Files.readString(report);
                    audit.requireAnyToken(html, List.of("MCP", "mcp"),
                            "MCP Extent report does not identify the MCP contract suite", blockers);
                    audit.requireAnyToken(html, List.of("Complete MCP generated package audit", "MCP audit passed", "Validation axis:", "Generated package validator"),
                            "MCP Extent report does not expose readable audit steps/details", blockers);
                    audit.requireAnyToken(html, List.of("Asserts:", "Artifacts:", "Expected:", "Runtime keys:", "Relative path", "Outcome:"),
                            "MCP Extent report lacks contract details explaining what was validated", blockers);
                    audit.requireAnyToken(html, List.of("package-state.json", "run-state.json", "root-source-thin-no-index.diff",
                                    "Business Flow Graph", "Score Overview"),
                            "MCP Extent report lacks concrete package artifact names", blockers);
                    long passTokens = Pattern.compile("(?i)\\bpass\\b").matcher(html).results().count();
                    if (passTokens < 5) {
                        blockers.add("MCP Extent report has too little visible pass/detail evidence: pass token count " + passTokens);
                    }
                }
                audit.assertNoSyntheticViolations("Frozen MCP Extent report detail contract", blockers, report,
                        "mcp.extent.details.missing",
                        audit.details(
                                "Asserts: the saved MCP Extent report is readable as a contract audit, not only a green shell.",
                                "Artifact: validation/mcp/ExtentReport.html.",
                                "Expected detail tokens: Asserts, Artifacts, Expected, runtime keys, package-state, run-state, diff, graph, score."));
            }

    public void assertFrozenMcpGateResultMatchesCurrentValidatorState() throws IOException {
                Path gate = audit.context.packageRoot().resolve("validation/mcp/mcp-gate-result.md");
                List<String> blockers = new ArrayList<>();
                if (!Files.isRegularFile(gate)) {
                    blockers.add("Missing frozen MCP gate result: validation/mcp/mcp-gate-result.md");
                } else {
                    String text = Files.readString(gate);
                    boolean claimsPass = Pattern.compile("(?i)\\bresult\\s*:\\s*pass\\b|no\\s+mcp\\s+blocker\\s+remains|passed=\\d+").matcher(text).find();
                    if (claimsPass && audit.context.result().hasBlockers()) {
                        blockers.add("Frozen MCP gate result claims PASS, but the current validator has "
                                + audit.context.result().blockerCount() + " blocker(s)");
                    }
                    boolean claimsPending = Pattern.compile("(?i)\\b(result|status)\\s*:\\s*pending\\b|\\bpending\\b")
                            .matcher(text)
                            .find();
                    if (claimsPending && !audit.context.result().hasBlockers()) {
                        blockers.add("Frozen MCP gate result is still PENDING, but the current validator has no blocker");
                    }
                }
                audit.assertNoSyntheticViolations("Frozen MCP gate result freshness", blockers, gate,
                        "mcp.gate-result.stale-pass",
                        audit.details(
                                "Asserts: frozen validation/mcp/mcp-gate-result.md agrees with the current validator state.",
                                "Artifact: validation/mcp/mcp-gate-result.md.",
                                "Expected: no PASS/no-blocker claim when the current MCP validator reports blockers; no PENDING claim after the current validator is clean.",
                                "Reason: MCP evidence must be regenerated after changing rules, sources, report, or package artifacts."));
            }

    public void assertExtentProofClassIsPublishedAndReflectedInScoring() throws IOException {
                List<String> classes = new ArrayList<>();
                audit.forEachRun(run -> {
                    for (Path report : audit.extentReports(run)) {
                        classes.add(classifyExtent(Files.readString(report)));
                    }
                });
                Set<String> distinctClasses = new java.util.LinkedHashSet<>(classes);
                String readme = Files.readString(audit.context.packageRoot().resolveSibling("README.md")).toLowerCase();
                String finalReport = audit.readFinalReport().toLowerCase();
                String state = Files.readString(audit.context.packageRoot().resolve("review/package-state.json"));
                int overall = extractJsonInt(state, "overall", 101);
                List<McpPackageViolation> violations = new ArrayList<>();

                if (distinctClasses.isEmpty()) {
                    violations.add(McpPackageViolation.blocker("run.extent.classification.missing",
                            "No per-run Extent proof class could be derived", audit.context.packageRoot()));
                }
                boolean hasSynthetic = distinctClasses.contains("synthetic");
                boolean hasStandalone = distinctClasses.contains("standalone");
                boolean weakClass = hasSynthetic || hasStandalone;
                String combined = readme + "\n" + finalReport;
                if (weakClass && !audit.containsAny(combined, "synthetic", "standalone", "custom proof carrier", "weak execution evidence", "weak product evidence")) {
                    violations.add(McpPackageViolation.blocker("run.extent.classification.unpublished",
                            "Per-run Extent class is " + distinctClasses + " but README/final report do not publish the weak proof class",
                            audit.finalReport()));
                }
                if (hasSynthetic && overall > 60) {
                    violations.add(McpPackageViolation.blocker("run.extent.synthetic.score-too-high",
                            "Synthetic/custom per-run Extent proof cannot support Overall above 60; published Overall is " + overall,
                            audit.context.packageRoot().resolve("review/package-state.json")));
                }
                if (weakClass && !combined.contains("not real ui-visible") && !combined.contains("not ui-owned")
                        && !combined.contains("not product-owned") && !combined.contains("not strong product proof")) {
                    violations.add(McpPackageViolation.blocker("run.extent.classification.score-context-missing",
                            "Weak Extent proof class is not tied to an explicit UI/product-proof cap",
                            audit.finalReport()));
                }
                audit.assertNoViolations("Extent proof class is published and reflected in scoring",
                        violations,
                        audit.details(
                                "Asserts: per-run Extent proof is classified as spark, standalone, or synthetic.",
                                "Artifacts: runs/run_##/validation/reports/ExtentReport.html, README.md, final report, package-state.json.",
                                "Expected: synthetic/standalone proof remains weak evidence and keeps Overall capped; Spark proof may support stronger evidence only with UI-owned product proof.",
                                "Observed classes: " + distinctClasses));
            }

    private String classifyExtent(String html) {
                String lower = html.toLowerCase();
                if (lower.contains("spark-style.css") || lower.contains("cdn.jsdelivr") || lower.contains("extent.spark")) {
                    return "spark";
                }
                if (lower.contains("spark-replacement") || lower.contains("single-file-proof")
                        || lower.contains("frozen audit view from executed xml")) {
                    return "synthetic";
                }
                if (lower.contains("extent-framework") || lower.contains("extent report") || lower.contains("extentreports")) {
                    return "standalone";
                }
                return "synthetic";
            }

    private int extractJsonInt(String json, String key, int fallback) {
                Matcher matcher = Pattern.compile("(?is)\"" + Pattern.quote(key) + "\"\\s*:\\s*\"?(\\d+)\"?").matcher(json);
                return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
            }
}
