package com.ai.skills.mcp.tests.core.calibration;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpViolationSeverity;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.rules.source.RealJavaTestCountabilityRule;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-run-countability-rule-contract-test")
public class McpRunCountabilityRuleContractTest {

    @Test
    @McpCheckRef("real-test-countability.config-methods.accepted")
    public void countabilityRuleAcceptsFiveRealTestsPlusConfigMethods() throws IOException {
        McpPackageValidationResult result = validateFixture(5, 5);

        Assert.assertFalse(result.hasBlockers(),
                "Config methods must not create count drift or fake missing @Test failures.");
    }

    @Test
    @McpCheckRef("real-test-countability.four-real-tests.rejected")
    public void countabilityRuleRejectsFourRealTestsWhenRunStateClaimsFive() throws IOException {
        McpPackageValidationResult result = validateFixture(4, 5);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "real-test-countability.count-drift".equals(violation.code())),
                "Four real tests with tests_produced=5 must be rejected as count drift.");
    }

    @Test
    @McpCheckRef("real-test-countability.html-ex-source-drift.warning")
    public void countabilityRuleWarnsForHistoricalHtmlExampleSourceDrift() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        Path projectRoot = Files.createTempDirectory("mcp-countability-project-");
        Path corpusRoot = Files.createTempDirectory("mcp-countability-corpus-");
        Path packageRoot = corpusRoot.resolve("MCP/html-ex/historical-source-drift");
        writeRuntime(projectRoot);
        writeSource(projectRoot, 1);
        writeRunArtifacts(packageRoot, 1, 1, "renamedAfterPromotion");

        McpPackageContext context = McpPackageContext.load(projectRoot, packageRoot, result);
        new RealJavaTestCountabilityRule().validate(context, result);

        Assert.assertFalse(result.violations().stream()
                        .anyMatch(violation -> "real-test-countability.method-missing".equals(violation.code())
                                && violation.severity() == McpViolationSeverity.BLOCKER),
                "Historical html-ex source drift must not be reported as latest live-source blocker.");
        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "real-test-countability.historical-source-drift".equals(violation.code())
                                && violation.severity() == McpViolationSeverity.WARNING),
                "Historical html-ex source drift must remain visible as a warning.");
    }

    private static McpPackageValidationResult validateFixture(int realTests, int testsProduced) throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        Path projectRoot = Files.createTempDirectory("mcp-countability-project-");
        Path packageRoot = Files.createTempDirectory("mcp-countability-package-");
        writeRuntime(projectRoot);
        writeSource(projectRoot, realTests);
        writeRunArtifacts(packageRoot, realTests, testsProduced, null);

        McpPackageContext context = McpPackageContext.load(projectRoot, packageRoot, result);
        new RealJavaTestCountabilityRule().validate(context, result);
        return result;
    }

    private static void writeRuntime(Path projectRoot) throws IOException {
        Path runtime = projectRoot.resolve("docs/prompts/html_ro/mcp/agent-runtime.properties");
        Files.createDirectories(runtime.getParent());
        Files.writeString(runtime, ""
                + "RUN_EXPECTED_DELTA_QUALITY=8\n"
                + "ROUND_TARGET_TEST_COUNT=5\n"
                + "META_ITERATION_COUNT=1\n");
    }

    private static void writeSource(Path projectRoot, int realTests) throws IOException {
        Path source = projectRoot.resolve("src/test/java/com/example/PositiveUserTests.java");
        Files.createDirectories(source.getParent());
        StringBuilder java = new StringBuilder("package com.example;\n"
                + "import org.testng.annotations.Test;\n"
                + "public class PositiveUserTests {\n"
                + "  public void setUp() {}\n");
        for (int index = 1; index <= realTests; index++) {
            java.append("  @Test public void test").append(index).append("() {}\n");
        }
        Files.writeString(source, java.append("  public void tearDown() {}\n}\n").toString());
    }

    private static void writeRunArtifacts(Path packageRoot, int realTests, int testsProduced, String overrideMethod) throws IOException {
        Path run = packageRoot.resolve("runs/run_01");
        Files.createDirectories(run.resolve("validation"));
        Files.createDirectories(run.resolve("review"));
        Files.writeString(run.resolve("validation/testng-results.xml"), xml(realTests, overrideMethod));
        Files.writeString(run.resolve("review/run-state.json"), "{\"tests_produced\":" + testsProduced + "}\n");
        Files.writeString(run.resolve("validation/command-result.md"), "mvn -q -Dtest=PositiveUserTests test\n");
    }

    private static String xml(int realTests, String overrideMethod) {
        StringBuilder xml = new StringBuilder("<testng-results><suite><test><class name=\"com.example.PositiveUserTests\">");
        xml.append("<test-method name=\"setUp\" status=\"PASS\" is-config=\"true\"/>");
        xml.append("<test-method name=\"beforeClass\" status=\"PASS\" is-config=\"true\"/>");
        for (int index = 1; index <= realTests; index++) {
            String method = overrideMethod == null ? "test" + index : overrideMethod;
            xml.append("<test-method name=\"").append(method).append("\" status=\"PASS\"/>");
        }
        xml.append("<test-method name=\"tearDown\" status=\"PASS\" is-config=\"true\"/>");
        return xml.append("</class></test></suite></testng-results>").toString();
    }
}
