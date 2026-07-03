package com.endava.ai.skills.atlas.tests.yaml;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.business.model.BusinessFlowStory;
import com.endava.ai.skills.business.taxonomy.BusinessMaturity;
import com.endava.ai.skills.business.model.BusinessObject;
import com.endava.ai.skills.business.model.BusinessProofStripRow;
import com.endava.ai.skills.business.model.BusinessScoreRationale;
import com.endava.ai.skills.business.model.BusinessSourceTraceRow;
import com.endava.ai.skills.business.taxonomy.BusinessUnderstandingConcept;
import com.endava.ai.skills.business.taxonomy.BusinessUnderstandingInputType;
import com.endava.ai.skills.business.BusinessUnderstandingKernel;
import com.endava.ai.skills.business.taxonomy.BusinessUnderstandingStage;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
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
public class AtlasBusinessUnderstandingConsumerContractTest {
    private static final String SKILL_ARCHITECTURE = "atf-core/contracts/skills/skill-architecture.yml";
    private static final String QUALITY_LANES = "skills/atlas/contracts/agentic-quality-lanes.yml";

    @Test
    public void atlasDeclaresBusinessUnderstandingAsSharedSemanticConsumer() {
        Map<String, String> atlas = SimpleYamlContractLoader.load(SKILL_ARCHITECTURE).stream()
                .filter(entry -> "skill.atlas".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing skill.atlas row in skill-architecture.yml"));

        Assert.assertTrue(atlas.values().stream().anyMatch(value -> value.contains("skill.business-understanding")),
                "Atlas must consume the shared Business Understanding kernel instead of becoming the only semantic owner.");
        Assert.assertTrue(atlas.get("artifactPolicy").contains("business-understanding atlas artifacts"),
                "Atlas owns Atlas artifacts, not MCP package publishability.");
    }

    @Test
    public void atlasQualityLanesMapToSharedBusinessUnderstandingConcepts() {
        String lanes = read(AtfSourcePaths.workspaceRoot()
                .resolve("src/main/resources")
                .resolve(QUALITY_LANES))
                .toLowerCase();

        Assert.assertTrue(lanes.contains("business") && lanes.contains("understanding"));
        Assert.assertTrue(lanes.contains("source-to-business trace"));
        Assert.assertTrue(lanes.contains("score-truth"));
        Assert.assertTrue(lanes.contains("business graph"));

        Set<BusinessUnderstandingConcept> concepts = BusinessUnderstandingKernel.concepts();
        Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.BUSINESS_FLOW_STORY));
        Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.SOURCE_TRACE));
        Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.PROOF_STRIP));
        Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.SCORE_TRUTH));
    }

    @Test
    public void atlasSourceBackedSemanticsReduceToSharedBusinessUnderstandingModels() {
        BusinessObject object = new BusinessObject(
                "source-backed business capability",
                "business behavior is reconstructed from Java tests and support layers");
        BusinessFlowStory story = new BusinessFlowStory(
                "product user",
                "complete a meaningful application flow",
                "exercise UI/API behavior through tests",
                object,
                "Atlas report publishes business graph, trace and score truth");
        BusinessSourceTraceRow trace = new BusinessSourceTraceRow(
                "com.endava.ai.ui.tests.*",
                object,
                List.of("test", "support action", "service/page", "validation", "business object"),
                BusinessMaturity.OWNED,
                "Does not decide whether MCP package artifacts are publishable.");
        BusinessProofStripRow proofStrip = new BusinessProofStripRow(
                "Business Flow Understanding",
                "AtlasCandidateGraphContractTest.candidateAtlasGraphNodesHaveBusinessProofStrips()",
                List.of("Atlas graph", "proof strip", "source trace", "score rationale"),
                "Each graph node has nearby proof and source-backed business evidence.",
                "MCP close-out remains outside Atlas.");
        BusinessScoreRationale score = new BusinessScoreRationale(
                "Business Understanding",
                95,
                "score source-backed understanding, not package readiness",
                "does source trace prove the business object and its limits?",
                "Atlas rules: graph, trace, proof strip, score truth",
                "cap if proof strips are generic or source references are missing");

        Assert.assertEquals(story.businessObject().name(), object.name());
        Assert.assertEquals(trace.maturity(), BusinessMaturity.OWNED);
        Assert.assertTrue(proofStrip.layerPath().contains("source trace"));
        Assert.assertTrue(score.capRationale().contains("source references"));
    }

    @Test
    public void atlasBusinessVocabularyRuleConsumesSharedBusinessUnderstandingArtifactParser() {
        Path rule = AtfSourcePaths.mainJava("com.endava.ai.skills.atlas.rules.business.AtlasComprehensionVocabularyRule");
        String source = read(rule);

        Assert.assertTrue(source.contains("BusinessUnderstandingArtifactParser"),
                "Atlas vocabulary rule must consume the shared Business Understanding artifact parser.");
        Assert.assertTrue(source.contains("BusinessUnderstandingInputType.ATLAS_REPORT"),
                "Atlas must identify its report input as ATLAS_REPORT.");
        Assert.assertTrue(source.contains("business flow story, source trace and understanding limits"),
                "Atlas must report a stable shared-semantics calibration message.");
    }

    @Test
    public void atlasDoesNotImportMcpWhileUsingSharedBusinessVocabulary() throws IOException {
        Assert.assertTrue(BusinessUnderstandingKernel.supports(BusinessUnderstandingInputType.SOURCE_STRUCTURE));
        Assert.assertTrue(BusinessUnderstandingKernel.supports(BusinessUnderstandingInputType.ATLAS_REPORT));
        Assert.assertTrue(BusinessUnderstandingKernel.semanticPipeline()
                        .contains(BusinessUnderstandingStage.MAP_SUPPORT_LAYERS),
                "Atlas source analysis must still pass through the shared support-layer stage.");

        List<String> mcpImports;
        try (Stream<Path> sources = Files.walk(AtfSourcePaths.MAIN_ATLAS_ROOT)) {
            mcpImports = sources
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(source -> read(source).contains(forbiddenMcpImport()))
                    .map(AtfSourcePaths::unixPath)
                    .collect(Collectors.toList());
        }
        Assert.assertTrue(mcpImports.isEmpty(),
                "Atlas may consume shared Business Understanding, but must not import MCP: " + mcpImports);
    }

    private static String read(Path source) {
        try {
            return Files.readString(source);
        } catch (IOException exception) {
            throw new AssertionError("Cannot read " + source, exception);
        }
    }

    private static String forbiddenMcpImport() {
        return "import " + "com.endava.ai.skills." + "mcp.";
    }
}
