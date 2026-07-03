package com.endava.ai.skills.business.tests;

import com.endava.ai.skills.business.model.BusinessClaim;
import com.endava.ai.skills.business.model.BusinessFlowStory;
import com.endava.ai.skills.business.model.BusinessGraphConstruction;
import com.endava.ai.skills.business.taxonomy.BusinessMaturity;
import com.endava.ai.skills.business.model.BusinessObject;
import com.endava.ai.skills.business.model.BusinessProofChain;
import com.endava.ai.skills.business.model.BusinessProofStripRow;
import com.endava.ai.skills.business.model.BusinessScoreRationale;
import com.endava.ai.skills.business.model.BusinessSourceTraceRow;
import com.endava.ai.skills.business.taxonomy.BusinessUnderstandingConcept;
import com.endava.ai.skills.business.taxonomy.BusinessUnderstandingInputType;
import com.endava.ai.skills.business.BusinessUnderstandingKernel;
import com.endava.ai.skills.business.taxonomy.BusinessUnderstandingStage;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
public class BusinessUnderstandingKernelContractTest {
    private static final String PIPELINE_RESOURCE = "skills/business/contracts/semantic-pipeline.yml";
    private static final String SEMANTIC_MODEL_RESOURCE = "skills/business/contracts/semantic-model.yml";
    private static final String SEMANTIC_CALIBRATION_RESOURCE = "skills/business/contracts/semantic-calibration.yml";
    private static final String SEMANTIC_LEXICON_RESOURCE = "skills/business/contracts/semantic-lexicon.yml";

    @Test
    public void semanticPipelineIsInputAgnosticAndOrdered() {
        List<BusinessUnderstandingStage> pipeline = BusinessUnderstandingKernel.semanticPipeline();
        List<Integer> orders = pipeline.stream()
                .map(BusinessUnderstandingStage::order)
                .collect(Collectors.toList());
        List<Integer> sorted = orders.stream().sorted().collect(Collectors.toList());

        BusinessUnderstandingReportNarrative.verify(
                PIPELINE_RESOURCE,
                "business-understanding.stage.normalize-input",
                "Pipeline order=" + pipeline + ", stageOrders=" + orders,
                () -> {
                    Assert.assertEquals(pipeline.size(), BusinessUnderstandingStage.values().length,
                            "The kernel must expose every semantic stage.");
                    Assert.assertEquals(orders, sorted, "Business-understanding stages must be published in order.");

                    Assert.assertEquals(pipeline.get(0), BusinessUnderstandingStage.NORMALIZE_INPUT,
                            "Different input shapes must be normalized before semantic judgment.");
                    Assert.assertTrue(pipeline.indexOf(BusinessUnderstandingStage.MAP_SUPPORT_LAYERS)
                                    < pipeline.indexOf(BusinessUnderstandingStage.IDENTIFY_BUSINESS_OBJECTS),
                            "Support/application layers must be mapped before business objects are scored.");
                    Assert.assertTrue(pipeline.indexOf(BusinessUnderstandingStage.MAP_PROOF_CHAIN)
                                    < pipeline.indexOf(BusinessUnderstandingStage.SCORE_UNDERSTANDING_LIMITS),
                            "Proof chains must be known before business understanding scores are accepted.");
                });
    }

    @Test
    public void kernelSupportsMcpAndAtlasInputShapesWithoutChangingTheSemanticPath() {
        BusinessUnderstandingReportNarrative.verify(
                PIPELINE_RESOURCE,
                "business-understanding.stage.normalize-input",
                "Supported input types are checked for MCP diffs/Extent/final report and Atlas source/report shapes.",
                () -> {
                    Assert.assertTrue(BusinessUnderstandingKernel.supports(BusinessUnderstandingInputType.GIT_DIFF),
                            "MCP may start from git/root diffs.");
                    Assert.assertTrue(BusinessUnderstandingKernel.supports(BusinessUnderstandingInputType.EXTENT_REPORT),
                            "MCP may start from Extent evidence.");
                    Assert.assertTrue(BusinessUnderstandingKernel.supports(BusinessUnderstandingInputType.FINAL_BUSINESS_REPORT),
                            "MCP may start from analysis/langgraph-business-understanding.html.");
                    Assert.assertTrue(BusinessUnderstandingKernel.supports(BusinessUnderstandingInputType.SOURCE_STRUCTURE),
                            "Atlas may start from the current Java source structure.");
                    Assert.assertTrue(BusinessUnderstandingKernel.supports(BusinessUnderstandingInputType.ATLAS_REPORT),
                            "Atlas may start from generated Atlas report evidence.");
                });
    }

    @Test
    public void yamlSemanticPipelineMatchesJavaStages() {
        List<Map<String, String>> entries = SimpleYamlContractLoader.load(PIPELINE_RESOURCE);
        List<String> yamlStages = entries.stream()
                .sorted(Comparator.comparingInt(entry -> integer(entry, "order", PIPELINE_RESOURCE)))
                .map(entry -> required(entry, "stage", PIPELINE_RESOURCE))
                .collect(Collectors.toList());
        List<String> javaStages = BusinessUnderstandingKernel.semanticPipeline().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        BusinessUnderstandingReportNarrative.verify(
                PIPELINE_RESOURCE,
                "business-understanding.stage.publish-semantic-summary",
                "YAML stages=" + yamlStages + ", Java stages=" + javaStages,
                () -> {
                    Assert.assertEquals(entries.size(), BusinessUnderstandingStage.values().length,
                            "YAML semantic pipeline must stay aligned with Java stages.");
                    Assert.assertEquals(yamlStages, javaStages,
                            "YAML stages must match the canonical BusinessUnderstandingStage order.");
                    for (Map<String, String> entry : entries) {
                        Assert.assertEquals(required(entry, "kind", PIPELINE_RESOURCE), "semantic-stage",
                                "Only semantic-stage entries belong in the shared pipeline.");
                        Assert.assertEquals(required(entry, "inputAgnostic", PIPELINE_RESOURCE), "true",
                                "Every shared semantic stage must be input-agnostic.");
                        Assert.assertFalse(required(entry, "expected", PIPELINE_RESOURCE).isBlank(),
                                "Every semantic stage must explain expected behavior.");
                        Assert.assertFalse(required(entry, "repair", PIPELINE_RESOURCE).isBlank(),
                                "Every semantic stage must provide a repair route.");
                    }
                });
    }

    @Test
    public void kernelVocabularyCoversMcpAtlasSharedBusinessConcepts() {
        Set<BusinessUnderstandingConcept> concepts = BusinessUnderstandingKernel.concepts();
        BusinessUnderstandingReportNarrative.verify(
                SEMANTIC_LEXICON_RESOURCE,
                "business-understanding.lexicon.business-object",
                "Kernel concepts=" + concepts,
                () -> {
                    Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.BUSINESS_OBJECT));
                    Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.BUSINESS_CLAIM));
                    Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.BUSINESS_FLOW_STORY));
                    Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.BUSINESS_MATURITY));
                    Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.SOURCE_TRACE));
                    Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.PROOF_CHAIN));
                    Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.PROOF_STRIP));
                    Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.SCORE_RATIONALE));
                    Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.SCORE_TRUTH));
                    Assert.assertTrue(concepts.contains(BusinessUnderstandingConcept.UNDERSTANDING_LIMIT));
                });
    }

    @Test
    public void semanticModelsTranslateGoldenAtlasBusinessUnderstandingPatterns() {
        List<Map<String, String>> entries = SimpleYamlContractLoader.load(SEMANTIC_MODEL_RESOURCE);
        Set<String> models = entries.stream()
                .map(entry -> required(entry, "model", SEMANTIC_MODEL_RESOURCE))
                .collect(Collectors.toSet());

        BusinessUnderstandingReportNarrative.verify(
                SEMANTIC_MODEL_RESOURCE,
                "business-understanding.model.flow-story",
                "Semantic models=" + models + ", model rows=" + entries.size(),
                () -> {
                    Assert.assertTrue(models.contains("BusinessFlowStory"),
                            "Golden Atlas flow rows use actor -> intent -> visible action -> business object -> result.");
                    Assert.assertTrue(models.contains("BusinessGraphConstruction"),
                            "Business graphs must start from domain objects, not run/proof artifact chains.");
                    Assert.assertTrue(models.contains("BusinessMaturity"),
                            "Golden Atlas separates owned/partial/diagnostic/surface-only proof depth.");
                    Assert.assertTrue(models.contains("BusinessProofStripRow"),
                            "Golden Atlas keeps node proof strips local to each graph.");
                    Assert.assertTrue(models.contains("BusinessSourceTraceRow"),
                            "Golden Atlas reopens business claims from source and layer trace.");
                    Assert.assertTrue(models.contains("BusinessScoreRationale"),
                            "Golden Atlas makes score cards audit rows, not decorative numbers.");

                    for (Map<String, String> entry : entries) {
                        Assert.assertEquals(required(entry, "kind", SEMANTIC_MODEL_RESOURCE), "semantic-model",
                                "Only semantic-model entries belong in the shared business model contract.");
                        Assert.assertFalse(required(entry, "sourcePattern", SEMANTIC_MODEL_RESOURCE).isBlank(),
                                "Each business model must name the golden-report pattern it abstracts.");
                        Assert.assertFalse(required(entry, "requiredFields", SEMANTIC_MODEL_RESOURCE).isBlank(),
                                "Each business model must publish the fields an agent has to fill.");
                        Assert.assertFalse(required(entry, "expected", SEMANTIC_MODEL_RESOURCE).isBlank(),
                                "Each business model must explain its invariant.");
                        Assert.assertFalse(required(entry, "repair", SEMANTIC_MODEL_RESOURCE).isBlank(),
                                "Each business model must provide calibration guidance.");
                    }
                    Map<String, String> graph = entries.stream()
                            .filter(entry -> "BusinessGraphConstruction".equals(entry.get("model")))
                            .findFirst()
                            .orElseThrow(() -> new AssertionError("Missing BusinessGraphConstruction semantic model."));
                    assertCsvContains(graph, "constructionSteps",
                            "start-from-business-object",
                            "choose-primary-owned-object",
                            "label-business-relationships",
                            "separate-frontier-as-dashed-or-capped",
                            "move-proof-artifacts-outside-graph");
                    assertCsvContains(graph, "forbiddenNodeTokens", "run_", "TestNG", "XML", "Extent", "MCP");
                    assertCsvContains(graph, "visualRequirements",
                            "primary-owned-node-highlighted",
                            "frontier-visible-and-limited",
                            "relationship-labels-present");
                });
    }

    @Test
    public void neutralValueObjectsCarryClaimFlowTraceScoreEvidenceAndLimits() {
        BusinessObject object = new BusinessObject("Refund resolution", "staff-approved refund is visible");
        BusinessClaim claim = new BusinessClaim(object, "Staff can approve a refund with auditable result");
        BusinessFlowStory story = new BusinessFlowStory(
                "staff user",
                "resolve customer refund",
                "approve refund from admin workspace",
                object,
                "approved refund resolution is visible after reopen");
        BusinessGraphConstruction graph = new BusinessGraphConstruction(
                object,
                "support staff resolves refund request",
                object,
                List.of("anchors", "captures", "submits", "awaits decision"),
                new BusinessObject("Settlement posting", "downstream settlement remains capped"),
                "Proof stays in Extent, XML, MCP, diff and source trace artifacts");
        BusinessProofChain chain = new BusinessProofChain(
                claim,
                List.of("ui.support.refunds", "api.support.refunds"),
                List.of("root-source-thin-no-index.diff", "ExtentReport.html"),
                "Does not prove downstream settlement posting");
        BusinessProofStripRow proofStripRow = new BusinessProofStripRow(
                "Refund approval",
                "staffApprovesRefundResolution()",
                List.of("test", "action", "page", "validation", "business object"),
                "Refund approval is visible and reopenable.",
                "Settlement posting remains out of scope.");
        BusinessSourceTraceRow traceRow = new BusinessSourceTraceRow(
                "RefundResolutionTests.staffApprovesRefundResolution()",
                object,
                List.of("test", "action", "page", "validation", "model"),
                BusinessMaturity.OWNED,
                "No payment provider audit is inferred.");
        BusinessScoreRationale rationale = new BusinessScoreRationale(
                "Business Understanding",
                94,
                "explain durable refund ownership",
                "does the flow prove a reopenable business object?",
                "RefundResolutionTests -> actions -> validation -> proof strip",
                "capped below 95 until settlement evidence exists");

        BusinessUnderstandingReportNarrative.verify(
                SEMANTIC_MODEL_RESOURCE,
                "business-understanding.model.proof-chain",
                "Story=" + story.actor() + " -> " + story.businessObject().name()
                        + ", supportLayers=" + chain.supportLayers()
                        + ", evidence=" + chain.evidenceReferences()
                        + ", score=" + rationale.score(),
                () -> {
                    Assert.assertEquals(story.actor(), "staff user");
                    Assert.assertEquals(story.businessObject().name(), "Refund resolution");
                    Assert.assertTrue(graph.startsFromBusinessObject());
                    Assert.assertTrue(graph.hasLabeledBusinessRelationships());
                    Assert.assertTrue(graph.separatesProofBoundary());
                    Assert.assertEquals(chain.claim().businessObject().name(), "Refund resolution");
                    Assert.assertEquals(chain.supportLayers().size(), 2);
                    Assert.assertTrue(chain.evidenceReferences().contains("ExtentReport.html"));
                    Assert.assertTrue(chain.understandingLimit().contains("Does not prove"));
                    Assert.assertEquals(proofStripRow.layerPath().get(0), "test");
                    Assert.assertEquals(traceRow.maturity(), BusinessMaturity.OWNED);
                    Assert.assertTrue(traceRow.maturity().strongOwnership());
                    Assert.assertEquals(rationale.score(), 94);
                    Assert.assertTrue(rationale.flowQuestion().contains("reopenable business object"));
                });
    }

    @Test
    public void semanticCalibrationRulesReferenceExistingModelsAndConcepts() {
        List<Map<String, String>> calibration = SimpleYamlContractLoader.load(SEMANTIC_CALIBRATION_RESOURCE);
        Set<String> modelNames = SimpleYamlContractLoader.load(SEMANTIC_MODEL_RESOURCE).stream()
                .map(entry -> required(entry, "model", SEMANTIC_MODEL_RESOURCE))
                .collect(Collectors.toSet());
        Set<String> conceptNames = BusinessUnderstandingKernel.concepts().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        BusinessUnderstandingReportNarrative.verify(
                SEMANTIC_CALIBRATION_RESOURCE,
                "business-understanding.calibration.class-reading-chain",
                "Calibration rows=" + calibration.size()
                        + ", known models=" + modelNames
                        + ", known concepts=" + conceptNames.size(),
                () -> {
                    Assert.assertFalse(calibration.isEmpty(), "Business-understanding semantic calibration must not be empty.");
                    for (Map<String, String> entry : calibration) {
                        Assert.assertEquals(required(entry, "kind", SEMANTIC_CALIBRATION_RESOURCE), "semantic-calibration",
                                "Only semantic-calibration entries belong in the business calibration contract.");
                        assertCsvValuesExist(entry, "requiredModels", modelNames, SEMANTIC_CALIBRATION_RESOURCE);
                        assertCsvValuesExist(entry, "requiredConcepts", conceptNames, SEMANTIC_CALIBRATION_RESOURCE);
                        Assert.assertFalse(required(entry, "expected", SEMANTIC_CALIBRATION_RESOURCE).isBlank(),
                                "Each calibration rule must state the expected business-understanding invariant.");
                        Assert.assertFalse(required(entry, "repair", SEMANTIC_CALIBRATION_RESOURCE).isBlank(),
                                "Each calibration rule must give an actionable repair route.");
                        Assert.assertFalse(required(entry, "expected", SEMANTIC_CALIBRATION_RESOURCE).contains("MCP close-out readiness")
                                        && required(entry, "id", SEMANTIC_CALIBRATION_RESOURCE).contains("business-object"),
                                "Business object rules must not become MCP close-out rules.");
                    }
                });
    }

    @Test
    public void semanticCalibrationPreservesGoldenAtlasLessons() {
        String calibration = readResource(SEMANTIC_CALIBRATION_RESOURCE);
        BusinessUnderstandingReportNarrative.verify(
                SEMANTIC_CALIBRATION_RESOURCE,
                "business-understanding.calibration.business-object-not-technical-layer",
                "Calibration artifact length=" + calibration.length()
                        + ", required golden lessons are checked by id.",
                () -> {
                    Assert.assertTrue(calibration.contains("business-object-not-technical-layer"),
                            "Golden Atlas keeps business nodes separate from technical layer evidence.");
                    Assert.assertTrue(calibration.contains("local-proof-before-global-summary"),
                            "Golden Atlas places proof strips next to graph/subgraph claims.");
                    Assert.assertTrue(calibration.contains("maturity-before-95"),
                            "Golden Atlas ties high scores to owned evidence and explicit limits.");
                    Assert.assertTrue(calibration.contains("suite-readiness-separated"),
                            "Golden Atlas separates understanding score from package/readiness gates.");
                    Assert.assertTrue(calibration.contains("repeated-corridor-signal"),
                            "Golden Atlas treats repeated corridors as evidence only when source-backed.");
                });
    }

    @Test
    public void semanticLexiconIsYamlOwnedAndCoversSharedConcepts() {
        List<Map<String, String>> entries = SimpleYamlContractLoader.load(SEMANTIC_LEXICON_RESOURCE);
        Set<String> lexiconConcepts = entries.stream()
                .map(entry -> required(entry, "concept", SEMANTIC_LEXICON_RESOURCE))
                .collect(Collectors.toSet());
        Set<String> kernelConcepts = BusinessUnderstandingKernel.concepts().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        BusinessUnderstandingReportNarrative.verify(
                SEMANTIC_LEXICON_RESOURCE,
                "business-understanding.lexicon.score-rationale",
                "Lexicon concepts=" + lexiconConcepts + ", kernel concepts=" + kernelConcepts,
                () -> {
                    Assert.assertFalse(entries.isEmpty(), "Business-understanding semantic lexicon must not be empty.");
                    Assert.assertTrue(lexiconConcepts.containsAll(Set.of(
                                    "BUSINESS_OBJECT",
                                    "BUSINESS_FLOW",
                                    "SOURCE_TRACE",
                                    "PROOF_CHAIN",
                                    "SCORE_RATIONALE",
                                    "UNDERSTANDING_LIMIT")),
                            "Lexicon must cover the core concepts parsed from real MCP/Atlas artifacts.");
                    Assert.assertTrue(kernelConcepts.containsAll(lexiconConcepts),
                            "Lexicon must not invent concepts outside BusinessUnderstandingConcept.");

                    for (Map<String, String> entry : entries) {
                        Assert.assertEquals(required(entry, "kind", SEMANTIC_LEXICON_RESOURCE), "semantic-lexicon",
                                "Only semantic-lexicon entries belong in the business lexicon contract.");
                        Assert.assertFalse(required(entry, "tokens", SEMANTIC_LEXICON_RESOURCE).isBlank(),
                                "Every lexicon entry must expose parser tokens.");
                        Assert.assertFalse(required(entry, "expected", SEMANTIC_LEXICON_RESOURCE).isBlank(),
                                "Every lexicon entry must explain the concept invariant.");
                        Assert.assertFalse(required(entry, "repair", SEMANTIC_LEXICON_RESOURCE).isBlank(),
                                "Every lexicon entry must provide calibration guidance.");
                        if (entry.containsKey("impliedConcepts")) {
                            assertCsvValuesExist(entry, "impliedConcepts", kernelConcepts, SEMANTIC_LEXICON_RESOURCE);
                        }
                    }
                });
    }

    private static void assertCsvValuesExist(Map<String, String> entry,
                                             String key,
                                             Set<String> allowedValues,
                                             String resource) {
        String value = entry.get(key);
        if (value == null || value.isBlank()) {
            return;
        }
        for (String token : value.split(",")) {
            String trimmed = token.trim();
            Assert.assertTrue(allowedValues.contains(trimmed),
                    key + " references unknown value " + trimmed + " in " + required(entry, "id", resource));
        }
    }

    private static void assertCsvContains(Map<String, String> entry, String key, String... expectedValues) {
        Set<String> values = Set.of(required(entry, key, SEMANTIC_MODEL_RESOURCE).split(","));
        for (String expectedValue : expectedValues) {
            Assert.assertTrue(values.contains(expectedValue),
                    key + " must include `" + expectedValue + "` for BusinessGraphConstruction.");
        }
    }

    private static int integer(Map<String, String> entry, String key, String resource) {
        return Integer.parseInt(required(entry, key, resource));
    }

    private static String required(Map<String, String> entry, String key, String resource) {
        return SimpleYamlContractLoader.required(entry, key, resource);
    }

    private static String readResource(String resource) {
        try {
            return Files.readString(AtfSourcePaths.workspaceRoot().resolve("src/main/resources").resolve(resource));
        } catch (IOException e) {
            throw new AssertionError("Could not read resource " + resource, e);
        }
    }
}
