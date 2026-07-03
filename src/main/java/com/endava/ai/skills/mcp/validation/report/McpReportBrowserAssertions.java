package com.endava.ai.skills.mcp.validation.report;

import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;

public final class McpReportBrowserAssertions {
    private final McpReportChromeAssertions chrome;
    private final McpReportVisualShellAssertions visualShell;
    private final McpReportOpeningAssertions opening;
    private final McpReportCssReuseAssertions cssReuse;

    public McpReportBrowserAssertions(McpPackageAuditValidation audit) {
        this.chrome = new McpReportChromeAssertions(audit);
        this.visualShell = new McpReportVisualShellAssertions(audit);
        this.opening = new McpReportOpeningAssertions(audit);
        this.cssReuse = new McpReportCssReuseAssertions(audit);
    }

    public void assertFinalReportUsesSharedCssAndDocumentChrome() throws IOException {
        chrome.assertUsesSharedCssAndDocumentChrome();
    }

    public void assertFinalReportAvoidsPlainWhiteDefaultTheme() throws IOException {
        visualShell.assertAvoidsPlainWhiteDefaultTheme();
    }

    public void assertFinalReportUsesHtmlExampleVisualShell() throws IOException {
        visualShell.assertUsesHtmlExampleVisualShell();
    }

    public void assertFinalReportOpeningHasVisiblePillRow() throws IOException {
        opening.assertHasVisiblePillRow();
    }

    public void assertFinalReportDoesNotExposeRootRunTimestampIdentity() throws IOException {
        opening.assertDoesNotExposeRootRunTimestampIdentity();
    }

    public void assertSharedCssLooksLikeCanonicalReferenceStylesheet() throws IOException {
        cssReuse.assertLooksLikeCanonicalReferenceStylesheet();
    }

    public void assertAnalysisSharedCssIsExactCopyOfProjectReference() throws IOException {
        cssReuse.assertAnalysisCopyIsExactProjectReference();
    }

    public void assertFinalReportDoesNotUseInlineStyleOverrides() throws IOException {
        cssReuse.assertFinalReportDoesNotUseInlineStyleOverrides();
    }
}
