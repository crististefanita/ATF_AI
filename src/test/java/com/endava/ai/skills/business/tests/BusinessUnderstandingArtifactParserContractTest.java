package com.endava.ai.skills.business.tests;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.business.parse.BusinessUnderstandingArtifactParser;
import com.endava.ai.skills.business.taxonomy.BusinessUnderstandingConcept;
import com.endava.ai.skills.business.taxonomy.BusinessUnderstandingInputType;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

@Listeners(TestListener.class)
public class BusinessUnderstandingArtifactParserContractTest {
    private static final String CONSUMER_BRIDGE_RESOURCE = "skills/business/contracts/consumer-bridge.yml";
    private static final String SEMANTIC_MODEL_RESOURCE = "skills/business/contracts/semantic-model.yml";
    private static final String SEMANTIC_LEXICON_RESOURCE = "skills/business/contracts/semantic-lexicon.yml";

    @Test
    public void parserReducesHtmlExLangGraphReportToSharedSemanticSummary() throws IOException {
        Path report = firstHtmlExLangGraphReport();
        BusinessUnderstandingArtifactParser.ArtifactSummary summary =
                BusinessUnderstandingArtifactParser.parseHtml(
                        BusinessUnderstandingInputType.FINAL_BUSINESS_REPORT,
                        AtfSourcePaths.unixPath(report),
                        Files.readString(report));

        BusinessUnderstandingReportNarrative.verify(
                CONSUMER_BRIDGE_RESOURCE,
                "business-understanding.consumer.mcp-langgraph-final-report",
                BusinessUnderstandingReportNarrative.observedSummary(summary),
                () -> {
                    Assert.assertTrue(summary.looksLikeMcpFinalBusinessReport(),
                            "html-ex LangGraph report must reduce to shared MCP final-report semantics.");
                    Assert.assertTrue(summary.hasSectionContaining("Business Flow Graph"));
                    Assert.assertTrue(summary.hasSectionContaining("Score Overview"));
                    Assert.assertTrue(summary.hasSectionContaining("Proof Mapping"));
                    Assert.assertTrue(summary.hasPublishableScoreBand());
                    Assert.assertTrue(summary.hasLinkContaining("validation/mcp")
                                    || summary.hasLinkContaining("ExtentReport")
                                    || summary.hasLinkContaining("run_"),
                            "LangGraph report should expose reopenable package evidence links.");
                    Assert.assertTrue(summary.hasConcepts(Set.of(
                            BusinessUnderstandingConcept.BUSINESS_FLOW_STORY,
                            BusinessUnderstandingConcept.PROOF_CHAIN,
                            BusinessUnderstandingConcept.SCORE_RATIONALE,
                            BusinessUnderstandingConcept.UNDERSTANDING_LIMIT)));
                });
    }

    @Test
    public void parserReducesAtlasReportToSharedSemanticSummary() throws IOException {
        Path atlas = AtfSourcePaths.workspaceRoot()
                .resolve("docs/atlas/framework-under-test_business-test-atlas.html");
        Assert.assertTrue(Files.isRegularFile(atlas), "Expected framework-under-test Atlas artifact.");

        BusinessUnderstandingArtifactParser.ArtifactSummary summary =
                BusinessUnderstandingArtifactParser.parseHtml(
                        BusinessUnderstandingInputType.ATLAS_REPORT,
                        "framework-under-test_business-test-atlas.html",
                        Files.readString(atlas));

        BusinessUnderstandingReportNarrative.verify(
                CONSUMER_BRIDGE_RESOURCE,
                "business-understanding.consumer.atlas-business-audit",
                BusinessUnderstandingReportNarrative.observedSummary(summary),
                () -> {
                    Assert.assertTrue(summary.looksLikeAtlasBusinessAudit(),
                            "Atlas report must reduce to shared business audit semantics.");
                    Assert.assertTrue(summary.hasSectionContaining("Business Flow Graph"));
                    Assert.assertTrue(summary.hasSectionContaining("Business Evidence"));
                    Assert.assertTrue(summary.hasPublishableScoreBand());
                    Assert.assertTrue(summary.hasConcepts(Set.of(
                            BusinessUnderstandingConcept.BUSINESS_OBJECT,
                            BusinessUnderstandingConcept.BUSINESS_FLOW_STORY,
                            BusinessUnderstandingConcept.PROOF_STRIP,
                            BusinessUnderstandingConcept.SOURCE_TRACE,
                            BusinessUnderstandingConcept.SCORE_RATIONALE)));
                });
    }

    @Test
    public void parserReducesSourceStructureToSharedSemanticSummary() {
        BusinessUnderstandingArtifactParser.ArtifactSummary summary =
                BusinessUnderstandingArtifactParser.parseSourceStructure(
                        "src/test/java/com/endava/ai/ui/tests/RefundResolutionTests.java",
                        "RefundResolutionTests source-to-business trace maps support layer actions to "
                                + "business object Refund resolution with proof strip and comprehension limit.");

        BusinessUnderstandingReportNarrative.verify(
                SEMANTIC_MODEL_RESOURCE,
                "business-understanding.model.source-trace-row",
                BusinessUnderstandingReportNarrative.observedSummary(summary),
                () -> {
                    Assert.assertEquals(summary.inputType(), BusinessUnderstandingInputType.SOURCE_STRUCTURE);
                    Assert.assertTrue(summary.hasConcepts(Set.of(
                            BusinessUnderstandingConcept.BUSINESS_OBJECT,
                            BusinessUnderstandingConcept.SOURCE_TRACE,
                            BusinessUnderstandingConcept.SUPPORT_LAYER,
                            BusinessUnderstandingConcept.PROOF_STRIP,
                            BusinessUnderstandingConcept.UNDERSTANDING_LIMIT)));
                });
    }

    @Test
    public void parserReducesGitDiffToSharedSemanticSummary() {
        BusinessUnderstandingArtifactParser.ArtifactSummary summary =
                BusinessUnderstandingArtifactParser.parseGitDiff(
                        "root-source-thin-no-index.diff",
                        "+ business claim: staff user proves a durable result\n"
                                + "+ proof chain: test -> action -> validation -> evidence\n"
                                + "+ score rationale: cap rationale explains the limit\n");

        BusinessUnderstandingReportNarrative.verify(
                SEMANTIC_MODEL_RESOURCE,
                "business-understanding.model.proof-chain",
                BusinessUnderstandingReportNarrative.observedSummary(summary),
                () -> {
                    Assert.assertEquals(summary.inputType(), BusinessUnderstandingInputType.GIT_DIFF);
                    Assert.assertTrue(summary.hasConcepts(Set.of(
                            BusinessUnderstandingConcept.BUSINESS_CLAIM,
                            BusinessUnderstandingConcept.PROOF_CHAIN,
                            BusinessUnderstandingConcept.SCORE_RATIONALE,
                            BusinessUnderstandingConcept.UNDERSTANDING_LIMIT)));
                });
    }

    @Test
    public void parserReducesExtentReportToSharedSemanticSummary() {
        BusinessUnderstandingArtifactParser.ArtifactSummary summary =
                BusinessUnderstandingArtifactParser.parseExtentReport(
                        "ExtentReport.html",
                        "<html><body><h1>Extent proof</h1>"
                                + "<article class='score-card'><span>Tests</span><strong>97</strong></article>"
                                + "<p>business object evidence mapping proof chain with score overview and limit</p>"
                                + "<a href='screens/final-state.png'>final proof</a></body></html>");

        BusinessUnderstandingReportNarrative.verify(
                SEMANTIC_LEXICON_RESOURCE,
                "business-understanding.lexicon.proof-chain",
                BusinessUnderstandingReportNarrative.observedSummary(summary),
                () -> {
                    Assert.assertEquals(summary.inputType(), BusinessUnderstandingInputType.EXTENT_REPORT);
                    Assert.assertTrue(summary.hasPublishableScoreBand());
                    Assert.assertTrue(summary.hasLinkContaining("final-state"));
                    Assert.assertTrue(summary.hasConcepts(Set.of(
                            BusinessUnderstandingConcept.BUSINESS_OBJECT,
                            BusinessUnderstandingConcept.PROOF_CHAIN,
                            BusinessUnderstandingConcept.SCORE_RATIONALE,
                            BusinessUnderstandingConcept.UNDERSTANDING_LIMIT)));
                });
    }

    @Test
    public void parserRejectsBlankArtifactInputs() {
        BusinessUnderstandingReportNarrative.verify(
                "business-understanding.parser.rejects-blank-artifact",
                "Blank artifact payloads must fail before semantic reduction so reports do not publish empty business evidence.",
                "Parser rejected a blank FINAL_BUSINESS_REPORT payload with IllegalArgumentException.",
                "Pass a real artifact body into the parser before claiming business-understanding evidence.",
                () -> Assert.assertThrows(IllegalArgumentException.class, () ->
                        BusinessUnderstandingArtifactParser.parseHtml(
                                BusinessUnderstandingInputType.FINAL_BUSINESS_REPORT,
                                "blank.html",
                                " ")));
    }

    private static Path firstHtmlExLangGraphReport() throws IOException {
        Path root = AtfSourcePaths.workspaceRoot().resolve("docs/html-ex");
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals("langgraph-business-understanding.html"))
                    .sorted(Comparator.comparing(AtfSourcePaths::unixPath))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Expected at least one html-ex LangGraph report."));
        }
    }
}
