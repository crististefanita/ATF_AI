package com.endava.ai.mcp.rules.report;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpArtifactPaths;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.contract.McpContract;
import com.endava.ai.mcp.contract.McpRuleCategory;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

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
        McpRuleSupport.requireText(html, report, result, "report.doctype", "Missing HTML doctype", "<!doctype html", "<!DOCTYPE html");
        McpRuleSupport.requireText(html, report, result, "report.charset", "Missing UTF-8 charset", "<meta charset=\"utf-8\"", "<meta charset='utf-8'");
        McpRuleSupport.requireText(html, report, result, "report.viewport", "Missing responsive viewport meta tag",
                "name=\"viewport\"", "name='viewport'");
        McpRuleSupport.requireText(html, report, result, "report.title", "Missing HTML title", "<title>");

        requireVisualShell(html, report, result);
        requireCssContract(context, result);
    }

    private void requireVisualShell(String html, Path report, McpPackageValidationResult result) {
        McpRuleSupport.requireText(html, report, result, "report.visual-shell.wrap",
                "Missing reusable report wrap container", "class=\"wrap\"", "class='wrap'");
        McpRuleSupport.requireText(html, report, result, "report.visual-shell.header",
                "Missing report header", "<header");
        McpRuleSupport.requireText(html, report, result, "report.visual-shell.hero",
                "Missing hero opening section", "class=\"hero\"", "class='hero'");
        McpRuleSupport.requireText(html, report, result, "report.visual-shell.score-wall",
                "Missing visible score wall or score cards", "score-wall", "score-card");
        McpRuleSupport.requireText(html, report, result, "report.visual-shell.runtime-strip",
                "Missing visible runtime or navigation strip", "runtime-strip", "pill-row");
    }

    private void requireCssContract(McpPackageContext context, McpPackageValidationResult result) {
        Path css = context.paths().reportCss();
        if (!Files.isRegularFile(css)) {
            return;
        }
        String cssContent = context.files().read(css, result);
        requireExactProjectCssReuse(context, css, cssContent, result);
        McpRuleSupport.requireText(cssContent, css, result, "report.css-shell",
                "Shared CSS must style the report shell", "body", "details", "summary");
        McpRuleSupport.requireText(cssContent, css, result, "report.css-business",
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
        if (normalized.contains("body{") && McpRuleSupport.containsAny(normalized,
                "background:#fff", "background:white", "background:#ffffff", "background:#fafafa")) {
            result.add(McpPackageViolation.blocker("report.visual-theme.plain-white",
                    "Body background still looks like a plain white/default document shell", css));
        }
        if (McpRuleSupport.containsAny(normalized,
                "details{border:1pxsolid#c9d2dc;padding:12px;margin:12px0;background:#fff",
                "details{background:#fff",
                "details{background:white",
                "details{background:#ffffff")) {
            result.add(McpPackageViolation.blocker("report.visual-theme.plain-white",
                    "Details/cards still use plain white panel styling", css));
        }
        if (!McpRuleSupport.containsAny(normalized, "box-shadow", "linear-gradient", "radial-gradient", "border-radius")) {
            result.add(McpPackageViolation.blocker("report.visual-theme.too-flat",
                    "Shared CSS lacks intentional visual treatment: box-shadow, gradient, or border-radius", css));
        }
    }
}
