package com.endava.ai.skills.mcp.tests.latest.report;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-latest-package-report-presentation-contract-test")
public class McpLatestPackageReportPresentationContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("report.presentation.shared-css")
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
    @McpCheckRef("report.template-profile.primary-html-ex")
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
    public void finalReportDoesNotExposeRunSliceIndexBeforeExecutiveContext() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportDoesNotExposeRunSliceIndexBeforeExecutiveContext();
    }

    @Test
    public void finalReportProofMappingLinksAllMandatoryArtifactFamilies() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportProofMappingLinksAllMandatoryArtifactFamilies();
    }

    @Test
    @McpCheckRef("report.proof-mapping.balanced-columns")
    public void finalReportProofMappingUsesBalancedTableColumns() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportProofMappingUsesBalancedTableColumns();
    }

    @Test
    public void finalReportRunAuditUsesStructuredEvidenceRows() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportRunAuditUsesStructuredEvidenceRows();
    }

    @Test
    @McpCheckRef("report.artifact-index.family-grouping")
    public void finalReportArtifactIndexUsesGroupedEvidenceFamilies() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportArtifactIndexUsesGroupedEvidenceFamilies();
    }

    @Test
    public void finalReportArtifactIndexSeparatesDecisiveIndexFromPerRunAuditDump() throws IOException {
        McpPackageAuditValidation audit = McpPackageAuditValidation.latestPackage();
        String html = audit.readFinalReport();
        String artifactIndex = audit.detailsBody(html, "Artifact Index");
        int perRunLinkCount = count(Pattern.compile("(?is)href=[\"'][^\"']*\\.\\./runs/run_\\d{2}/"), artifactIndex);
        int familyProseCount = count(Pattern.compile("(?is)<section\\b[^>]*\\bartifact-family\\b[^>]*>\\s*<h[34]\\b[^>]*>.*?</h[34]>\\s*<p\\b[^>]*>.{80,}?</p>"),
                artifactIndex);

        assertTrue(perRunLinkCount <= 2,
                "Artifact Index must stay a decisive package evidence map; per-run links belong in Proof Mapping and Run Audit. "
                        + "Found " + perRunLinkCount + " per-run links in Artifact Index.");
        assertTrue(artifactIndex.contains("This index keeps decisive")
                        || artifactIndex.contains("local decisive evidence")
                        || familyProseCount >= 4,
                "Artifact Index must either explain that it keeps decisive evidence, or use artifact-family blocks with meaningful provenance prose. "
                        + "A plain link cloud is not enough.");
    }

    @Test
    public void finalReportArtifactIndexUsesComparableTableStructure() throws IOException {
        McpPackageAuditValidation audit = McpPackageAuditValidation.latestPackage();
        String html = audit.readFinalReport();
        String artifactIndex = audit.detailsBody(html, "Artifact Index");
        String normalized = artifactIndex.toLowerCase();

        assertTrue(artifactIndex.contains("<table"),
                "Artifact Index must use at least one table so decisive artifacts scan like Proof Mapping, Run Audit and MCP Gate sections; "
                        + "family prose alone still reads like a link dump.");
        assertTrue(normalized.contains("<th") && normalized.contains("family")
                        && normalized.contains("artifact") && (normalized.contains("evidence") || normalized.contains("role"))
                        && (normalized.contains("why") || normalized.contains("interpretation") || normalized.contains("status")),
                "Artifact Index table must expose column meaning such as Family, Artifact, Evidence role and Why/Status.");
        assertTrue(count(Pattern.compile("(?is)<tr\\b"), artifactIndex) >= 5,
                "Artifact Index table must contain enough rows to group the package spine, governance, run evidence, MCP close-out and KPI/calibration families.");
    }

    @Test
    @McpCheckRef("report.mcp-evidence.final-links")
    public void finalReportLinksFrozenMcpContractEvidence() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportLinksFrozenMcpContractEvidence();
    }

    @Test
    public void finalReportLinksAuditedFrameworkPropertiesSnapshot() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportLinksAuditedFrameworkPropertiesSnapshot();
    }

    private int count(Pattern pattern, String text) {
        int count = 0;
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private String exactDetailsBlock(String html, String summaryTitle) {
        Matcher matcher = Pattern.compile("(?is)<details\\b[^>]*>\\s*<summary\\b[^>]*>.*?"
                        + Pattern.quote(summaryTitle) + ".*?</summary>(.*?)</details>")
                .matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
