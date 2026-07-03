package com.endava.ai.skills.mcp.tests.core;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.skills.mcp.contracts.candidate.McpHtmlExCorpusSpec;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.validation.profile.McpReportSections;
import com.endava.ai.skills.mcp.validation.profile.html.McpHtmlExampleReference;
import com.endava.ai.skills.mcp.validation.profile.html.McpHtmlExampleReferenceCorpus;
import com.endava.ai.skills.mcp.validation.profile.html.McpReportTemplateConsensus;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-html-ex-reference-health-contract-test")
public class McpHtmlExReferenceHealthContractTest {
    private static final List<String> REQUIRED_PRIMARY_REPORT_TOKENS = List.of(
            "class=\"score-wall\"",
            "Executive Context",
            "Business Flow Graph",
            "Score Overview",
            "Proof Mapping",
            "Run Audit",
            "MCP Gate References",
            "Artifact Index",
            "Close-Out",
            "shared-report-reference.css");
    private static final List<String> REQUIRED_PACKAGE_ARTIFACTS = List.of(
            McpArtifactPaths.README,
            McpArtifactPaths.FINAL_REPORT,
            McpArtifactPaths.REPORT_CSS,
            McpArtifactPaths.PACKAGE_STATE,
            McpArtifactPaths.SOURCE_EVOLUTION_LEDGER,
            McpArtifactPaths.ROOT_DIFF,
            McpArtifactPaths.MCP_EXTENT_REPORT,
            McpArtifactPaths.MCP_EXTENT_SHA256,
            McpArtifactPaths.MCP_EXTENT_ORIGIN,
            McpArtifactPaths.MCP_GATE_RESULT,
            McpArtifactPaths.MCP_METHOD_LEDGER,
            McpArtifactPaths.MCP_TESTNG_XML,
            McpArtifactPaths.MCP_ALTERNATE_TESTNG_XML);

    @Test
    @McpCheckRef("mcp.html-ex-reference.sanity-library-health")
    public void htmlExampleReferenceLibraryIsUsableByMcpSanityAndCloseout() throws IOException {
        McpHtmlExampleReferenceCorpus corpus = McpHtmlExampleReferenceCorpus.load();
        McpHtmlExCorpusSpec spec = corpus.spec();
        Path root = corpus.root();
        Path readme = corpus.readme();
        McpHtmlExampleReference primary = corpus.primaryReference();
        Path primaryReport = primary.report();

        Assert.assertTrue(Files.isDirectory(root), "Missing html-ex library root: " + root);
        Assert.assertTrue(Files.isRegularFile(readme), "Missing html-ex library README: " + readme);
        Assert.assertTrue(Files.isDirectory(primary.root()),
                "Primary structural html-ex reference directory is missing: " + primary.root());
        Assert.assertTrue(Files.isRegularFile(primaryReport),
                "Primary structural html-ex reference report is missing: " + primaryReport);

        Set<String> activeExamples = new LinkedHashSet<>(corpus.exampleNames());
        Assert.assertTrue(activeExamples.size() >= spec.minimumExamples(),
                "docs/html-ex README must list at least " + spec.minimumExamples()
                        + " active examples, actual=" + activeExamples);
        Assert.assertFalse(corpus.reusableReferences().isEmpty(),
                "docs/html-ex must expose at least one current-standard reusable template reference.");
        Assert.assertTrue(activeExamples.contains(primary.exampleName()),
                "Auto-selected primary html-ex reference must come from docs/html-ex README Active Library: "
                        + primary.exampleName());
        Assert.assertTrue(primary.hasReusableCloseoutSpine(),
                "Auto-selected primary html-ex reference must expose the reusable close-out spine. Missing: "
                        + primary.missingReusableCloseoutArtifacts());
        Assert.assertTrue(primary.hasReusableReportShape(),
                "Auto-selected primary html-ex reference must expose the current report template shape. Missing: "
                        + primary.missingReusableReportTokens());

        List<String> directories = corpus.directoryNames();
        Assert.assertEquals(directories, activeExamples.stream().sorted().collect(Collectors.toUnmodifiableList()),
                "docs/html-ex README Active Library must match the actual example directories exactly.");

        for (McpHtmlExampleReference reference : corpus.references()) {
            assertExamplePackageSpine(reference.root(), reference.exampleName());
        }

        String html = Files.readString(primaryReport);
        for (String token : REQUIRED_PRIMARY_REPORT_TOKENS) {
            Assert.assertTrue(html.contains(token),
                    "Primary html-ex report missing structural token `" + token + "`: " + primaryReport);
        }
        Assert.assertFalse(McpHtmlExampleReference.scoreCardLabels(html).isEmpty(),
                "Primary html-ex report must expose parseable score-card labels: " + primaryReport);
    }

    @Test
    @McpCheckRef("mcp.html-ex-reference.template-consensus")
    public void htmlExampleReferenceCorpusPublishesReusableTemplateConsensus() throws IOException {
        McpReportTemplateConsensus consensus = McpReportTemplateConsensus.fromHtmlExCorpus();

        Assert.assertFalse(consensus.sources().isEmpty(),
                "Template consensus must be backed by at least one current-standard reusable html-ex example.");
        Assert.assertTrue(consensus.scoreCardLabelsStable(),
                "All reusable html-ex examples must agree on score-card labels/order before MCP derives expectations: "
                        + consensus.sources());
        Assert.assertTrue(consensus.scoreCardLabels().size() >= 6,
                "html-ex consensus must expose the complete opening score wall.");
        Assert.assertEquals(consensus.scoreCardLabels().get(consensus.scoreCardLabels().size() - 1), "Overall",
                "Overall must remain the final score-card in the reusable html-ex opening shape.");
        Assert.assertTrue(consensus.visualClasses().containsAll(List.of("wrap", "hero", "score-wall", "score-card")),
                "html-ex consensus must expose the reusable visual shell classes: " + consensus.visualClasses());
        Assert.assertEquals(consensus.openSections(), List.of(McpReportSections.BUSINESS_FLOW_GRAPH),
                "Only Business Flow Graph should be default-open in the reusable html-ex structure.");
        Assert.assertTrue(consensus.collapsedSections().containsAll(McpReportSections.COLLAPSED_MAIN),
                "html-ex consensus must keep the major evidence sections collapsed: "
                        + consensus.collapsedSections());
        Assert.assertFalse(consensus.proofHeaders().isEmpty(),
                "html-ex consensus must derive Proof Mapping table headers instead of hardcoded expected text.");
        Assert.assertFalse(consensus.runAuditEvidenceTokens().isEmpty(),
                "html-ex consensus must derive Run Audit evidence tokens.");
        Assert.assertFalse(consensus.artifactIndexTokens().isEmpty(),
                "html-ex consensus must derive Artifact Index evidence tokens.");
    }

    private static void assertExamplePackageSpine(Path exampleRoot, String example) {
        for (String relative : REQUIRED_PACKAGE_ARTIFACTS) {
            Path artifact = exampleRoot.resolve(relative).normalize();
            Assert.assertTrue(Files.isRegularFile(artifact),
                    "html-ex example `" + example + "` missing required reusable artifact: " + relative);
        }
    }
}
