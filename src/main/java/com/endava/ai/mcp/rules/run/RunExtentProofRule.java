package com.endava.ai.mcp.rules.run;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpRunArtifacts;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@com.endava.ai.mcp.contract.McpContract(id = "run.window.extent-proof",
        category = com.endava.ai.mcp.contract.McpRuleCategory.RUN,
        htmlReq = "execute-and-understand-run.html",
        artifact = "runs/run_##/validation/reports/ExtentReport_*.html")
public final class RunExtentProofRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (Path runDirectory : RunRuleSupport.runDirectories(context, result)) {
            requireExtentReports(context, runDirectory, result);
        }
    }

    private void requireExtentReports(McpPackageContext context,
                                      Path runDirectory,
                                      McpPackageValidationResult result) {
        Path reports = runDirectory.resolve(McpRunArtifacts.REPORTS_ROOT);
        List<Path> extentReports = RunRuleSupport.extentReports(context, runDirectory, result);
        if (extentReports.isEmpty()) {
            result.add(McpPackageViolation.blocker("run.extent.missing",
                    "Missing Extent HTML report under validation/reports", reports));
        }
        for (Path extentReport : extentReports) {
            String html = context.files().read(extentReport, result);
            String lower = html.toLowerCase(Locale.ROOT);
            requireExtentLooksReal(extentReport, html, lower, result);
            requireEmbeddedScreenshot(extentReport, html, result);
            requireExtentMatchesRunXml(context, runDirectory, extentReport, html, result);
            requireExtentIsSelfContainedSingleFile(extentReport, html, result);
        }
    }

    private void requireExtentLooksReal(Path extentReport,
                                        String html,
                                        String lower,
                                        McpPackageValidationResult result) {
        if (html.length() < RunRuleSupport.MIN_EXTENT_REPORT_CHARS) {
            result.add(McpPackageViolation.blocker("run.extent.toy-report",
                    "Extent report is too small for audit-grade evidence: " + html.length() + " chars",
                    extentReport));
        }
        if (!McpRuleSupport.containsAny(lower, "spark-style.css", "test-view", "dashboard-view",
                "test-list-item", "extent-framework", "extent report", "extentreports")) {
            result.add(McpPackageViolation.blocker("run.extent.toy-report",
                    "Extent report does not contain recognizable Extent/Spark shell tokens", extentReport));
        }
        if (RunRuleSupport.REPEATED_PASS_PLACEHOLDER.matcher(html).find()) {
            result.add(McpPackageViolation.blocker("run.extent.toy-report",
                    "Extent report looks hand-written: repeated PASS tokens are not execution detail", extentReport));
        }
        if (!McpRuleSupport.containsAny(lower, "step", "details", "expected", "actual", "log", "status", "duration")) {
            result.add(McpPackageViolation.blocker("run.extent.toy-report",
                    "Extent report lacks readable execution details such as steps, expected/actual, logs, status, or duration",
                    extentReport));
        }
    }

    private void requireEmbeddedScreenshot(Path extentReport,
                                           String html,
                                           McpPackageValidationResult result) {
        Matcher screenshot = RunRuleSupport.BASE64_SCREENSHOT.matcher(html);
        if (!screenshot.find()) {
            result.add(McpPackageViolation.blocker("run.extent.screenshot.missing",
                    "Extent exists, but no embedded base64 screenshot was found", extentReport));
        } else if (screenshot.group().length() < 500) {
            result.add(McpPackageViolation.blocker("run.extent.screenshot.missing",
                    "Extent embeds only a tiny placeholder image, not useful UI proof", extentReport));
        }
    }

    private void requireExtentMatchesRunXml(McpPackageContext context,
                                            Path runDirectory,
                                            Path extentReport,
                                            String html,
                                            McpPackageValidationResult result) {
        Path xml = runDirectory.resolve(McpRunArtifacts.TESTNG_RESULTS);
        if (!Files.isRegularFile(xml)) {
            return;
        }
        Set<String> currentRunMethods = RunRuleSupport.passedXmlMethodNames(context.files().read(xml, result));
        if (currentRunMethods.isEmpty()) {
            return;
        }

        List<String> missingOwnMethods = currentRunMethods.stream()
                .filter(method -> !html.contains(method))
                .collect(Collectors.toList());
        if (!missingOwnMethods.isEmpty()) {
            result.add(McpPackageViolation.blocker("run.extent.xml-method-missing",
                    "Extent report does not expose its own XML method(s): " + missingOwnMethods,
                    extentReport));
        }

        Set<String> allOtherRunMethods = new HashSet<>();
        Path runsRoot = runDirectory.getParent();
        if (runsRoot != null && Files.isDirectory(runsRoot)) {
            for (Path sibling : context.files().runDirectories(runsRoot, result)) {
                if (sibling.equals(runDirectory)) {
                    continue;
                }
                Path siblingXml = sibling.resolve(McpRunArtifacts.TESTNG_RESULTS);
                if (Files.isRegularFile(siblingXml)) {
                    allOtherRunMethods.addAll(RunRuleSupport.passedXmlMethodNames(context.files().read(siblingXml, result)));
                }
            }
        }

        List<String> extraOtherRunMethods = allOtherRunMethods.stream()
                .filter(html::contains)
                .limit(8)
                .collect(Collectors.toList());
        if (!extraOtherRunMethods.isEmpty()) {
            result.add(McpPackageViolation.blocker("run.extent.full-suite-copy",
                    "Extent report contains methods from other runs; per-run proof is likely a copied full-suite report. Examples: "
                            + extraOtherRunMethods,
                    extentReport));
        }
    }

    private void requireExtentIsSelfContainedSingleFile(Path extentReport,
                                                        String html,
                                                        McpPackageValidationResult result) {
        Matcher matcher = RunRuleSupport.LOCAL_SPARK_ASSET.matcher(html);
        Set<String> references = new HashSet<>();
        while (matcher.find()) {
            String reference = matcher.group(1);
            if (reference.startsWith("http://") || reference.startsWith("https://") || reference.startsWith("data:")) {
                continue;
            }
            references.add(reference);
        }
        if (!references.isEmpty()) {
            result.add(McpPackageViolation.blocker("run.extent.not-self-contained",
                    "Extent report must be a single-file frozen proof; local spark assets are not allowed: "
                            + references.stream().limit(5).collect(Collectors.toList()),
                    extentReport));
        }
    }
}
