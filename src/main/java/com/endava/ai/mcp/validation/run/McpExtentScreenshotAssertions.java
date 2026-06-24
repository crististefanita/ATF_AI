package com.endava.ai.mcp.validation.run;

import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

final class McpExtentScreenshotAssertions {
    private final McpPackageAuditValidation audit;

    McpExtentScreenshotAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

    public void assertExtentReportsDoNotUseExternalScreenshotFolderReferences() throws IOException {
                List<String> externalReferences = new ArrayList<>();
                audit.forEachRun(run -> {
                    for (Path report : audit.extentReports(run)) {
                        String html = Files.readString(report);
                        if (html.contains("src=\"screenshots/") || html.contains("src='screenshots/")) {
                            externalReferences.add("External screenshot reference in " + report);
                        }
                    }
                });
                audit.assertNoSyntheticViolations("Extent screenshot embedding contract", externalReferences,
                        audit.context.packageRoot(), "run.extent.external-screenshot",
                        audit.details(
                                "Asserts: Extent proof does not depend on external screenshots/ folders.",
                                "Artifact pattern: runs/run_##/validation/**/ExtentReport*.html.",
                                "Forbidden HTML references: src=\"screenshots/ and src='screenshots/."));
            }

    public void assertExtentReportsEmbedScreenshotsWhenUiProofClaimsScreenshots() throws IOException {
                List<McpPackageViolation> weakReports = new ArrayList<>();
                audit.forEachRun(run -> {
                    for (Path report : audit.extentReports(run)) {
                        String html = Files.readString(report);
                        Matcher screenshot = McpPackageAuditValidation.BASE64_SCREENSHOT.matcher(html);
                        if (!screenshot.find()) {
                            weakReports.add(McpPackageViolation.blocker("run.extent.screenshot.missing",
                                    "Extent report has no embedded base64 screenshot", report));
                        } else if (screenshot.group().length() < 500) {
                            weakReports.add(McpPackageViolation.blocker("run.extent.screenshot.missing",
                                    "Extent embeds only a tiny placeholder image, not useful UI proof", report));
                        }
                    }
                });
                audit.assertNoViolations("Extent base64 screenshot evidence", weakReports,
                        audit.details(
                                "Asserts: Extent report embeds screenshot evidence as base64 data URI.",
                                "Artifact pattern: runs/run_##/validation/**/ExtentReport*.html.",
                                "Expected token pattern: data:image/png;base64,..."));
            }
}
