package com.endava.ai.atf.core.support.architecture;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Listeners(TestListener.class)
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_MANIFEST)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_MANIFEST_CATALOG)
public class CoreSupportKernelArchitectureContractTest {
    private static final Path CORE_SUPPORT_ROOT = AtfSourcePaths.MAIN_CORE_SUPPORT_ROOT;
    private static final String CORE_SUPPORT_ROOT_TEXT = AtfSourcePaths.unixPath(CORE_SUPPORT_ROOT);
    private static final String SHARED_YAML_PACKAGE = "com.endava.ai.core.support.contract.yaml";
    private static final List<String> SHARED_YAML_TYPES = List.of(
            "ContractIndex",
            "ContractResource",
            "SimpleYamlContractLoader",
            "YamlEnums",
            "YamlScalarLists",
            "YamlScalars"
    );

    @Test
    public void coreSupportKernelStaysDomainNeutralAndDocumented() throws IOException {
        Assert.assertTrue(Files.isDirectory(CORE_SUPPORT_ROOT), "Missing Core support kernel: " + CORE_SUPPORT_ROOT);

        for (String packageInfo : List.of(
                CORE_SUPPORT_ROOT_TEXT + "/package-info.java",
                CORE_SUPPORT_ROOT_TEXT + "/contract/package-info.java",
                CORE_SUPPORT_ROOT_TEXT + "/contract/yaml/package-info.java",
                CORE_SUPPORT_ROOT_TEXT + "/html/package-info.java"
        )) {
            Assert.assertTrue(Files.isRegularFile(path(packageInfo)),
                    "Core support subpackages must explain ownership in package-info.java: " + packageInfo);
        }

        List<String> violations = new ArrayList<>();
        for (Path source : javaSourcesUnder(CORE_SUPPORT_ROOT)) {
            assertSupportImportsStayNeutral(source, violations);
        }

        Assert.assertTrue(violations.isEmpty(),
                "core.support is the shared kernel; keep it free of MCP/Atlas/TestNG and avoid new reporting coupling: "
                        + violations);
    }

    @Test
    public void yamlContractSupportHasOneSharedImplementation() throws IOException {
        List<String> violations = new ArrayList<>();
        Path mainRoot = AtfSourcePaths.MAIN_JAVA_ROOT;

        for (String type : SHARED_YAML_TYPES) {
            Path expected = CORE_SUPPORT_ROOT.resolve("contract/yaml/" + type + AtfSourcePaths.JAVA_EXTENSION);
            Assert.assertTrue(Files.isRegularFile(expected),
                    type + " must live in the shared Core support YAML package.");

            List<Path> declarations = javaSourcesUnder(mainRoot).stream()
                    .filter(source -> source.getFileName().toString().equals(type + ".java"))
                    .collect(Collectors.toList());
            Assert.assertEquals(declarations, List.of(expected),
                    type + " must not be duplicated in MCP, Atlas or old root packages.");
        }

        for (Path source : javaSourcesUnder(mainRoot)) {
            assertNoRemovedYamlPackageOrLocalLoader(source, violations);
        }

        Assert.assertTrue(violations.isEmpty(),
                "YAML contract parsing must be centralized under " + SHARED_YAML_PACKAGE + ": " + violations);
    }

    @Test
    public void mcpAtlasAndCoreYamlSpecsConsumeSharedYamlSupport() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path root : List.of(
                AtfSourcePaths.MAIN_MCP_ROOT,
                AtfSourcePaths.MAIN_ATLAS_ROOT,
                CORE_SUPPORT_ROOT.resolve("contract/core/yaml"),
                AtfSourcePaths.MAIN_CORE_REPORTING_ROOT.resolve("htmlcontract")
        )) {
            for (Path source : javaSourcesUnder(root)) {
                assertSharedYamlImportsUseCoreSupport(source, offenders);
            }
        }

        Assert.assertTrue(offenders.isEmpty(),
                "All YAML-backed specs must consume the shared Core support YAML API: " + offenders);
    }

    private static void assertSupportImportsStayNeutral(Path source, List<String> violations) throws IOException {
        String relative = relative(source);
        for (String line : Files.readAllLines(source)) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("import ")) {
                continue;
            }
            if (trimmed.startsWith("import com.endava.ai.skills.mcp.")
                    || trimmed.startsWith("import com.endava.ai.skills.atlas.")
                    || trimmed.startsWith("import org.testng.")) {
                violations.add(relative + " imports domain/test-only API: " + trimmed);
            }
            if (trimmed.startsWith("import com.endava.ai.core.reporting.")
                    && !relative.endsWith("AtfContractSteps.java")
                    && !relative.endsWith("AtfContractReportLabels.java")) {
                violations.add(relative + " imports reporting outside the explicit contract-step bridge: "
                        + trimmed);
            }
        }
    }

    private static void assertNoRemovedYamlPackageOrLocalLoader(Path source, List<String> violations)
            throws IOException {
        String text = Files.readString(source);
        String relative = relative(source);
        if (text.contains("package com.endava.ai.contracts.yaml")) {
            violations.add(relative + " still declares the removed root YAML package");
        }
        if (text.contains("com.endava.ai.contracts.yaml")) {
            violations.add(relative + " still imports the removed root YAML package");
        }
        if (!relative.startsWith(CORE_SUPPORT_ROOT_TEXT) && text.contains("class SimpleYamlContractLoader")) {
            violations.add(relative + " declares a local YAML loader instead of using "
                    + SHARED_YAML_PACKAGE + ".SimpleYamlContractLoader");
        }
    }

    private static void assertSharedYamlImportsUseCoreSupport(Path source, List<String> offenders)
            throws IOException {
        String relative = relative(source);
        for (String line : Files.readAllLines(source)) {
            String trimmed = line.trim();
            if (!isSharedYamlSupportImport(trimmed)) {
                continue;
            }
            if (!trimmed.contains(SHARED_YAML_PACKAGE)) {
                offenders.add(relative + " imports YAML support outside " + SHARED_YAML_PACKAGE + ": " + trimmed);
            }
        }
    }

    private static boolean isSharedYamlSupportImport(String line) {
        return line.startsWith("import ")
                && SHARED_YAML_TYPES.stream().anyMatch(type -> line.contains("." + type));
    }

    private static List<Path> javaSourcesUnder(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(AtfSourcePaths::isJavaSource)
                    .collect(Collectors.toList());
        }
    }

    private static String relative(Path source) {
        Path workspaceRoot = AtfSourcePaths.workspaceRoot().toAbsolutePath().normalize();
        Path absoluteSource = source.toAbsolutePath().normalize();
        return AtfSourcePaths.unixPath(workspaceRoot.relativize(absoluteSource));
    }

    private static Path path(String relativePath) {
        return AtfSourcePaths.workspaceRoot().resolve(relativePath);
    }
}
