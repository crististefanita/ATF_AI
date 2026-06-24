package com.endava.ai.mcp.validation.report;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import java.io.IOException;

public final class McpReportAssertions {
    private final McpReportBrowserAssertions browserAssertions;
    private final McpReportTemplateAssertions templateAssertions;
    private final McpReportEvidenceAssertions evidenceAssertions;
    private final McpReportLinkAssertions linkAssertions;

    public McpReportAssertions(McpPackageAuditValidation audit) {
        this.browserAssertions = new McpReportBrowserAssertions(audit);
        this.templateAssertions = new McpReportTemplateAssertions(audit);
        this.evidenceAssertions = new McpReportEvidenceAssertions(audit);
        this.linkAssertions = new McpReportLinkAssertions(audit);
    }

    public void assertFinalReportUsesSharedCssAndDocumentChrome() throws IOException {
        browserAssertions.assertFinalReportUsesSharedCssAndDocumentChrome();
    }

    public void assertFinalReportAvoidsPlainWhiteDefaultTheme() throws IOException {
        browserAssertions.assertFinalReportAvoidsPlainWhiteDefaultTheme();
    }

    public void assertFinalReportUsesHtmlExampleVisualShell() throws IOException {
        browserAssertions.assertFinalReportUsesHtmlExampleVisualShell();
    }

    public void assertFinalReportOpeningHasVisiblePillRow() throws IOException {
        browserAssertions.assertFinalReportOpeningHasVisiblePillRow();
    }

    public void assertFinalReportDoesNotExposeRootRunTimestampIdentity() throws IOException {
        browserAssertions.assertFinalReportDoesNotExposeRootRunTimestampIdentity();
    }

    public void assertSharedCssLooksLikeCanonicalReferenceStylesheet() throws IOException {
        browserAssertions.assertSharedCssLooksLikeCanonicalReferenceStylesheet();
    }

    public void assertAnalysisSharedCssIsExactCopyOfProjectReference() throws IOException {
        browserAssertions.assertAnalysisSharedCssIsExactCopyOfProjectReference();
    }

    public void assertFinalReportDoesNotUseInlineStyleOverrides() throws IOException {
        browserAssertions.assertFinalReportDoesNotUseInlineStyleOverrides();
    }

    public void assertFinalReportHasRequiredBusinessStructure() {
        templateAssertions.assertFinalReportHasRequiredBusinessStructure();
    }

    public void assertFinalReportContainsCanonicalBusinessSections() throws IOException {
        templateAssertions.assertFinalReportContainsCanonicalBusinessSections();
    }

    public void assertFinalReportMainSectionsAreCollapsibleWithOnlyGraphOpen() throws IOException {
        templateAssertions.assertFinalReportMainSectionsAreCollapsibleWithOnlyGraphOpen();
    }

    public void assertFinalReportOpeningMatchesCanonicalHtmlExampleShape() throws IOException {
        templateAssertions.assertFinalReportOpeningMatchesCanonicalHtmlExampleShape();
    }

    public void assertFinalReportFollowsPrimaryHtmlExampleTemplateProfile() throws IOException {
        templateAssertions.assertFinalReportFollowsPrimaryHtmlExampleTemplateProfile();
    }

    public void assertFinalReportPublishesRuntimeConstantsAndCounters() throws IOException {
        templateAssertions.assertFinalReportPublishesRuntimeConstantsAndCounters();
    }

    public void assertFinalReportDoesNotLeakExternalCalibrationSources() throws IOException {
        templateAssertions.assertFinalReportDoesNotLeakExternalCalibrationSources();
    }

    public void assertFinalReportProofMappingLinksAllMandatoryArtifactFamilies() throws IOException {
        evidenceAssertions.assertFinalReportProofMappingLinksAllMandatoryArtifactFamilies();
    }

    public void assertFinalReportRunAuditUsesStructuredEvidenceRows() throws IOException {
        evidenceAssertions.assertFinalReportRunAuditUsesStructuredEvidenceRows();
    }

    public void assertFinalReportLinksFrozenMcpContractEvidence() throws IOException {
        evidenceAssertions.assertFinalReportLinksFrozenMcpContractEvidence();
    }

    public void assertFinalReportLinksEveryRunEvidenceFamily() throws IOException {
        evidenceAssertions.assertFinalReportLinksEveryRunEvidenceFamily();
    }

    public void assertFinalReportLocalHyperlinksResolve() {
        linkAssertions.assertFinalReportLocalHyperlinksResolve();
    }

    public void assertEveryLocalHrefInFinalReportResolves() throws IOException {
        linkAssertions.assertEveryLocalHrefInFinalReportResolves();
    }

    public void assertSavedLinkAuditReportsNoMissingLinks() throws IOException {
        linkAssertions.assertSavedLinkAuditReportsNoMissingLinks();
    }

}
