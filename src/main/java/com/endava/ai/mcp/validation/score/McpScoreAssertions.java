package com.endava.ai.mcp.validation.score;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import java.io.IOException;

public final class McpScoreAssertions {
    private final McpScoreCapAssertions cap;
    private final McpScoreSyncAssertions sync;
    private final McpScoreCardAssertions cards;
    private final McpScoreOverviewAssertions overview;

    public McpScoreAssertions(McpPackageAuditValidation audit) {
        this.cap = new McpScoreCapAssertions(audit);
        this.sync = new McpScoreSyncAssertions(audit);
        this.cards = new McpScoreCardAssertions(audit);
        this.overview = new McpScoreOverviewAssertions(audit);
    }

    public void assertScoreCapIsConsistentWithWeakestAxis() {
        cap.assertScoreCapIsConsistentWithWeakestAxis();
    }

    public void assertOverallTruthIsSynchronizedAcrossCanonicalArtifacts() throws IOException {
        sync.assertOverallTruthIsSynchronizedAcrossCanonicalArtifacts();
    }

    public void assertPublishedOverallDoesNotExceedPackageStateCap() throws IOException {
        cap.assertPublishedOverallDoesNotExceedPackageStateCap();
    }

    public void assertModelOnlyPackagesStayCappedBelowUiOwnedPackages() throws IOException {
        cap.assertModelOnlyPackagesStayCappedBelowUiOwnedPackages();
    }

    public void assertFinalReportScoreOverviewUsesCanonicalBusinessAxes() throws IOException {
        overview.assertFinalReportScoreOverviewUsesCanonicalBusinessAxes();
    }

    public void assertFinalReportOpeningScoreCardsUseReadableStructure() throws IOException {
        cards.assertFinalReportOpeningScoreCardsUseReadableStructure();
    }

    public void assertFinalReportOpeningScoreWallHasNoLooseScoreElements() throws IOException {
        cards.assertFinalReportOpeningScoreWallHasNoLooseScoreElements();
    }

    public void assertFinalReportScoreCardsFollowPrimaryHtmlExampleReferenceOrder() throws IOException {
        cards.assertFinalReportScoreCardsFollowPrimaryHtmlExampleReferenceOrder();
    }

    public void assertRuntimeCountersAreNotDisplayedBetweenScoreCardsAndExecutiveContext() throws IOException {
        cards.assertRuntimeCountersAreNotDisplayedBetweenScoreCardsAndExecutiveContext();
    }

    public void assertOpeningDoesNotExposeVerdictOrRuntimeBeforeScoreCards() throws IOException {
        cards.assertOpeningDoesNotExposeVerdictOrRuntimeBeforeScoreCards();
    }

    public void assertExecutiveContextCarriesVerdictAndRuntimeAsCollapsedSubsections() throws IOException {
        overview.assertExecutiveContextCarriesVerdictAndRuntimeAsCollapsedSubsections();
    }

    public void assertExecutiveContextIsCollapsedByDefault() throws IOException {
        overview.assertExecutiveContextIsCollapsedByDefault();
    }
}
