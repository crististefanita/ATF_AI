package com.ai.skills.mcp.validation.run.extent;

import lombok.RequiredArgsConstructor;


import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.ai.skills.mcp.validation.run.McpTestNgXmlMethods;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public final class McpExtentXmlAlignmentAssertions {
    private final McpPackageAuditValidation audit;

    public List<McpPackageViolation> extentXmlMismatchViolations(String run, Path report, String html) throws IOException {
                List<McpPackageViolation> violations = new ArrayList<>();
                Set<String> currentMethods = McpTestNgXmlMethods.passedRealMethodNames(
                        audit.readRunFile(run, "validation/testng-results.xml"));
                for (String method : currentMethods) {
                    if (!html.contains(method)) {
                        violations.add(McpPackageViolation.blocker("run.extent.xml-method-missing",
                                "Extent report does not expose its own XML method: " + method, report));
                    }
                }

                Set<String> otherMethods = new java.util.LinkedHashSet<>();
                audit.forEachRun(otherRun -> {
                    if (!otherRun.equals(run)) {
                        otherMethods.addAll(McpTestNgXmlMethods.passedRealMethodNames(
                                audit.readRunFile(otherRun, "validation/testng-results.xml")));
                    }
                });
                List<String> extra = otherMethods.stream()
                        .filter(html::contains)
                        .limit(8)
                        .collect(java.util.stream.Collectors.toList());
                if (!extra.isEmpty()) {
                    violations.add(McpPackageViolation.blocker("run.extent.full-suite-copy",
                            "Extent report contains XML methods from other runs; per-run proof is likely a copied full-suite report. Examples: " + extra,
                            report));
                }
                return violations;
            }
}
