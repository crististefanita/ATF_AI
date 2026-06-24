package com.endava.ai.mcp.validation.score;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.mcp.validation.profile.McpHtmlExampleReference;
import com.endava.ai.mcp.validation.profile.McpReportSections;
import com.endava.ai.mcp.validation.profile.McpReportTemplateProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McpScoreCardAssertions {
    private final McpPackageAuditValidation audit;

    public McpScoreCardAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

    public void assertFinalReportOpeningScoreCardsUseReadableStructure() throws IOException {
            String html = audit.readFinalReport();
            List<String> blockers = new ArrayList<>();
            int scoreWall = html.indexOf("class=\"score-wall\"");
            int executiveContext = html.indexOf(McpReportSections.EXECUTIVE_CONTEXT);
            if (scoreWall < 0 || executiveContext < 0 || scoreWall > executiveContext) {
                blockers.add("Cannot locate score-wall before Executive Context");
            } else {
                String openingScoreWall = html.substring(scoreWall, executiveContext);
                int cards = count(openingScoreWall, "class=\"score-card\"");
                int labels = count(openingScoreWall, "class=\"label\"");
                int scores = count(openingScoreWall, "class=\"score\"");
                if (cards < 6) {
                    blockers.add("Opening score-wall must expose at least 6 score cards; found " + cards);
                }
                if (labels < 6 || scores < 6) {
                    blockers.add("Each opening score card must use readable .label and .score elements; labels="
                            + labels + ", scores=" + scores);
                }
                Matcher compressed = Pattern.compile("(?is)<span>\\s*(Business Substance|Business Graph|Historical Reopenability|Proof Chain|Contract Artifact)\\s*</span>\\s*<strong>\\s*\\d+\\s*</strong>")
                        .matcher(openingScoreWall);
                if (compressed.find()) {
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
            String html = audit.readFinalReport();
            List<String> blockers = new ArrayList<>();
            int scoreWall = html.indexOf("class=\"score-wall\"");
            int executiveContext = html.indexOf(McpReportSections.EXECUTIVE_CONTEXT);
            if (scoreWall < 0 || executiveContext < 0 || scoreWall > executiveContext) {
                blockers.add("Cannot locate score-wall before Executive Context");
            } else {
                String openingScoreWall = html.substring(scoreWall, executiveContext);
                int firstCard = openingScoreWall.indexOf("class=\"score-card\"");
                if (firstCard < 0) {
                    blockers.add("Opening score-wall has no score-card children");
                } else {
                    String beforeFirstCard = openingScoreWall.substring(0, firstCard);
                    if (beforeFirstCard.contains("class=\"label\"") || beforeFirstCard.contains("class='label'")
                            || beforeFirstCard.contains("class=\"score\"") || beforeFirstCard.contains("class='score'")) {
                        blockers.add("Opening score-wall contains loose label/score elements before the first score-card; wrap Overall in a score-card");
                    }
                }
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
            McpHtmlExampleReference reference = McpHtmlExampleReference.load();
            McpReportTemplateProfile referenceProfile = McpReportTemplateProfile.primaryReference();
            McpReportTemplateProfile actualProfile = McpReportTemplateProfile.fromHtml(audit.readFinalReport());
            List<String> expected = referenceProfile.scoreCardLabels();
            List<String> actual = actualProfile.scoreCardLabels();
            List<String> blockers = new ArrayList<>();
            if (expected.isEmpty()) {
                blockers.add("Primary html-ex reference exposes no score-card labels: " + reference.report());
            }
            if (actual.isEmpty()) {
                blockers.add("Final report exposes no readable score-card labels");
            }
            if (!expected.equals(actual)) {
                blockers.add("Opening score-card label order differs from primary html-ex structural reference. Expected "
                        + expected + " but found " + actual);
            }
            if (!actual.isEmpty() && !"Overall".equals(actual.get(actual.size() - 1))) {
                blockers.add("Overall score card must be the rightmost/final score-card, following primary html-ex reference");
            }
            audit.assertNoSyntheticViolations("Final report score cards follow primary html-ex reference order",
                    blockers, audit.finalReport(), "report.score-card.reference-order",
                    audit.details(
                            "Asserts: score-card order is learned dynamically from docs/html-ex primary structural reference.",
                            "Reference README: " + audit.context.relative(reference.readme()) + ".",
                            "Reference report: " + audit.context.relative(reference.report()) + ".",
                            "Expected labels: " + expected + "."));
        }

    public void assertRuntimeCountersAreNotDisplayedBetweenScoreCardsAndExecutiveContext() throws IOException {
            String html = audit.readFinalReport();
            List<String> blockers = new ArrayList<>();
            int scoreWall = html.indexOf("class=\"score-wall\"");
            int executiveContext = html.indexOf(McpReportSections.EXECUTIVE_CONTEXT);
            if (scoreWall < 0 || executiveContext < 0) {
                blockers.add("Cannot locate score-wall and Executive Context to verify opening layout");
            } else if (scoreWall < executiveContext) {
                String between = html.substring(scoreWall, executiveContext);
                if (between.contains("class=\"runtime-strip\"") || between.contains("RUN_EXPECTED_DELTA_QUALITY")
                        || between.contains("ROUND_TARGET_TEST_COUNT") || between.contains("META_ITERATION_COUNT")
                        || between.contains("runs consumed") || between.contains("tests produced")) {
                    blockers.add("Runtime/accounting strip is displayed between score cards and Executive Context; move it into Score Overview or Artifact/Close-Out");
                }
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
            String html = audit.readFinalReport();
            List<String> blockers = new ArrayList<>();
            int hero = html.indexOf("class=\"hero\"");
            int scoreWall = html.indexOf("class=\"score-wall\"");
            if (hero < 0 || scoreWall < 0 || hero > scoreWall) {
                blockers.add("Cannot locate hero opening and score-wall to verify pre-score opening discipline");
            } else {
                String beforeScoreCards = html.substring(hero, scoreWall);
                if (beforeScoreCards.contains("<h2>Verdict</h2>") || beforeScoreCards.contains("class=\"runtime-strip\"")
                        || beforeScoreCards.contains("RUN_EXPECTED_DELTA_QUALITY")
                        || beforeScoreCards.contains("ROUND_TARGET_TEST_COUNT")
                        || beforeScoreCards.contains("META_ITERATION_COUNT")) {
                    blockers.add("Opening exposes Verdict/runtime constants before score cards; move them into collapsed Executive Context subsections");
                }
            }
            audit.assertNoSyntheticViolations("Final report opening has no visible Verdict/runtime block before score cards",
                    blockers, audit.finalReport(), "report.opening.pre-score-runtime",
                    audit.details(
                            "Asserts: the opening remains business-first and does not start with a raw Verdict/runtime block.",
                            "Artifact: analysis/langgraph-business-understanding.html.",
                            "Forbidden before score-wall: <h2>Verdict</h2>, runtime-strip, RUN_EXPECTED_DELTA_QUALITY, ROUND_TARGET_TEST_COUNT, META_ITERATION_COUNT.",
                            "Expected: verdict/runtime details live in collapsed Executive Context subsections or canonical Score/Artifact/Close-Out sections."));
        }

    private int count(String text, String token) {
            int count = 0;
            int index = text.indexOf(token);
            while (index >= 0) {
                count++;
                index = text.indexOf(token, index + token.length());
            }
            return count;
        }
}
