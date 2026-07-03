package com.ai.skills.mcp.validation.report;

import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class McpReportChromeAssertions {
    private static final String FINAL_REPORT_AND_CSS_ARTIFACTS =
            "Artifacts: " + McpArtifactPaths.FINAL_REPORT + " and " + McpArtifactPaths.REPORT_CSS + ".";

    private final McpPackageAuditValidation audit;

    void assertUsesSharedCssAndDocumentChrome() throws IOException {
        String html = audit.readFinalReport();
        String css = Files.readString(audit.context.packageRoot().resolve(McpArtifactPaths.REPORT_CSS));
        List<String> blockers = new ArrayList<>();
        audit.requireAnyToken(html, List.of("<!doctype html", "<!DOCTYPE html"),
                "Missing <!doctype html>", blockers);
        audit.requireToken(html, "<meta charset=\"utf-8\"", "Missing UTF-8 meta charset", blockers);
        audit.requireAnyToken(html, List.of("name=\"viewport\"", "name='viewport'"),
                "Missing responsive viewport meta tag", blockers);
        audit.requireToken(html, "<title>", "Missing HTML title", blockers);
        audit.requireToken(html, "href=\"shared-report-reference.css\"",
                "Missing shared-report-reference.css stylesheet link", blockers);
        audit.requireAnyToken(css, List.of("body", "details", "summary"),
                "Shared CSS must style body/details/summary report shell", blockers);
        audit.requireAnyToken(css, List.of(".graph", "svg", ".business", ".score"),
                "Shared CSS must style graph/score/business presentation classes", blockers);
        blockers.addAll(audit.canonicalSharedCssBlockers(css));
        audit.assertNoSyntheticViolations("Final report shared CSS and document chrome", blockers, audit.finalReport(),
                "report.chrome.missing",
                audit.details(
                        "Asserts: final report has reusable document shell and shared CSS styling.",
                        FINAL_REPORT_AND_CSS_ARTIFACTS,
                        "Expected HTML: doctype, UTF-8 charset, viewport meta, title, stylesheet link.",
                        "Expected CSS: body/details/summary plus graph/score/business styling."));
    }
}
