package com.ai.skills.business.tests;

import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.business.taxonomy.BusinessUnderstandingConcept;
import com.ai.skills.business.taxonomy.BusinessUnderstandingInputType;
import com.ai.skills.business.taxonomy.BusinessUnderstandingStage;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
public class BusinessUnderstandingConsumerBridgeContractTest {
    private static final String BRIDGE_RESOURCE = "skills/business/contracts/consumer-bridge.yml";
    private static final String MODEL_RESOURCE = "skills/business/contracts/semantic-model.yml";
    private static final Set<String> EXPECTED_CONSUMERS = Set.of("skill.mcp", "skill.atlas");

    @Test
    public void consumerBridgeDeclaresMcpAndAtlasWithoutCreatingRuntimeCoupling() {
        List<Map<String, String>> entries = SimpleYamlContractLoader.load(BRIDGE_RESOURCE);
        Set<String> consumers = entries.stream()
                .map(entry -> required(entry, "consumer"))
                .collect(Collectors.toSet());

        BusinessUnderstandingReportNarrative.verify(
                BRIDGE_RESOURCE,
                "business-understanding.consumer.mcp-langgraph-final-report",
                "Declared semantic consumers=" + consumers + ", bridge entries=" + entries.size(),
                () -> {
                    Assert.assertEquals(consumers, EXPECTED_CONSUMERS,
                            "Business Understanding bridge must declare exactly the MCP and Atlas semantic consumers.");

                    for (Map<String, String> entry : entries) {
                        Assert.assertEquals(required(entry, "kind"), "semantic-consumer");
                        Assert.assertTrue(Files.isRegularFile(path(required(entry, "ownerSuite"))),
                                "Consumer bridge owner suite must exist: " + required(entry, "ownerSuite"));
                        Assert.assertTrue(Files.isRegularFile(path(required(entry, "consumerContract"))),
                                "Consumer bridge test contract must exist: " + required(entry, "consumerContract"));
                        Assert.assertTrue(Files.isRegularFile(path(required(entry, "contractResource"))),
                                "Consumer bridge resource must exist: " + required(entry, "contractResource"));
                        Assert.assertEquals(required(entry, "artifactReducer"),
                                "com.ai.skills.business.parse.BusinessUnderstandingArtifactParser",
                                "Consumer bridge must route real artifact reduction through the shared neutral parser.");
                        Assert.assertFalse(required(entry, "expected").isBlank(),
                                "Consumer bridge must explain expected semantic usage.");
                        Assert.assertFalse(required(entry, "repair").isBlank(),
                                "Consumer bridge must provide a repair hint.");
                        Assert.assertNotEquals(required(entry, "consumer"), required(entry, "forbiddenOwner"),
                                "A bridge consumer cannot forbid itself.");
                    }
                });
    }

    @Test
    public void consumerBridgeReferencesOnlySharedModelsConceptsStagesAndInputs() {
        Set<String> modelNames = SimpleYamlContractLoader.load(MODEL_RESOURCE).stream()
                .map(entry -> required(entry, "model"))
                .collect(Collectors.toSet());
        Set<String> conceptNames = enumNames(BusinessUnderstandingConcept.class);
        Set<String> stageNames = enumNames(BusinessUnderstandingStage.class);
        Set<String> inputTypeNames = enumNames(BusinessUnderstandingInputType.class);

        BusinessUnderstandingReportNarrative.verify(
                BRIDGE_RESOURCE,
                "business-understanding.consumer.atlas-business-audit",
                "Model names=" + modelNames
                        + ", concepts=" + conceptNames.size()
                        + ", stages=" + stageNames
                        + ", inputTypes=" + inputTypeNames,
                () -> {
                    for (Map<String, String> entry : SimpleYamlContractLoader.load(BRIDGE_RESOURCE)) {
                        assertCsvSubset(entry, "requiredModels", modelNames);
                        assertCsvSubset(entry, "requiredConcepts", conceptNames);
                        assertCsvSubset(entry, "requiredStages", stageNames);
                        assertCsvSubset(entry, "inputTypes", inputTypeNames);
                    }
                });
    }

    @Test
    public void consumerBridgeContractsAreIncludedInOwningSanityProfiles() {
        BusinessUnderstandingReportNarrative.verify(
                "business-understanding.consumer.bridge-suite-coverage",
                "Every Business Understanding consumer bridge must be present in the owning skill sanity profile.",
                "Checked owner suites for " + SimpleYamlContractLoader.load(BRIDGE_RESOURCE).size()
                        + " semantic consumers.",
                "Add the consumer contract class to its skill sanity TestNG profile before relying on the bridge.",
                () -> {
                    for (Map<String, String> entry : SimpleYamlContractLoader.load(BRIDGE_RESOURCE)) {
                        String suite = read(path(required(entry, "ownerSuite")));
                        String className = javaClassName(required(entry, "consumerContract"));
                        Assert.assertTrue(suite.contains(className),
                                required(entry, "ownerSuite") + " must include " + className);
                    }
                });
    }

    @Test
    public void consumerBridgeKeepsReportingOutOfBusinessMeaning() {
        BusinessUnderstandingReportNarrative.verify(
                "business-understanding.consumer.reporting-boundary",
                "Business Understanding bridge rows must describe business meaning only, not Extent shell, CSS or Spark layout presentation.",
                "Scanned semantic consumer bridge wording for Reporting-specific shell vocabulary.",
                "Move visual/report-shell language to Reporting skill contracts; keep this bridge semantic.",
                () -> {
                    for (Map<String, String> entry : SimpleYamlContractLoader.load(BRIDGE_RESOURCE)) {
                        String combined = String.join(" ", entry.values()).toLowerCase();
                        Assert.assertFalse(combined.contains("extent shell") || combined.contains("css") || combined.contains("spark layout"),
                                "Business Understanding bridge must not become a Reporting shell contract: " + required(entry, "id"));
                    }
                });
    }

    private static void assertCsvSubset(Map<String, String> entry, String key, Set<String> allowedValues) {
        for (String token : required(entry, key).split(",")) {
            String value = token.trim();
            Assert.assertTrue(allowedValues.contains(value),
                    key + " references unknown value `" + value + "` in " + required(entry, "id"));
        }
    }

    private static <E extends Enum<E>> Set<String> enumNames(Class<E> enumType) {
        return Set.of(enumType.getEnumConstants()).stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    private static String javaClassName(String sourcePath) {
        String normalized = sourcePath.replace('\\', '/');
        return normalized.substring(normalized.indexOf("com/"))
                .replace('/', '.')
                .replace(".java", "");
    }

    private static String required(Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, BRIDGE_RESOURCE);
    }

    private static Path path(String relativePath) {
        return AtfSourcePaths.workspaceRoot().resolve(relativePath);
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception exception) {
            throw new AssertionError("Could not read " + path, exception);
        }
    }
}
