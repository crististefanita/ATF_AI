package com.ai.skills.mcp.validation.run.extent;

import lombok.RequiredArgsConstructor;


import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class McpExtentPortabilityAssertions {
    private static final Pattern LOCAL_SPARK_ASSET = Pattern.compile("(?i)(?:href|src)=[\"']([^\"']*spark/[^\"']+)[\"']");
    private final McpPackageAuditValidation audit;

    public void assertPerRunExtentReportsAreSingleFileProofsWithoutSparkAssets() throws IOException {
                List<McpPackageViolation> violations = new ArrayList<>();
                audit.forEachRun(run -> {
                    for (Path report : audit.extentReports(run)) {
                        violations.addAll(extentSelfContainedViolations(report, Files.readString(report)));
                    }
                });
                audit.assertNoViolations("Per-run Extent report single-file proof contract", violations,
                        audit.details(
                                "Asserts: every frozen per-run Extent report is reopenable as one HTML file.",
                                "Artifacts: runs/run_##/validation/reports/ExtentReport*.html.",
                                "Expected: no local spark/ CSS, JS, image, or font dependency beside the HTML.",
                                "Why: copied Extent HTML plus missing spark/ assets is not a portable audit proof."));
            }

    public List<McpPackageViolation> extentSelfContainedViolations(Path report, String html) {
                List<McpPackageViolation> violations = new ArrayList<>();
                Matcher matcher = LOCAL_SPARK_ASSET.matcher(html);
                Set<String> references = new java.util.LinkedHashSet<>();
                while (matcher.find()) {
                    String reference = matcher.group(1);
                    if (reference.startsWith("http://") || reference.startsWith("https://") || reference.startsWith("data:")) {
                        continue;
                    }
                    references.add(reference);
                }
                if (!references.isEmpty()) {
                    violations.add(McpPackageViolation.blocker("run.extent.not-self-contained",
                            "Extent report must be a single-file frozen proof; local spark assets are not allowed: "
                                    + references.stream().limit(5).collect(java.util.stream.Collectors.toList()),
                            report));
                }
                return violations;
            }
}
