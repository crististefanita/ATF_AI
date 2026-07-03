package com.ai.skills.mcp.tests.core.shape;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.skills.mcp.tests.support.McpRunNegativeSupport;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

import static com.ai.skills.mcp.support.shape.McpPackageShapeBudgetAssertions.assertLineCapBudgets;
import static com.ai.skills.mcp.support.shape.McpPackageShapeBudgetAssertions.assertRecursiveSurfaceBudgets;
import static com.ai.skills.mcp.support.shape.McpPackageShapeBudgetAssertions.assertYamlByteBudgets;
import static com.ai.skills.mcp.support.shape.McpPackageShapePolicyAssertions.assertManifestRoutesOwnershipToSizeBudgets;
import static com.ai.skills.mcp.support.shape.McpPackageShapePolicyAssertions.assertPackagesStayUnderClassLimit;
import static com.ai.skills.mcp.support.shape.McpPackageShapePolicyAssertions.assertSkillSurfacesPublishOwnerRootReadmeAndSanityRoute;
import static com.ai.skills.mcp.support.shape.McpPackageShapeSourceAssertions.assertConsoleOutputUsesGateway;
import static com.ai.skills.mcp.support.shape.McpPackageShapeSourceAssertions.assertLombokUsageStaysDisciplined;
import static com.ai.skills.mcp.support.shape.McpPackageShapeSourceAssertions.assertMajorPackagesExposeCreditEfficientReadmes;
import static com.ai.skills.mcp.support.shape.McpPackageShapeSourceAssertions.assertNoDirectImports;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-package-shape-contract-test")
public class McpPackageShapeContractTest {
    private static final Path MAIN_MCP = AtfSourcePaths.MAIN_MCP_ROOT;
    private static final Path TEST_MCP = AtfSourcePaths.TEST_MCP_TESTS_ROOT;
    private static final Path TEST_MCP_ROOT = AtfSourcePaths.TEST_MCP_ROOT;
    private static final String ATLAS_PACKAGE = "com.ai.skills.atlas";

    @Test
    public void mcpPackageShapeManifestRoutesOwnershipToSizeBudgets() {
        assertManifestRoutesOwnershipToSizeBudgets();
    }

    @Test
    @McpCheckRef("mcp.package-shape.main.max-ten-classes")
    public void mainMcpPackagesStayUnderClassLimit() throws IOException {
        assertPackagesStayUnderClassLimit(MAIN_MCP);
    }

    @Test
    @McpCheckRef("mcp.package-shape.tests.max-eight-classes")
    public void mcpTestPackagesStayUnderClassLimit() throws IOException {
        assertPackagesStayUnderClassLimit(TEST_MCP);
    }

    @Test
    @McpCheckRef("mcp.package-shape.large-class-ratchet")
    public void knownLargeMcpClassesCannotGrow() {
        assertLineCapBudgets();
    }

    @Test
    @McpCheckRef("mcp.package-shape.yaml-resource-byte-ratchet")
    public void knownLargeMcpYamlResourcesCannotGrowSilently() {
        assertYamlByteBudgets();
    }

    @Test
    @McpCheckRef("mcp.package-shape.recursive-surface-ratchet")
    public void broadMcpSurfacesCannotGrowSilently() {
        assertRecursiveSurfaceBudgets();
    }

    @Test
    @McpCheckRef("mcp.package-shape.skill-surfaces")
    public void mcpSkillSurfacesPublishOwnerRootReadmeAndSanityRoute() {
        assertSkillSurfacesPublishOwnerRootReadmeAndSanityRoute();
    }

    @Test
    @McpCheckRef("mcp.package-shape.console-gateway")
    public void mainMcpCodeRoutesConsoleOutputThroughConsoleLogger() throws IOException {
        assertConsoleOutputUsesGateway(MAIN_MCP);
    }

    @Test
    @McpCheckRef("mcp.package-shape.no-atlas-imports")
    public void mainMcpCodeDoesNotImportAtlasProductCode() throws IOException {
        assertNoDirectImports(
                MAIN_MCP,
                ATLAS_PACKAGE,
                "MCP is the package governance product; Atlas quality calibration must stay artifact/YAML based, "
                        + "not a Java dependency: ");
    }

    @Test
    @McpCheckRef("mcp.package-shape.tests.no-atlas-imports")
    public void mcpTestCodeDoesNotImportAtlasProductCode() throws IOException {
        assertNoDirectImports(
                TEST_MCP_ROOT,
                ATLAS_PACKAGE,
                "MCP contract tests must validate MCP as an agentic governance skill. Atlas calibration stays "
                        + "artifact/YAML based or in Atlas suites, not a Java test dependency: ");
    }

    @Test
    @McpCheckRef("mcp.package-shape.local-readmes.credit-efficient")
    public void majorMcpPackagesExposeCreditEfficientLocalReadmes() {
        assertMajorPackagesExposeCreditEfficientReadmes(MAIN_MCP);
    }

    @Test
    public void mcpLombokUsageStaysBroadDisciplinedAndImmutable() throws IOException {
        assertLombokUsageStaysDisciplined(MAIN_MCP, TEST_MCP_ROOT);
    }

    @Test
    @McpCheckRef("mcp.negative-support.testng-classloader.loadable")
    public void mcpRunNegativeSupportIsPublicAndTestNgLoadable() {
        Class<?> support = McpRunNegativeSupport.class;

        Assert.assertTrue(Modifier.isPublic(support.getModifiers()),
                "McpRunNegativeSupport must be public for selected TestNG subsets.");
        Assert.assertTrue(Modifier.isFinal(support.getModifiers()),
                "McpRunNegativeSupport should remain a stateless final helper.");
    }
}
