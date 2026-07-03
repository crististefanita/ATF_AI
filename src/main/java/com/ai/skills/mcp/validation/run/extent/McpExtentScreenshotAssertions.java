package com.ai.skills.mcp.validation.run.extent;

import lombok.RequiredArgsConstructor;


import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class McpExtentScreenshotAssertions {
    private static final String FINAL_STATE_KEY = "ui.screenshots.capture.final.state";
    private static final Pattern FINAL_STATE_WORDS = Pattern.compile(
            "(?i)(final state|final-state|final screenshot|final ui|captura finala|stare finala|after test|test final)");
    private static final Pattern CONFIDENT_RUN_STATUS = Pattern.compile(
            "(?i)(countable|owned|kept|met|pass|proof_ui\"\\s*:\\s*\"?yes|validation/reports/ExtentReport)");

    private final McpPackageAuditValidation audit;

    public void assertExtentReportsDoNotUseExternalScreenshotFolderReferences() throws IOException {
                List<String> externalReferences = new ArrayList<>();
                audit.forEachRun(run -> {
                    for (Path report : canonicalExtentReports(run)) {
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
                boolean finalStateScreenshotsRequired = finalStateScreenshotsRequired(weakReports);
                audit.forEachRun(run -> {
                    boolean runHasFinalStateBlocker = false;
                    for (Path report : canonicalExtentReports(run)) {
                        String html = Files.readString(report);
                        int before = weakReports.size();
                        List<McpExtentScreenshotEvidence.EmbeddedScreenshot> screenshots =
                                McpExtentScreenshotEvidence.findEmbedded(html);
                        if (screenshots.isEmpty()) {
                            weakReports.add(McpPackageViolation.blocker("run.extent.screenshot.missing",
                                    "Extent report has no embedded base64 screenshot media", report));
                        } else if (screenshots.stream().noneMatch(screenshot -> screenshot.dataUriLength() >= 500)) {
                            weakReports.add(McpPackageViolation.blocker("run.extent.screenshot.missing",
                                    "Extent embeds only tiny placeholder screenshot media, not useful UI proof", report));
                        }
                        if (finalStateScreenshotsRequired) {
                            requireFinalStateScreenshot(report, html, weakReports);
                        }
                        runHasFinalStateBlocker = runHasFinalStateBlocker || weakReports.size() > before;
                    }
                    if (finalStateScreenshotsRequired && runHasFinalStateBlocker) {
                        requireRunStateDoesNotOverclaimUiProof(run, weakReports);
                    }
                });
                audit.assertNoViolations("Extent base64 screenshot evidence", weakReports,
                        audit.details(
                                "Asserts: Extent report embeds screenshot evidence as base64 data URI.",
                                "Framework switch: " + FINAL_STATE_KEY + "=" + finalStateScreenshotsRequired + ".",
                                "Accepted evidence: <img src=\"data:image/png;base64,...\"> or native Extent media anchor with href=\"data:image/png;base64,...\".",
                                "When true, each run Extent must expose a visible final-state UI screenshot, not only hidden generic media.",
                                "Artifact pattern: runs/run_##/validation/**/ExtentReport*.html.",
                                "Expected token pattern: data:image/png;base64,..."));
            }

    private boolean finalStateScreenshotsRequired(List<McpPackageViolation> weakReports) throws IOException {
        Path frameworkProperties = audit.context.packageRoot()
                .resolve("review/framework.properties")
                .normalize();
        Properties properties = new Properties();
        if (!Files.isRegularFile(frameworkProperties)) {
            weakReports.add(McpPackageViolation.blocker("framework-properties.snapshot.missing-for-screenshot-contract",
                    "Cannot decide final-state screenshot obligation because review/framework.properties is missing",
                    frameworkProperties));
            return false;
        }
        try (var reader = Files.newBufferedReader(frameworkProperties)) {
            properties.load(reader);
        }
        return Boolean.parseBoolean(properties.getProperty(FINAL_STATE_KEY, "false").trim());
    }

    private void requireRunStateDoesNotOverclaimUiProof(String run, List<McpPackageViolation> weakReports) throws IOException {
        Path runState = audit.runRoot(run).resolve("review/run-state.json");
        if (!Files.isRegularFile(runState)) {
            return;
        }
        String content = Files.readString(runState);
        if (CONFIDENT_RUN_STATUS.matcher(content).find()) {
            weakReports.add(McpPackageViolation.blocker("run-state.ui-proof-status.overclaimed",
                    "run-state publishes a countable/kept UI proof status, but framework.properties requires final-state screenshots and the run Extent proof is incomplete",
                    runState));
        }
    }

    private void requireFinalStateScreenshot(Path report, String html, List<McpPackageViolation> weakReports) {
        List<McpExtentScreenshotEvidence.EmbeddedScreenshot> screenshots = McpExtentScreenshotEvidence.findEmbedded(html);
        int visibleScreenshots = 0;
        int hiddenScreenshots = 0;
        boolean hasFinalStateLabel = false;
        for (McpExtentScreenshotEvidence.EmbeddedScreenshot screenshot : screenshots) {
            boolean hidden = screenshot.isHiddenIn(html);
            if (hidden) {
                hiddenScreenshots++;
            } else {
                visibleScreenshots++;
                if (FINAL_STATE_WORDS.matcher(screenshot.textContext(html)).find()) {
                    hasFinalStateLabel = true;
                }
            }
        }
        if (visibleScreenshots == 0 && hiddenScreenshots == 0) {
            weakReports.add(McpPackageViolation.blocker("run.extent.final-state-screenshot.missing",
                    "framework.properties requires final-state screenshots, but Extent has no embedded screenshot media",
                    report));
        }
        if (hiddenScreenshots > 0 && visibleScreenshots == 0) {
            weakReports.add(McpPackageViolation.blocker("run.extent.final-state-screenshot.hidden",
                    "framework.properties requires final-state screenshots, but the embedded screenshot media is hidden from the Extent report",
                    report));
        }
        if (visibleScreenshots > 0 && !hasFinalStateLabel) {
            weakReports.add(McpPackageViolation.blocker("run.extent.final-state-screenshot.unlabeled",
                    "framework.properties requires final-state screenshots, but no embedded screenshot is labelled as final-state UI proof",
                    report));
        }
    }

    private List<Path> canonicalExtentReports(String run) throws IOException {
        Path reports = audit.runRoot(run).resolve(McpRunArtifacts.REPORTS_ROOT);
        List<Path> canonical = audit.findHtml(reports);
        return canonical.isEmpty() ? audit.extentReports(run) : canonical;
    }
}
