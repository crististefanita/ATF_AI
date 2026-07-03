package com.endava.ai.skills.mcp.tests.contracts;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.business.model.BusinessClaim;
import com.endava.ai.skills.business.model.BusinessFlowStory;
import com.endava.ai.skills.business.model.BusinessObject;
import com.endava.ai.skills.business.model.BusinessProofChain;
import com.endava.ai.skills.business.model.BusinessProofStripRow;
import com.endava.ai.skills.business.model.BusinessScoreRationale;
import com.endava.ai.skills.business.taxonomy.BusinessUnderstandingConcept;
import com.endava.ai.skills.business.BusinessUnderstandingKernel;
import com.endava.ai.skills.business.taxonomy.BusinessUnderstandingStage;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-business-understanding-consumer-contract-test")
public class McpBusinessUnderstandingConsumerContractTest {
    private static final String SKILL_ARCHITECTURE = "atf-core/contracts/skills/skill-architecture.yml";
    private static final String MCP_PROFILES = "skills/mcp/contracts/profiles.yml";

    @Test
    public void mcpDeclaresBusinessUnderstandingAsSharedSemanticConsumer() {
        Map<String, String> mcp = SimpleYamlContractLoader.load(SKILL_ARCHITECTURE).stream()
                .filter(entry -> "skill.mcp".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing skill.mcp row in skill-architecture.yml"));

        Assert.assertTrue(mcp.values().stream().anyMatch(value -> value.contains("skill.business-understanding")),
                "MCP must consume the shared Business Understanding kernel instead of inventing a parallel language.");
        Assert.assertTrue(mcp.get("artifactPolicy").contains("package validation artifacts"),
                "MCP remains package governance; Business Understanding only supplies semantics.");
    }

    @Test
    public void langGraphFinalReportProfileRunsAfterSurfaceCloseoutAndOwnsSemanticValidation() {
        Map<String, String> profile = SimpleYamlContractLoader.load(MCP_PROFILES).stream()
                .filter(entry -> "mcp-langgraph-final-report".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing mcp-langgraph-final-report profile"));

        Assert.assertTrue(profile.get("role").contains("business structure"));
        Assert.assertTrue(profile.get("role").contains("graph"));
        Assert.assertTrue(profile.get("role").contains("score"));
        Assert.assertTrue(profile.get("role").contains("artifact proof"));
        Assert.assertTrue(profile.get("expectedMethods").contains("businessFlowGraphIsRenderedAsDiagram"));
        Assert.assertTrue(profile.get("expectedMethods").contains("finalReportProofMappingLinksAllMandatoryArtifactFamilies"));
        Assert.assertTrue(profile.get("expectedMethods").contains("finalReportScoreOverviewUsesCanonicalBusinessAxes"));
    }

    @Test
    public void mcpFinalReportSemanticsReduceToSharedBusinessUnderstandingModels() {
        BusinessObject object = new BusinessObject(
                "generated package business object",
                "durable result is published in analysis/langgraph-business-understanding.html");
        BusinessClaim claim = new BusinessClaim(object,
                "final MCP report explains package business value through frozen proof");
        BusinessFlowStory story = new BusinessFlowStory(
                "agentic AI",
                "close a generated package truthfully",
                "publish final LangGraph business report",
                object,
                "reader can reopen graph, proof mapping, score and artifact links");
        BusinessProofChain proof = new BusinessProofChain(
                claim,
                List.of("source diff", "run evidence", "validation/mcp", "final report"),
                List.of(
                        "review/root-source-thin-no-index.diff",
                        "validation/mcp/ExtentReport.html",
                        "analysis/langgraph-business-understanding.html"),
                "MCP publishability is still decided by MCP gates, not by the shared kernel");
        BusinessProofStripRow proofStrip = new BusinessProofStripRow(
                "Business Flow Graph",
                "businessFlowGraphIsRenderedAsDiagram()",
                List.of("MCP report", "graph", "proof mapping", "artifact link"),
                "Graph nodes and proof rows are locally reopenable from frozen artifacts.",
                "Does not replace Atlas source-backed business scoring.");
        BusinessScoreRationale score = new BusinessScoreRationale(
                "Business Understanding",
                94,
                "score final report meaning without hiding package blockers",
                "does the report connect business object, proof chain, score and limits?",
                "MCP LangGraph final report contract methods",
                "cap below 95 if semantic proof is only declared, not locally reopenable");

        Assert.assertEquals(story.businessObject().name(), object.name());
        Assert.assertTrue(proof.evidenceReferences().contains("validation/mcp/ExtentReport.html"));
        Assert.assertTrue(proofStrip.layerPath().contains("proof mapping"));
        Assert.assertTrue(score.capRationale().contains("locally reopenable"));
    }

    @Test
    public void mcpFinalReportRuleConsumesSharedBusinessUnderstandingArtifactParser() {
        Path rule = AtfSourcePaths.mainJava("com.endava.ai.skills.mcp.rules.report.BusinessGraphReportRule");
        String source = read(rule);

        Assert.assertTrue(source.contains("BusinessUnderstandingArtifactParser"),
                "MCP final report graph rule must consume the shared Business Understanding artifact parser.");
        Assert.assertTrue(source.contains("BusinessUnderstandingInputType.FINAL_BUSINESS_REPORT"),
                "MCP must identify LangGraph report input as FINAL_BUSINESS_REPORT.");
        Assert.assertTrue(source.contains("report.business-understanding.shared-semantics"),
                "MCP must emit a stable blocker code when shared semantics are missing.");
    }

    @Test
    public void mcpDoesNotImportAtlasWhileUsingSharedBusinessVocabulary() throws IOException {
        Assert.assertTrue(BusinessUnderstandingKernel.concepts().contains(BusinessUnderstandingConcept.PROOF_CHAIN));
        Assert.assertTrue(BusinessUnderstandingKernel.semanticPipeline()
                        .contains(BusinessUnderstandingStage.SCORE_UNDERSTANDING_LIMITS),
                "MCP final report scoring must use the shared score/limit stage vocabulary.");

        List<String> atlasImports;
        try (Stream<Path> sources = Files.walk(AtfSourcePaths.MAIN_MCP_ROOT)) {
            atlasImports = sources
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(source -> read(source).contains(forbiddenAtlasImport()))
                    .map(AtfSourcePaths::unixPath)
                    .collect(Collectors.toList());
        }
        Assert.assertTrue(atlasImports.isEmpty(),
                "MCP may consume shared Business Understanding, but must not import Atlas: " + atlasImports);

        Set<String> required = Set.of("BUSINESS_OBJECT", "PROOF_CHAIN", "SCORE_RATIONALE", "UNDERSTANDING_LIMIT");
        Set<String> actual = BusinessUnderstandingKernel.concepts().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        Assert.assertTrue(actual.containsAll(required), "Shared vocabulary missing MCP semantic concepts.");
    }

    private static String read(Path source) {
        try {
            return Files.readString(source);
        } catch (IOException exception) {
            throw new AssertionError("Cannot read " + source, exception);
        }
    }

    private static String forbiddenAtlasImport() {
        return "import " + "com.endava.ai.skills." + "atlas.";
    }
}
