package com.endava.ai.skills.mcp.validation.run.extent;

import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class McpExtentPresenceAssertions {
    private final McpPackageAuditValidation audit;
    private final McpExtentXmlAlignmentAssertions xmlAlignment;
    private final McpExtentPortabilityAssertions portability;

    public McpExtentPresenceAssertions(McpPackageAuditValidation audit, McpExtentXmlAlignmentAssertions xmlAlignment, McpExtentPortabilityAssertions portability) {
        this.audit = audit;
        this.xmlAlignment = xmlAlignment;
        this.portability = portability;
    }

    public void assertEveryRunHasExtentReport() throws IOException {
                List<String> missing = new ArrayList<>();
                audit.forEachRun(run -> {
                    if (audit.extentReports(run).isEmpty()) {
                        missing.add("Missing Extent report for " + run);
                    }
                });
                audit.assertNoSyntheticViolations("Per-run Extent report presence", missing, audit.context.packageRoot(), "run.extent.missing",
                        audit.details(
                                "Asserts: each run has at least one Extent HTML report.",
                                "Relative path pattern: runs/run_##/validation/reports/*.html or runs/run_##/validation/*ExtentReport*.html.",
                                "Expected run count: " + audit.context.runtime().metaIterationCount()));
            }

    public void assertPerRunExtentReportsAreGeneratedAndSubstantive() throws IOException {
                List<McpPackageViolation> weakReports = new ArrayList<>();
                audit.forEachRun(run -> {
                    for (Path report : audit.extentReports(run)) {
                        String html = Files.readString(report);
                        String lower = html.toLowerCase();
                        if (html.length() < 10_000) {
                            weakReports.add(McpPackageViolation.blocker("run.extent.toy-report",
                                    "Extent report is too small for audit-grade evidence: " + html.length() + " chars", report));
                        }
                        if (!audit.containsAny(lower, "spark-style.css", "test-view", "dashboard-view", "test-list-item",
                                "extent-framework", "extent report", "extentreports")) {
                            weakReports.add(McpPackageViolation.blocker("run.extent.toy-report",
                                    "Extent report does not contain recognizable Extent/Spark shell tokens", report));
                        }
                        if (Pattern.compile("(?i)\\bpass\\s+pass\\s+pass\\s+pass\\s+pass\\b").matcher(html).find()) {
                            weakReports.add(McpPackageViolation.blocker("run.extent.toy-report",
                                    "Extent report looks hand-written: repeated PASS tokens are not execution detail", report));
                        }
                        if (!audit.containsAny(lower, "step", "details", "expected", "actual", "log", "status", "duration")) {
                            weakReports.add(McpPackageViolation.blocker("run.extent.toy-report",
                                    "Extent report lacks readable execution details such as steps, expected/actual, logs, status, or duration", report));
                        }
                        weakReports.addAll(McpExtentReportMode.fromHtml(html).consistencyViolations(report, html));
                        weakReports.addAll(xmlAlignment.extentXmlMismatchViolations(run, report, html));
                        weakReports.addAll(portability.extentSelfContainedViolations(report, html));
                    }
                });
                audit.assertNoViolations("Per-run Extent report generated shell and detail contract", weakReports,
                        audit.details(
                                "Asserts: per-run Extent reports are generated audit artifacts, not tiny handmade PASS shells.",
                                "Artifacts: runs/run_##/validation/reports/ExtentReport*.html.",
                                "Expected: Extent/Spark shell tokens, meaningful size, embedded non-placeholder screenshot proof, readable step/detail/status evidence, XML-method alignment, and single-file portability.",
                                "Forbidden: minimal HTML containing only PASS PASS PASS and a 1x1 base64 placeholder."));
            }
}
