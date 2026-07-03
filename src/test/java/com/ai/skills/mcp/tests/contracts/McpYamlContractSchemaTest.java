package com.ai.skills.mcp.tests.contracts;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.candidate.McpHtmlExCorpusSpec;
import com.ai.skills.mcp.tests.contracts.support.McpYamlPolicySchemaAssertions;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.*;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-yaml-contract-schema-test")
public class McpYamlContractSchemaTest {
    private static final List<Path> YAML_SPEC_ROOTS = List.of(
            AtfSourcePaths.MAIN_MCP_ROOT.resolve("contracts"),
            AtfSourcePaths.MAIN_CORE_ROOT.resolve("support/contract/core/yaml"),
            AtfSourcePaths.MAIN_ATLAS_ROOT.resolve("contract/yaml"));

    @Test
    @McpCheckRef("mcp.yaml-contract.schema-version")
    public void yamlContractResourcesDeclareSupportedSchemaVersion() throws IOException {
        try (Stream<Path> files = Files.walk(CONTRACT_RESOURCE_ROOT)) {
            List<Path> yamlFiles = files
                    .filter(path -> path.getFileName().toString().endsWith(".yml"))
                    .collect(Collectors.toUnmodifiableList());

            Assert.assertFalse(yamlFiles.isEmpty(), "Expected YAML contract resources.");
            for (Path yaml : yamlFiles) {
                String resourcePath = CONTRACT_RESOURCE_ROOT.relativize(yaml)
                        .toString()
                        .replace('\\', '/');
                SimpleYamlContractLoader.load(resourcePath);
            }
        }
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.duplicate-key-detection")
    public void yamlContractLoaderRejectsDuplicateKeysExplicitly() {
        String duplicateKeyYaml = ""
                + "schemaVersion: 1\n"
                + "contracts:\n"
                + "  - id: duplicate_key_fixture\n"
                + "    expected: first value\n"
                + "    expected: second value\n"
                + "    artifacts: target/fixture.md\n"
                + "    failureMeaning: duplicate keys must not be accepted\n"
                + "    repairHint: remove the duplicate key\n";

        IllegalArgumentException error = Assert.expectThrows(IllegalArgumentException.class,
                () -> SimpleYamlContractLoader.loadText(duplicateKeyYaml, "inline/duplicate-key.yml"));
        Assert.assertTrue(error.getMessage().contains("Duplicate YAML key")
                        && error.getMessage().contains("inline/duplicate-key.yml"),
                "Duplicate key rejection should identify the YAML resource. Actual: " + error.getMessage());
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.scalar-parser-guardrail")
    public void yamlContractSpecsUseStrictScalarParsers() {
        List<String> offenders = yamlSpecOffenders(McpYamlContractSchemaTest::containsRawScalarParser);

        Assert.assertTrue(offenders.isEmpty(),
                "YAML contract specs must use YamlScalars for boolean/integer parsing: " + offenders);
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.scalar-parser-guardrail")
    public void yamlContractSpecsUseSharedContractIdHelper() {
        List<String> offenders = yamlSpecOffenders(McpYamlContractSchemaTest::containsLocalContractIdFallback);

        Assert.assertTrue(offenders.isEmpty(),
                "YAML contract specs must use YamlScalars.contractId instead of local id fallbacks: " + offenders);
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.kpi-schema.required-fields")
    public void sourceEvolutionYamlKpisHaveCompleteSchema() {
        McpYamlPolicySchemaAssertions.assertSourceEvolutionKpisHaveCompleteSchema();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.kpi-schema.identity")
    public void sourceEvolutionYamlKpisHaveStableIdentity() {
        McpYamlPolicySchemaAssertions.assertSourceEvolutionKpisHaveStableIdentity();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.profile-schema")
    public void mcpProfileYamlDeclaresPrimaryExecutionStages() {
        McpYamlPolicySchemaAssertions.assertMcpProfilesDeclarePrimaryExecutionStages();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.agentic-execution-flow")
    public void agenticExecutionFlowSeparatesCalibrationCloseoutAndPromotionStages() {
        McpYamlPolicySchemaAssertions.assertAgenticExecutionFlowSeparatesCalibrationCloseoutAndPromotionStages();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.atlas-quality-bridge")
    public void atlasQualityBridgeKeepsMcpFinalReportCalibratedWithoutBecomingAtlasGate() {
        McpYamlPolicySchemaAssertions.assertAtlasQualityBridgeKeepsMcpCalibratedWithoutBecomingAtlasGate();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.closeout-artifact-schema")
    public void packageCloseoutArtifactYamlDeclaresFullMcpSpine() {
        McpYamlPolicySchemaAssertions.assertPackageCloseoutArtifactSpine();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.policy-catalog-schema")
    public void mcpPolicyYamlFilesHaveStableMinimumSchema() {
        McpYamlPolicySchemaAssertions.assertStableMinimumSchema();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.claim-action-schema")
    public void claimActionYamlDeclaresCalibratableVerbContracts() {
        McpYamlPolicySchemaAssertions.assertClaimActionContracts();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.catalog-facade.no-public-map")
    public void contractCatalogPublicApiDoesNotExposeYamlMaps() {
        List<String> offenders = Arrays.stream(ContractCatalog.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getGenericReturnType)
                .map(java.lang.reflect.Type::getTypeName)
                .filter(type -> type.contains("java.util.Map"))
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(offenders.isEmpty(),
                "ContractCatalog public API must expose typed specs, not YAML maps: " + offenders);
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.policy-catalog-schema")
    public void packagePolicyYamlKeepsExecutableArtifactConstants() {
        McpYamlPolicySchemaAssertions.assertPackagePolicyArtifactConstants();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.report-template-artifact-spine")
    public void reportTemplateYamlPackageSpineMatchesArtifactPathConstants() {
        McpYamlPolicySchemaAssertions.assertReportTemplatePackageSpine();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.report-template-schema")
    public void reportTemplateYamlDeclaresCanonicalReportSurfaces() {
        McpYamlPolicySchemaAssertions.assertReportTemplateCanonicalSurfaces();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.extent-provenance-schema")
    public void extentProvenanceYamlDeclaresMavenExtentEvidenceBoundaries() {
        McpYamlPolicySchemaAssertions.assertExtentProvenanceBoundaries();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.extent-provenance-sync")
    public void extentProvenanceYamlKeepsKnownAntiSurgeryMarkers() {
        McpYamlPolicySchemaAssertions.assertExtentProvenanceAntiSurgeryMarkers();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.kpi-schema.negative-metadata")
    public void yamlContractSchemaHelpersRejectBadMetadata() {
        McpYamlPolicySchemaAssertions.assertSchemaHelpersRejectBadMetadata();
    }

    private static List<String> yamlSpecOffenders(Predicate<Path> predicate) {
        return YAML_SPEC_ROOTS.stream()
                .flatMap(McpYamlContractSchemaTest::javaFiles)
                .filter(predicate)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    private static Stream<Path> javaFiles(Path root) {
        try {
            return Files.walk(root)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .collect(Collectors.toUnmodifiableList())
                    .stream();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan YAML contract specs under " + root, e);
        }
    }

    private static boolean containsRawScalarParser(Path path) {
        return sourceContainsAny(path, "Boolean.parseBoolean", "Integer.parseInt", "Long.parseLong", "Double.parseDouble");
    }

    private static boolean containsLocalContractIdFallback(Path path) {
        return sourceContainsAny(path, "getOrDefault(\"id\", \"<unknown>\")");
    }

    private static boolean sourceContainsAny(Path path, String... tokens) {
        try {
            String source = Files.readString(path);
            return Arrays.stream(tokens).anyMatch(source::contains);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect YAML contract spec " + path, e);
        }
    }

}
