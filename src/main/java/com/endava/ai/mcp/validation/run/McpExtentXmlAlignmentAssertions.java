package com.endava.ai.mcp.validation.run;

import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class McpExtentXmlAlignmentAssertions {
    private static final Pattern XML_PASSED_METHOD_NAME = Pattern.compile(
            "(?is)<test-method\\s+[^>]*name=[\"']([^\"']+)[\"'][^>]*status=[\"']PASS[\"']");
    private final McpPackageAuditValidation audit;

    McpExtentXmlAlignmentAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

    List<McpPackageViolation> extentXmlMismatchViolations(String run, Path report, String html) throws IOException {
                List<McpPackageViolation> violations = new ArrayList<>();
                Set<String> currentMethods = passedXmlMethodNames(audit.readRunFile(run, "validation/testng-results.xml"));
                for (String method : currentMethods) {
                    if (!html.contains(method)) {
                        violations.add(McpPackageViolation.blocker("run.extent.xml-method-missing",
                                "Extent report does not expose its own XML method: " + method, report));
                    }
                }

                Set<String> otherMethods = new java.util.LinkedHashSet<>();
                audit.forEachRun(otherRun -> {
                    if (!otherRun.equals(run)) {
                        otherMethods.addAll(passedXmlMethodNames(audit.readRunFile(otherRun, "validation/testng-results.xml")));
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

    private Set<String> passedXmlMethodNames(String xml) {
                Set<String> names = new java.util.LinkedHashSet<>();
                Matcher matcher = XML_PASSED_METHOD_NAME.matcher(xml);
                while (matcher.find()) {
                    names.add(matcher.group(1));
                }
                return names;
            }
}
