package com.endava.ai.skills.mcp.tests.latest.report;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.RequiresArtifacts;
import com.endava.ai.skills.mcp.contract.ValidatesMcpContract;
import com.endava.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertFalse;

@Listeners(TestListener.class)
@ValidatesMcpContract("latest-package-score-and-opening-contract")
@RequiresArtifacts({
        "analysis/langgraph-business-understanding.html",
        "review/package-state.json",
        "validation/mcp/mcp-gate-result.md"
})
@McpSuiteRef("mcp.suite.mcp-latest-package-score-and-opening-contract-test")
public class McpLatestPackageScoreAndOpeningContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("score.overview.canonical-axes")
    public void finalReportScoreOverviewUsesCanonicalBusinessAxes() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertFinalReportScoreOverviewUsesCanonicalBusinessAxes();
    }

    @Test
    public void finalReportScoreNarrativeDoesNotGradeRuntimeAccountingAsBusinessDepth() throws IOException {
        McpPackageAuditValidation audit = McpPackageAuditValidation.latestPackage();
        String html = audit.readFinalReport();
        String scoreOverview = audit.detailsBody(html, "Score Overview");
        String scoreWall = between(html, "<section class=\"score-wall\"", "</section>");

        assertFalse(hasProofOnlyBusinessAxis(axisScoreCard(scoreWall, "Historical Reopenability")
                        + "\n" + axisTableRow(scoreOverview, "Historical Reopenability")),
                "Historical Reopenability score narrative must explain the reopened application state/object, "
                        + "not only say that runs, XML, Extent, state, tests or MCP proof exist.");
        assertFalse(hasProofOnlyBusinessAxis(axisScoreCard(scoreWall, "Business Substance")
                        + "\n" + axisTableRow(scoreOverview, "Business Substance")),
                "Business Substance score narrative must name business application substance, not only run/test/MCP accounting.");
        assertFalse(hasProofOnlyBusinessAxis(axisScoreCard(scoreWall, "Business Graph")
                        + "\n" + axisTableRow(scoreOverview, "Business Graph")),
                "Business Graph score narrative must explain application object flow/frontier, not only report proof or run chronology.");
    }

    @Test
    public void finalReportOpeningScoreCardsUseReadableStructure() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertFinalReportOpeningScoreCardsUseReadableStructure();
    }

    @Test
    public void finalReportOpeningScoreWallHasNoLooseScoreElements() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertFinalReportOpeningScoreWallHasNoLooseScoreElements();
    }

    @Test
    @ValidatesMcpContract("report.score-card.primary-html-ex-reference-order")
    @RequiresArtifacts({
            "analysis/langgraph-business-understanding.html",
            "docs/html-ex/README.md"
    })
    public void finalReportScoreCardsFollowPrimaryHtmlExampleReferenceOrder() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertFinalReportScoreCardsFollowPrimaryHtmlExampleReferenceOrder();
    }

    @Test
    public void overallTruthIsSynchronizedAcrossCanonicalArtifacts() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertOverallTruthIsSynchronizedAcrossCanonicalArtifacts();
    }

    @Test
    public void scoreCapIsConsistentWithWeakestAxis() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertScoreCapIsConsistentWithWeakestAxis();
    }

    @Test
    public void publishedOverallDoesNotExceedPackageStateCap() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertPublishedOverallDoesNotExceedPackageStateCap();
    }

    @Test
    public void modelOnlyPackagesStayCappedBelowUiOwnedPackages() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertModelOnlyPackagesStayCappedBelowUiOwnedPackages();
    }

    @Test
    public void runtimeCountersAreNotDisplayedBetweenScoreCardsAndExecutiveContext() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertRuntimeCountersAreNotDisplayedBetweenScoreCardsAndExecutiveContext();
    }

    @Test
    public void openingDoesNotExposeVerdictOrRuntimeBeforeScoreCards() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertOpeningDoesNotExposeVerdictOrRuntimeBeforeScoreCards();
    }

    @Test
    public void executiveContextCarriesVerdictAndRuntimeAsCollapsedSubsections() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertExecutiveContextCarriesVerdictAndRuntimeAsCollapsedSubsections();
    }

    @Test
    public void executiveContextIsCollapsedByDefault() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertExecutiveContextIsCollapsedByDefault();
    }

    @Test
    public void finalReportOpeningMatchesCanonicalHtmlExampleShape() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportOpeningMatchesCanonicalHtmlExampleShape();
    }

    private boolean hasProofOnlyBusinessAxis(String axisHtml) {
        String axisText = stripTags(axisHtml).toLowerCase(Locale.ROOT)
                .replace("historical reopenability", " ")
                .replace("business substance", " ")
                .replace("business graph", " ");
        if (axisText.isBlank()) {
            return false;
        }
        if (axisText.contains("run proofs") || axisText.contains("every run links")) {
            return true;
        }
        int proofHits = countMatches(axisText, "\\b(run|runs|test|tests|25/25|xml|extent|mcp|close-out|gate|proof|method|preflight)\\b");
        int businessHits = countMatches(axisText, "\\b(customer|invoice|refund|support|case|message|order|cart|payment|ledger|shipment|timeline|status|checkout|account|settlement|decision|lifecycle|reopen)\\b");
        return proofHits >= 2 && businessHits == 0;
    }

    private String axisScoreCard(String scoreWall, String axis) {
        Matcher matcher = Pattern.compile("(?is)<article\\b[^>]*class=[\"'][^\"']*score-card[^\"']*[\"'][^>]*>(.*?)</article>")
                .matcher(scoreWall);
        while (matcher.find()) {
            String article = matcher.group(1);
            if (stripTags(article).toLowerCase(Locale.ROOT).contains(axis.toLowerCase(Locale.ROOT))) {
                return article;
            }
        }
        return "";
    }

    private String axisTableRow(String scoreOverview, String axis) {
        Matcher matcher = Pattern.compile("(?is)<tr\\b[^>]*>(.*?)</tr>").matcher(scoreOverview);
        while (matcher.find()) {
            String row = matcher.group(1);
            if (stripTags(row).toLowerCase(Locale.ROOT).contains(axis.toLowerCase(Locale.ROOT))) {
                return row;
            }
        }
        return "";
    }

    private String between(String text, String startToken, String endToken) {
        int start = text.indexOf(startToken);
        if (start < 0) {
            return "";
        }
        int end = text.indexOf(endToken, start);
        if (end < 0) {
            return text.substring(start);
        }
        return text.substring(start, end + endToken.length());
    }

    private String stripTags(String html) {
        return html.replaceAll("(?is)<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private int countMatches(String text, String regex) {
        int count = 0;
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
