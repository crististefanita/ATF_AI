package com.endava.ai.mcp.tests.latest;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class McpLatestPackageReportPresentationContractTest {
    @Test
    public void finalReportUsesSharedCssAndDocumentChrome() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportUsesSharedCssAndDocumentChrome();
    }

    @Test
    public void finalReportAvoidsPlainWhiteDefaultTheme() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportAvoidsPlainWhiteDefaultTheme();
    }

    @Test
    public void finalReportUsesHtmlExampleVisualShell() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportUsesHtmlExampleVisualShell();
    }

    @Test
    public void finalReportFollowsPrimaryHtmlExampleTemplateProfile() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportFollowsPrimaryHtmlExampleTemplateProfile();
    }

    @Test
    public void finalReportOpeningHasVisiblePillRow() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportOpeningHasVisiblePillRow();
    }

    @Test
    public void finalReportDoesNotExposeRootRunTimestampIdentity() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportDoesNotExposeRootRunTimestampIdentity();
    }

    @Test
    public void sharedCssLooksLikeCanonicalReferenceStylesheet() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertSharedCssLooksLikeCanonicalReferenceStylesheet();
    }

    @Test
    public void analysisSharedCssIsExactCopyOfProjectReference() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertAnalysisSharedCssIsExactCopyOfProjectReference();
    }

    @Test
    public void finalReportDoesNotUseInlineStyleOverrides() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportDoesNotUseInlineStyleOverrides();
    }

    @Test
    public void finalReportMainSectionsAreCollapsibleWithOnlyGraphOpen() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportMainSectionsAreCollapsibleWithOnlyGraphOpen();
    }

    @Test
    public void finalReportProofMappingLinksAllMandatoryArtifactFamilies() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportProofMappingLinksAllMandatoryArtifactFamilies();
    }

    @Test
    public void finalReportRunAuditUsesStructuredEvidenceRows() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportRunAuditUsesStructuredEvidenceRows();
    }

    @Test
    public void finalReportLinksFrozenMcpContractEvidence() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportLinksFrozenMcpContractEvidence();
    }
}
