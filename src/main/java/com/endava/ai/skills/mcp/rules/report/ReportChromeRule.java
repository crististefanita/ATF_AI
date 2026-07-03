package com.endava.ai.skills.mcp.rules.report;

import com.endava.ai.core.reporting.htmlcontract.model.HtmlReportShellProfile;
import com.endava.ai.core.reporting.htmlcontract.validation.HtmlReportShellValidator;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@McpContract(id = "report.chrome.shared-css",
        category = McpRuleCategory.REPORT,
        htmlReq = "reporting.html",
        artifact = "analysis/langgraph-business-understanding.html")
public final class ReportChromeRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path report = context.paths().report();
        if (!Files.isRegularFile(report)) {
            return;
        }

        String html = context.files().read(report, result);
        requireSharedShell(html, report, result);
        requireStructuralCollapseTruth(html, report, result);
        requireCssContract(context, result);
    }

    private void requireSharedShell(String html, Path report, McpPackageValidationResult result) {
        new HtmlReportShellValidator().validate(html, HtmlReportShellProfile.MCP_FINAL_REPORT)
                .forEach(violation -> result.add(McpPackageViolation.blocker(
                        violation.code(), violation.message(), report)));
    }

    private void requireStructuralCollapseTruth(String html, Path report, McpPackageValidationResult result) {
        Pattern sectionHintPattern = Pattern.compile(
                "class=[\"'][^\"']*(?:sumhint|pill)[^\"']*[\"'][^>]*>([^<]+)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = sectionHintPattern.matcher(html);
        while (matcher.find()) {
            String label = matcher.group(1).replace("&nbsp;", " ").trim();
            if (isDecorativeCollapseLabel(label)) {
                result.add(McpPackageViolation.blocker("report.section-hint.decorative-collapse-state",
                        "Section pills/sumhints must explain the section contents; collapse state belongs to the <details> structure, not to decorative wording: " + label,
                        report));
            }
        }
    }

    private boolean isDecorativeCollapseLabel(String label) {
        String normalized = label.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.equals("closed") || normalized.equals("open")
                || normalized.equals("collapsed") || normalized.equals("expanded")) {
            return true;
        }
        return (normalized.contains("default") || normalized.contains("implicitly"))
                && containsAny(normalized, "closed", "open", "collapsed", "expanded");
    }

    private void requireCssContract(McpPackageContext context, McpPackageValidationResult result) {
        Path css = context.paths().reportCss();
        if (!Files.isRegularFile(css)) {
            return;
        }
        String cssContent = context.files().read(css, result);
        requireExactProjectCssReuse(context, css, cssContent, result);
        requireText(cssContent, css, result, "report.css-shell",
                "Shared CSS must style the report shell", "body", "details", "summary");
        requireText(cssContent, css, result, "report.css-business",
                "Shared CSS must style graph or score presentation", ".graph", "svg", ".score", ".business");
        requireVisualTheme(cssContent, css, result);
    }

    private void requireExactProjectCssReuse(McpPackageContext context,
                                             Path packageCss,
                                             String packageCssContent,
                                             McpPackageValidationResult result) {
        Path projectCss = context.projectRoot().resolve(McpArtifactPaths.PROJECT_SHARED_REPORT_CSS).normalize();
        if (!Files.isRegularFile(projectCss)) {
            result.add(McpPackageViolation.blocker("report.css.shared-reference-copy-drift",
                    "Missing project-level shared CSS reference: " + projectCss, projectCss));
            return;
        }
        String expected = context.files().read(projectCss, result).replace("\r\n", "\n");
        String actual = packageCssContent.replace("\r\n", "\n");
        if (!actual.equals(expected)) {
            result.add(McpPackageViolation.blocker("report.css.shared-reference-copy-drift",
                    "analysis/shared-report-reference.css must be an exact copy of docs/shared-report-reference.css",
                    packageCss));
        }
    }

    private void requireVisualTheme(String cssContent, Path css, McpPackageValidationResult result) {
        String normalized = cssContent.toLowerCase(Locale.ROOT).replace(" ", "");
        if (normalized.contains("body{") && containsAny(normalized,
                "background:#fff", "background:white", "background:#ffffff", "background:#fafafa")) {
            result.add(McpPackageViolation.blocker("report.visual-theme.plain-white",
                    "Body background still looks like a plain white/default document shell", css));
        }
        if (containsAny(normalized,
                "details{border:1pxsolid#c9d2dc;padding:12px;margin:12px0;background:#fff",
                "details{background:#fff",
                "details{background:white",
                "details{background:#ffffff")) {
            result.add(McpPackageViolation.blocker("report.visual-theme.plain-white",
                    "Details/cards still use plain white panel styling", css));
        }
        if (!containsAny(normalized, "box-shadow", "linear-gradient", "radial-gradient", "border-radius")) {
            result.add(McpPackageViolation.blocker("report.visual-theme.too-flat",
                    "Shared CSS lacks intentional visual treatment: box-shadow, gradient, or border-radius", css));
        }
    }

    private static void requireText(String text,
                                    Path path,
                                    McpPackageValidationResult result,
                                    String code,
                                    String message,
                                    String... tokens) {
        if (!containsAny(text, tokens)) {
            result.add(McpPackageViolation.blocker(code, message, path));
        }
    }

    private static boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
