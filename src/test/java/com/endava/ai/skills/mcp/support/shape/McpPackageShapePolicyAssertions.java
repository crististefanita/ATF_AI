package com.endava.ai.skills.mcp.support.shape;

import com.endava.ai.atf.core.skills.architecture.support.PromptMirrorRootAssertions;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.policy.McpSizeBudgetSpec;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class McpPackageShapePolicyAssertions {
    private static final String PACKAGE_SHAPE_RESOURCE = ContractCatalog.PACKAGE_SHAPE_RESOURCE;
    private static final List<String> REQUIRED_BUDGET_TYPES = List.of(
            "package-default",
            "package-override",
            "recursive-package-cap",
            "line-cap",
            "resource-max-bytes"
    );
    private static final Set<String> ALLOWED_POLICY_KINDS = Set.of(
            "budget-source",
            "required-budget-type",
            "required-readme",
            "skill-surface",
            "forbidden-import",
            "console-gateway"
    );

    private McpPackageShapePolicyAssertions() {
    }

    public static void assertManifestRoutesOwnershipToSizeBudgets() {
        List<Map<String, String>> entries = SimpleYamlContractLoader.load(PACKAGE_SHAPE_RESOURCE);
        Assert.assertFalse(entries.isEmpty(), "skills/mcp/contracts/package-shape.yml must not be empty.");
        Assert.assertEquals(
                entries.stream().map(entry -> required(entry, "id")).collect(Collectors.toSet()).size(),
                entries.size(),
                "MCP package-shape policy ids must be unique."
        );

        Map<String, String> source = entries.stream()
                .filter(entry -> "budget-source".equals(required(entry, "kind")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("MCP package-shape must declare the size budget source."));
        Assert.assertEquals(required(source, "path"), "src/main/resources/skills/mcp/contracts/size-budgets.yml",
                "MCP package-shape must not duplicate numeric caps outside size-budgets.yml.");
        Assert.assertTrue(Files.isRegularFile(projectPath(required(source, "path"))),
                "MCP package-shape budget source must exist.");

        List<String> declaredBudgetTypes = ContractCatalog.sizeBudgetContracts().stream()
                .map(McpSizeBudgetSpec::type)
                .distinct()
                .collect(Collectors.toList());
        for (String budgetType : REQUIRED_BUDGET_TYPES) {
            Assert.assertTrue(declaredBudgetTypes.contains(budgetType),
                    "MCP size-budgets.yml must declare budget type `" + budgetType + "`.");
            Assert.assertTrue(entries.stream().anyMatch(entry -> budgetType.equals(entry.get("budgetType"))),
                    "MCP package-shape.yml must explain required budget type `" + budgetType + "`.");
        }

        for (Map<String, String> entry : entries) {
            assertPackageShapePolicyEntry(entry);
        }
    }

    public static void assertPackagesStayUnderClassLimit(Path root) throws IOException {
        int defaultCap = defaultPackageCap(root);
        List<String> overloadedPackages;
        try (Stream<Path> directories = Files.walk(root)) {
            overloadedPackages = directories
                    .filter(Files::isDirectory)
                    .map(McpPackageShapePolicyAssertions::packageClassCount)
                    .filter(count -> count.getValue() > packageCap(Path.of(count.getKey()), defaultCap))
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(count -> count.getKey() + " has " + count.getValue()
                            + " classes, cap " + packageCap(Path.of(count.getKey()), defaultCap))
                    .collect(Collectors.toList());
        }

        Assert.assertTrue(overloadedPackages.isEmpty(),
                "MCP packages must stay below the readability limit: " + overloadedPackages);
    }

    public static void assertSkillSurfacesPublishOwnerRootReadmeAndSanityRoute() {
        List<Map<String, String>> surfaces = SimpleYamlContractLoader.load(PACKAGE_SHAPE_RESOURCE).stream()
                .filter(entry -> "skill-surface".equals(required(entry, "kind")))
                .collect(Collectors.toList());

        Assert.assertFalse(surfaces.isEmpty(),
                "MCP package-shape must declare skill-surface entries for contracts, rules, validation and prompts.");
        for (String suffix : List.of(".contracts", ".rules", ".validation", ".prompts")) {
            Assert.assertTrue(surfaces.stream().anyMatch(entry -> required(entry, "id").endsWith(suffix)),
                    "MCP skill surfaces must include the `" + suffix + "` surface.");
        }
    }

    private static void assertPackageShapePolicyEntry(Map<String, String> entry) {
        Assert.assertTrue(ALLOWED_POLICY_KINDS.contains(required(entry, "kind")),
                "Unsupported MCP package-shape policy kind: " + required(entry, "kind"));
        Assert.assertFalse(required(entry, "reason").isBlank(),
                "Every MCP package-shape entry must explain why it exists: " + required(entry, "id"));
        if ("required-readme".equals(required(entry, "kind"))) {
            Assert.assertTrue(Files.isRegularFile(projectPath(required(entry, "path"))),
                    "MCP package-shape README path must exist for " + required(entry, "id"));
            assertPromptMirrorRootsIfDeclared(entry, required(entry, "path"));
        }
        if ("skill-surface".equals(required(entry, "kind"))) {
            assertSkillSurfaceShape(entry);
        }
        if ("forbidden-import".equals(required(entry, "kind")) || "console-gateway".equals(required(entry, "kind"))) {
            Assert.assertTrue(Files.isDirectory(projectPath(required(entry, "root"))),
                    "MCP package-shape root must exist for " + required(entry, "id"));
        }
    }

    private static void assertSkillSurfaceShape(Map<String, String> entry) {
        String id = required(entry, "id");
        Assert.assertTrue(Files.isDirectory(projectPath(required(entry, "root"))),
                "MCP skill surface root must exist for " + id);
        Assert.assertTrue(Files.isRegularFile(projectPath(required(entry, "readme"))),
                "MCP skill surface README must exist for " + id);
        Assert.assertTrue(Files.isRegularFile(projectPath(required(entry, "sanitySuite"))),
                "MCP skill surface sanity suite must exist for " + id);
        assertPromptMirrorRootsIfDeclared(entry, required(entry, "readme"));
        Assert.assertFalse(required(entry, "role").isBlank(),
                "MCP skill surface must explain its role: " + id);
        String readme = readUnchecked(projectPath(required(entry, "readme")));
        Assert.assertTrue(readme.contains("## Purpose"),
                "MCP skill surface README must expose Purpose for " + id);
        Assert.assertTrue(readme.contains("## Entry points") || readme.contains("## Entry Points"),
                "MCP skill surface README must expose Entry points for " + id);
        Assert.assertTrue(readme.contains("## Verification"),
                "MCP skill surface README must expose Verification for " + id);
    }

    private static int defaultPackageCap(Path root) {
        String normalizedRoot = normalize(root);
        return ContractCatalog.sizeBudgetContracts().stream()
                .filter(entry -> "package-default".equals(entry.type()))
                .filter(entry -> normalizedRoot.equals(normalize(Path.of(entry.root()))))
                .map(McpSizeBudgetSpec::maxClasses)
                .findFirst()
                .orElse(8);
    }

    private static int packageCap(Path directory, int defaultCap) {
        String normalizedDirectory = normalize(directory);
        return ContractCatalog.sizeBudgetContracts().stream()
                .filter(entry -> "package-override".equals(entry.type()))
                .filter(entry -> normalizedDirectory.equals(normalize(Path.of(entry.path()))))
                .map(McpSizeBudgetSpec::maxClasses)
                .findFirst()
                .orElse(defaultCap);
    }

    private static Map.Entry<String, Long> packageClassCount(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            long classes = files.filter(McpPackageShapePolicyAssertions::isJavaSource).count();
            return Map.entry(directory.toString(), classes);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect package " + directory, e);
        }
    }

    private static String required(Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, PACKAGE_SHAPE_RESOURCE);
    }

    private static void assertPromptMirrorRootsIfDeclared(Map<String, String> entry, String readmePath) {
        PromptMirrorRootAssertions.assertDeclaredRootsAreIndexed(
                entry, AtfSourcePaths.workspaceRoot(), readmePath, "MCP");
    }

    private static Path projectPath(String relativePath) {
        return AtfSourcePaths.workspaceRoot().resolve(relativePath);
    }

    static boolean isJavaSource(Path path) {
        return path.getFileName().toString().endsWith(AtfSourcePaths.JAVA_EXTENSION);
    }

    private static String readUnchecked(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect MCP source " + path, e);
        }
    }

    private static String normalize(Path path) {
        return AtfSourcePaths.unixPath(path.normalize());
    }
}
