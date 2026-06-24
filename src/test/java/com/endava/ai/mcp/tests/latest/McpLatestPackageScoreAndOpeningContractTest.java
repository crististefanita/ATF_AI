package com.endava.ai.mcp.tests.latest;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.contract.RequiresArtifacts;
import com.endava.ai.mcp.contract.ValidatesMcpContract;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
@ValidatesMcpContract("latest-package-score-and-opening-contract")
@RequiresArtifacts({
        "analysis/langgraph-business-understanding.html",
        "review/package-state.json",
        "validation/mcp/mcp-gate-result.md"
})
public class McpLatestPackageScoreAndOpeningContractTest {
    @Test
    public void finalReportScoreOverviewUsesCanonicalBusinessAxes() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertFinalReportScoreOverviewUsesCanonicalBusinessAxes();
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
}
