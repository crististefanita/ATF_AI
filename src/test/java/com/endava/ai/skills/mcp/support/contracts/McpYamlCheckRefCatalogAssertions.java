package com.endava.ai.skills.mcp.support.contracts;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.checks.McpCheckSpec;
import com.endava.ai.skills.mcp.contracts.suites.McpSuiteSpec;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class McpYamlCheckRefCatalogAssertions {
    private McpYamlCheckRefCatalogAssertions() {
    }

    public static void assertCheckRefsHaveCompleteSchema() {
        List<McpCheckSpec> specs = ContractCatalog.mcpChecks();

        Assert.assertFalse(specs.isEmpty(), "MCP YAML check catalog must not be empty.");
        Assert.assertTrue(duplicateIds(specs).isEmpty(), "Duplicate MCP YAML check ids: " + duplicateIds(specs));
        for (McpCheckSpec spec : specs) {
            Assert.assertFalse(spec.id().isBlank(), "id must be present");
            Assert.assertFalse(spec.expected().isBlank(), "expected must be present for " + spec.id());
            Assert.assertFalse(spec.artifacts().isEmpty(), "artifacts must be present for " + spec.id());
            Assert.assertFalse(spec.failureMeaning().isBlank(), "failureMeaning must be present for " + spec.id());
            Assert.assertFalse(spec.repairHint().isBlank(), "repairHint must be present for " + spec.id());
        }
    }

    public static void assertEveryMcpCheckYamlResourceIsLoadedByCatalog() throws IOException {
        Set<String> registered = Arrays.stream(ContractCatalog.MCP_CHECKS_RESOURCES)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> physical;
        try (Stream<java.nio.file.Path> files = Files.list(AtfSourcePaths.workspaceRoot()
                .resolve("src/main/resources/skills/mcp/contracts/checks"))) {
            physical = files
                    .filter(path -> path.getFileName().toString().startsWith("mcp-checks-"))
                    .filter(path -> path.getFileName().toString().endsWith(".yml"))
                    .map(path -> AtfSourcePaths.unixPath(
                            AtfSourcePaths.workspaceRoot().resolve("src/main/resources").relativize(path)))
                    .collect(Collectors.toUnmodifiableSet());
        }

        Assert.assertEquals(registered, physical,
                "Every physical mcp-checks-*.yml file must be listed in McpContractResources.MCP_CHECKS.");
    }

    public static void assertEveryJavaMcpCheckRefResolvesToYamlMetadata() throws IOException {
        List<String> referencedIds = McpYamlCheckRefScanner.referencedCheckIds();
        List<String> yamlIds = ContractCatalog.mcpChecks().stream()
                .map(McpCheckSpec::id)
                .collect(Collectors.toUnmodifiableList());

        List<String> missingYaml = referencedIds.stream()
                .filter(id -> !yamlIds.contains(id))
                .collect(Collectors.toUnmodifiableList());
        List<String> orphanYaml = yamlIds.stream()
                .filter(id -> !referencedIds.contains(id))
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(referencedIds.size() >= 150,
                "Expected MCP tests to use YAML-backed @McpCheckRef metadata broadly.");
        Assert.assertTrue(missingYaml.isEmpty(), "Missing YAML check metadata for refs: " + missingYaml);
        Assert.assertTrue(orphanYaml.isEmpty(), "YAML check entries without @McpCheckRef usage: " + orphanYaml);
    }

    public static void assertMcpTestsDoNotUseInlineMcpCheckNarratives() throws IOException {
        List<String> inlineChecks = McpYamlCheckRefScanner.inlineMcpCheckUsages();

        Assert.assertTrue(inlineChecks.isEmpty(),
                "MCP test narrative metadata must live in mcp-checks-*.yml via @McpCheckRef, not inline @McpCheck: "
                        + inlineChecks);
    }

    public static void assertSuiteRefsHaveCompleteSchema() {
        List<McpSuiteSpec> specs = ContractCatalog.mcpSuites();

        Assert.assertFalse(specs.isEmpty(), "MCP YAML suite catalog must not be empty.");
        Assert.assertTrue(duplicateSuiteIds(specs).isEmpty(),
                "Duplicate MCP YAML suite ids: " + duplicateSuiteIds(specs));
        for (McpSuiteSpec spec : specs) {
            Assert.assertFalse(spec.id().isBlank(), "id must be present");
            Assert.assertFalse(spec.className().isBlank(), "className must be present for " + spec.id());
            Assert.assertNotNull(spec.profile(), "profile must be present for " + spec.id());
            Assert.assertNotNull(spec.category(), "category must be present for " + spec.id());
            Assert.assertFalse(spec.title().isBlank(), "title must be present for " + spec.id());
            Assert.assertFalse(spec.htmlReq().isBlank(), "htmlReq must be present for " + spec.id());
            Assert.assertFalse(spec.purpose().isBlank(), "purpose must be present for " + spec.id());
        }
    }

    public static void assertEveryJavaMcpSuiteRefResolvesToYamlMetadata() throws IOException {
        List<String> referencedIds = McpYamlCheckRefScanner.referencedSuiteIds();
        List<String> yamlIds = ContractCatalog.mcpSuites().stream()
                .map(McpSuiteSpec::id)
                .collect(Collectors.toUnmodifiableList());

        List<String> missingYaml = referencedIds.stream()
                .filter(id -> !yamlIds.contains(id))
                .collect(Collectors.toUnmodifiableList());
        List<String> orphanYaml = yamlIds.stream()
                .filter(id -> !referencedIds.contains(id))
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(referencedIds.size() >= 35,
                "Expected MCP suite classes to use YAML-backed @McpSuiteRef metadata broadly.");
        Assert.assertTrue(missingYaml.isEmpty(), "Missing YAML suite metadata for refs: " + missingYaml);
        Assert.assertTrue(orphanYaml.isEmpty(), "YAML suite entries without @McpSuiteRef usage: " + orphanYaml);
    }

    public static void assertMcpTestsDoNotUseInlineMcpSuiteNarratives() throws IOException {
        List<String> inlineSuites = McpYamlCheckRefScanner.inlineMcpSuiteUsages();

        Assert.assertTrue(inlineSuites.isEmpty(),
                "MCP suite metadata must live in mcp-suites.yml via @McpSuiteRef, not inline @McpSuite: "
                        + inlineSuites);
    }

    public static void assertMcpTestNgProfileClassesHaveYamlSuiteMetadata() {
        Map<String, McpSuiteSpec> suitesByClass = ContractCatalog.mcpSuites().stream()
                .collect(Collectors.toUnmodifiableMap(McpSuiteSpec::className, spec -> spec));
        List<String> profileClasses = ContractCatalog.testNgProfiles().stream()
                .flatMap(spec -> McpYamlCheckRefScanner.testNgClasses(spec.suiteXml()).stream())
                .distinct()
                .sorted()
                .collect(Collectors.toUnmodifiableList());

        List<String> missingYaml = profileClasses.stream()
                .filter(className -> !suitesByClass.containsKey(className))
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(missingYaml.isEmpty(),
                "MCP TestNG profile classes must have YAML suite metadata: " + missingYaml);
    }

    public static void assertYamlSuiteClassNamesPointBackToMatchingSuiteRefs() {
        List<String> broken = ContractCatalog.mcpSuites().stream()
                .filter(spec -> !McpYamlCheckRefScanner.sourceDeclaresSuiteRef(spec))
                .map(spec -> spec.className() + " -> " + spec.id())
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(broken.isEmpty(),
                "mcp-suites.yml className entries must point to Java classes with the matching @McpSuiteRef: "
                        + broken);
    }

    private static List<String> duplicateIds(List<McpCheckSpec> specs) {
        Map<String, Long> counts = specs.stream()
                .collect(Collectors.groupingBy(McpCheckSpec::id, LinkedHashMap::new, Collectors.counting()));
        return counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<String> duplicateSuiteIds(List<McpSuiteSpec> specs) {
        Map<String, Long> counts = specs.stream()
                .collect(Collectors.groupingBy(McpSuiteSpec::id, LinkedHashMap::new, Collectors.counting()));
        return counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableList());
    }
}
