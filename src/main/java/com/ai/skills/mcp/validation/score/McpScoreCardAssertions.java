package com.ai.skills.mcp.validation.score;

import lombok.RequiredArgsConstructor;


import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.ai.skills.mcp.validation.profile.McpReportTemplateProfile;
import com.ai.skills.mcp.validation.profile.html.McpReportTemplateConsensus;
import com.ai.skills.mcp.validation.report.model.OpeningScoreWallModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class McpScoreCardAssertions {
    private final McpPackageAuditValidation audit;

    public void assertFinalReportOpeningScoreCardsUseReadableStructure() throws IOException {
        OpeningScoreWallModel scoreWall = OpeningScoreWallModel.fromHtml(audit.readFinalReport());
        List<String> blockers = new ArrayList<>();
        if (!scoreWall.hasScoreWallBeforeExecutiveContext()) {
            blockers.add("Cannot locate score-wall before Executive Context");
        } else {
            if (scoreWall.scoreCardCount() < 6) {
                blockers.add("Opening score-wall must expose at least 6 score cards; found "
                        + scoreWall.scoreCardCount());
            }
            if (scoreWall.labelCount() < 6 || scoreWall.scoreCount() < 6) {
                blockers.add("Each opening score card must use readable .label and .score elements; labels="
                        + scoreWall.labelCount() + ", scores=" + scoreWall.scoreCount());
            }
            if (scoreWall.hasCompressedSpanStrongScoreCard()) {
                blockers.add("Score cards use compressed span+strong markup; use separate .label, .score, and .note/small elements");
            }
        }
        audit.assertNoSyntheticViolations("Final report opening score-card readable structure",
                blockers, audit.finalReport(), "report.score-card.structure",
                audit.details(
                        "Asserts: score cards are visually readable, not only token-present.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: at least 6 .score-card blocks with separate .label and .score elements.",
                        "Forbidden: compressed span+strong markup that visually glues label and number."));
    }

    public void assertFinalReportOpeningScoreWallHasNoLooseScoreElements() throws IOException {
        OpeningScoreWallModel scoreWall = OpeningScoreWallModel.fromHtml(audit.readFinalReport());
        List<String> blockers = new ArrayList<>();
        if (!scoreWall.hasScoreWallBeforeExecutiveContext()) {
            blockers.add("Cannot locate score-wall before Executive Context");
        } else if (!scoreWall.hasScoreCardChildren()) {
            blockers.add("Opening score-wall has no score-card children");
        } else if (scoreWall.hasLooseLabelOrScoreBeforeFirstCard()) {
            blockers.add("Opening score-wall contains loose label/score elements before the first score-card; wrap Overall in a score-card");
        }
        audit.assertNoSyntheticViolations("Final report opening score-wall has no loose score elements",
                blockers, audit.finalReport(), "report.score-wall.loose-elements",
                audit.details(
                        "Asserts: opening score-wall is a clean grid of cards, not loose label/score text plus cards.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: every visible score value lives inside a score-card.",
                        "Forbidden: direct .label or .score children before the first score-card."));
    }

    public void assertFinalReportScoreCardsFollowPrimaryHtmlExampleReferenceOrder() throws IOException {
        McpReportTemplateConsensus reference = McpReportTemplateProfile.htmlExConsensus();
        McpReportTemplateProfile actualProfile = McpReportTemplateProfile.fromHtml(audit.readFinalReport());
        List<String> expected = reference.scoreCardLabels();
        List<String> actual = actualProfile.scoreCardLabels();
        List<String> blockers = new ArrayList<>();
        if (expected.isEmpty()) {
            blockers.add("html-ex reference corpus exposes no stable score-card labels: " + reference.sources());
        }
        if (actual.isEmpty()) {
            blockers.add("Final report exposes no readable score-card labels");
        }
        if (!expected.equals(actual)) {
            blockers.add("Opening score-card label order differs from html-ex structural consensus. Expected "
                    + expected + " but found " + actual);
        }
        if (!actual.isEmpty() && !"Overall".equals(actual.get(actual.size() - 1))) {
            blockers.add("Overall score card must be the rightmost/final score-card, following primary html-ex reference");
        }
        audit.assertNoSyntheticViolations("Final report score cards follow primary html-ex reference order",
                blockers, audit.finalReport(), "report.score-card.reference-order",
                audit.details(
                        "Asserts: score-card order is learned dynamically from docs/html-ex active reference corpus.",
                        "Reference README: " + audit.context.relative(reference.corpus().readme()) + ".",
                        "Reference corpus: " + reference.sources() + ".",
                        "Expected labels: " + expected + "."));
    }

    public void assertRuntimeCountersAreNotDisplayedBetweenScoreCardsAndExecutiveContext() throws IOException {
        OpeningScoreWallModel scoreWall = OpeningScoreWallModel.fromHtml(audit.readFinalReport());
        List<String> blockers = new ArrayList<>();
        if (!scoreWall.hasScoreWallBeforeExecutiveContext()) {
            blockers.add("Cannot locate score-wall and Executive Context to verify opening layout");
        } else if (scoreWall.hasRuntimeStripBetweenScoreWallAndExecutiveContext()) {
            blockers.add("Runtime/accounting strip is displayed between score cards and Executive Context; move it into Score Overview or Artifact/Close-Out");
        }
        audit.assertNoSyntheticViolations("Final report opening has no runtime strip between score cards and Executive Context",
                blockers, audit.finalReport(), "report.opening.runtime-strip misplaced",
                audit.details(
                        "Asserts: opening stays business-first after score cards.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Forbidden between score-wall and Executive Context: runtime-strip, runtime constants, runs consumed, tests produced.",
                        "Expected: runtime/accounting details live in Score Overview, Artifact Index, or Close-Out."));
    }

    public void assertOpeningDoesNotExposeVerdictOrRuntimeBeforeScoreCards() throws IOException {
        OpeningScoreWallModel scoreWall = OpeningScoreWallModel.fromHtml(audit.readFinalReport());
        List<String> blockers = new ArrayList<>();
        if (!scoreWall.hasHeroBeforeScoreWall()) {
            blockers.add("Cannot locate hero opening and score-wall to verify pre-score opening discipline");
        } else if (scoreWall.hasVerdictOrRuntimeBeforeScoreCards()) {
            blockers.add("Opening exposes Verdict/runtime constants before score cards; move them into collapsed Executive Context subsections");
        }
        audit.assertNoSyntheticViolations("Final report opening has no visible Verdict/runtime block before score cards",
                blockers, audit.finalReport(), "report.opening.pre-score-runtime",
                audit.details(
                        "Asserts: the opening remains business-first and does not start with a raw Verdict/runtime block.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Forbidden before score-wall: <h2>Verdict</h2>, runtime-strip, RUN_EXPECTED_DELTA_QUALITY, ROUND_TARGET_TEST_COUNT, META_ITERATION_COUNT.",
                        "Expected: verdict/runtime details live in collapsed Executive Context subsections or canonical Score/Artifact/Close-Out sections."));
    }
}
