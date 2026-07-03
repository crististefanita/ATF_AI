package com.endava.ai.atf.core.skills.architecture;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.skills.atlas.contract.execution.AtlasExecutionProfile;
import com.endava.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertPackageShapeVisible;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertDirectory;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entry;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.path;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.required;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.values;

@Listeners(TestListener.class)
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_MANIFEST)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_MANIFEST_CATALOG)
public class AtfSkillPackageGovernanceContractTest {
    private static final List<String> YAML_CONTRACT_ROOTS = List.of(
            "src/main/resources/atf-core/contracts",
            "src/main/resources/skills/business/contracts",
            "src/main/resources/skills/reporting/contracts",
            "src/main/resources/skills/codequality/contracts",
            "src/main/resources/skills/mcp/contracts",
            "src/main/resources/skills/atlas/contracts"
    );
    private static final List<String> STALE_CORE_SKILL_PACKAGE_TOKENS = List.of(
            "core" + ".skills." + "business",
            "core" + ".skills." + "report",
            "core" + ".skills." + "codequality"
    );
    private static final List<String> STALE_REFERENCE_SCAN_ROOTS = List.of(
            "src/main/java",
            "src/test/java",
            "src/main/resources",
            "src/test/resources"
    );

    @Test
    public void skillArchitectureKeepsPackageShapeGovernanceSymmetric() throws IOException {
        assertPackageShapeVisible(
                "skill.core.reporting",
                "src/main/resources/skills/reporting/contracts/reporting-package-shape.yml",
                "src/main/java/com/endava/ai/core/reporting/README.md"
        );
        assertPackageShapeVisible(
                "skill.mcp",
                "src/main/resources/skills/mcp/contracts/package-shape.yml",
                "src/main/java/com/endava/ai/skills/mcp/contracts/README.md"
        );
        assertPackageShapeVisible(
                "skill.business-understanding",
                "src/main/resources/skills/business/contracts/package-shape.yml",
                "src/main/java/com/endava/ai/skills/business/README.md"
        );
        assertPackageShapeVisible(
                "skill.atlas",
                "src/main/resources/skills/atlas/contracts/package-shape.yml",
                "src/main/java/com/endava/ai/skills/atlas/rules/README.md"
        );
    }

    @Test
    public void skillArchitectureYamlContractRootsHaveLocalIndexes() throws IOException {
        List<String> violations = new ArrayList<>();

        for (String rootPath : YAML_CONTRACT_ROOTS) {
            Path root = path(rootPath);
            Path readme = root.resolve(AtfSourcePaths.README_FILE_NAME);
            Assert.assertTrue(Files.isRegularFile(readme),
                    "YAML contract root must publish a local README index: " + rootPath);
            String index = Files.readString(readme);
            try (Stream<Path> files = Files.walk(root)) {
                files.filter(Files::isRegularFile)
                        .filter(file -> file.getFileName().toString().endsWith(".yml"))
                        .map(root::relativize)
                        .map(AtfSourcePaths::unixPath)
                        .filter(relative -> !index.contains("`" + relative + "`"))
                        .forEach(relative -> violations.add(rootPath + " README does not list `" + relative + "`"));
            }
        }

        Assert.assertTrue(violations.isEmpty(),
                "Every YAML contract must be discoverable in its owner README: " + violations);
    }

    @Test
    public void skillArchitectureRejectsStaleCoreHostedDomainSkillReferences() throws IOException {
        List<String> violations = new ArrayList<>();
        for (String scanRoot : STALE_REFERENCE_SCAN_ROOTS) {
            Path root = path(scanRoot);
            if (Files.isRegularFile(root)) {
                inspectStaleSkillTokens(root, violations);
                continue;
            }
            try (Stream<Path> files = Files.walk(root)) {
                files.filter(Files::isRegularFile)
                        .filter(AtfSkillPackageGovernanceContractTest::isTextSource)
                        .forEach(file -> inspectStaleSkillTokens(file, violations));
            }
        }
        Assert.assertTrue(violations.isEmpty(),
                "Concrete domain skills must stay under com.endava.ai.skills.*; stale Core-hosted references: "
                        + violations);
    }

    @Test
    public void skillArchitectureDocumentsArtifactGitBoundaries() {
        Map<String, String> core = entry("skill.core");
        Map<String, String> mcp = entry("skill.mcp");
        Map<String, String> atlas = entry("skill.atlas");
        Map<String, String> businessUnderstanding = entry("skill.business-understanding");
        Map<String, String> promptAssets = entry("prompt.assets");
        Map<String, String> generatedCloseout = entry("usecase.generated-package-closeout");

        String generatedPackageRoot = "docs" + "/out";
        Assert.assertTrue(core.get("artifactPolicy").contains("must not own " + generatedPackageRoot),
                "Core artifact policy must keep package runs outside Core sanity");
        Assert.assertTrue(mcp.get("artifactPolicy").contains("html-ex promotion gates"),
                "MCP artifact policy must make html-ex promotion deliberate");
        Assert.assertTrue(atlas.get("artifactPolicy").contains("must not decide MCP publishability"),
                "Atlas artifact policy must not become MCP close-out governance");
        Assert.assertTrue(businessUnderstanding.get("artifactPolicy").contains("must not decide MCP package publishability"),
                "Business Understanding must stay semantic and not become the MCP gate");
        Assert.assertTrue(businessUnderstanding.get("artifactPolicy").contains("own Atlas generated artifacts"),
                "Business Understanding must not take over Atlas artifact ownership");
        Assert.assertTrue(promptAssets.get("artifactPolicy").contains("stay synchronized"),
                "Prompt artifact policy must protect html_ro/html_EN mirrors");
        Assert.assertTrue(generatedCloseout.get("artifactPolicy").contains("html-ex receives only curated replacements"),
                "Generated package closeout must not silently promote generated package runs into html-ex");
    }

    @Test
    public void skillArchitectureSuitesStayAlignedWithTypedExecutionProfiles() {
        Map<String, String> mcp = entry("skill.mcp");
        Assert.assertEquals(required(mcp, "sanitySuite"), suite(McpExecutionProfile.SANITY),
                "MCP skill sanity suite must use the typed MCP execution profile.");
        Assert.assertEquals(required(mcp, "deepSuite"), suite(McpExecutionProfile.PACKAGE_CLOSEOUT),
                "MCP skill deep suite must use the typed MCP package close-out profile.");

        Map<String, String> atlas = entry("skill.atlas");
        Assert.assertEquals(required(atlas, "sanitySuite"), suite(AtlasExecutionProfile.SANITY),
                "Atlas skill sanity suite must use the typed Atlas execution profile.");
        Assert.assertEquals(required(atlas, "deepSuite"), suite(AtlasExecutionProfile.FULL),
                "Atlas skill deep suite must use the typed Atlas full execution profile.");

        Map<String, String> businessUnderstanding = entry("skill.business-understanding");
        Assert.assertEquals(required(businessUnderstanding, "sanitySuite"),
                "src/test/resources/skills/business/testng-business-understanding-sanity.xml",
                "Business Understanding skill must expose its own isolated sanity profile.");
        Assert.assertTrue(values(entry("skill.mcp"), "consumes").contains("skill.business-understanding"),
                "MCP must consume shared business-understanding semantics instead of duplicating them.");
        Assert.assertTrue(values(entry("skill.atlas"), "consumes").contains("skill.business-understanding"),
                "Atlas must consume shared business-understanding semantics instead of forking them.");

        Map<String, String> promptContracts = entry("prompt.assets");
        Assert.assertEquals(required(promptContracts, "sanitySuite"), suite(McpExecutionProfile.SANITY),
                "Prompt asset sanity must stay on the MCP sanity profile.");
        Assert.assertEquals(required(promptContracts, "deepSuite"), suite(McpExecutionProfile.SELF),
                "Prompt asset deep validation must stay on the MCP self profile.");
        Assert.assertEquals(required(promptContracts, "kind"), "prompt-assets",
                "Prompt files are contract assets with explicit owners, not a skill surface.");
        Assert.assertTrue(required(promptContracts, "role").contains("not an independent skill"),
                "Prompt assets must explicitly reject the standalone prompt-skill model.");
    }

    @Test
    public void reportingSkillIsFacadeOverCoreReportingImplementation() throws IOException {
        Map<String, String> reporting = entry("skill.reporting");

        Assert.assertEquals(required(reporting, "sourceRoot"), "src/main/java/com/endava/ai/skills/reporting",
                "Reporting skill surface must remain a small facade package.");
        Assert.assertEquals(required(reporting, "implementationRoot"), "src/main/java/com/endava/ai/core/reporting",
                "Reporting implementation must remain in the reusable core.reporting library.");
        assertDirectory(reporting, "implementationRoot");
        Assert.assertEquals(required(reporting, "facadeTestRoot"), "src/test/java/com/endava/ai/skills/reporting",
                "Reporting facade smoke tests must remain visible beside the promoted skill package.");
        assertDirectory(reporting, "facadeTestRoot");
        Assert.assertTrue(required(reporting, "role").contains("facade"),
                "Reporting skill role must state that skills/reporting is a facade.");
        Assert.assertTrue(values(reporting, "consumes").contains("skill.business-understanding"),
                "Reporting may consume Business Understanding as neutral BusinessGraphConstruction layout input.");
        Assert.assertTrue(Files.readString(path(required(reporting, "readme")))
                        .contains("facade over com.endava.ai.core.reporting"),
                "Reporting README must make the facade-over-implementation split explicit.");
        Assert.assertFalse(values(reporting, "consumes").contains("skill.mcp")
                        || values(reporting, "consumes").contains("skill.atlas"),
                "Reporting is shared infrastructure; it must not consume MCP or Atlas.");
    }

    private static String suite(McpExecutionProfile profile) {
        return profile.suiteXml();
    }

    private static String suite(AtlasExecutionProfile profile) {
        return profile.suiteXml();
    }

    private static boolean isTextSource(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".java") || fileName.endsWith(".yml") || fileName.endsWith(".xml")
                || fileName.endsWith(".md");
    }

    private static void inspectStaleSkillTokens(Path path, List<String> violations) {
        try {
            String source = Files.readString(path);
            for (String token : STALE_CORE_SKILL_PACKAGE_TOKENS) {
                if (source.contains(token)) {
                    violations.add(AtfSourcePaths.projectRelative(path) + " contains " + token);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect stale skill references in " + path, exception);
        }
    }
}
