package com.ai.skills.mcp.tests.negative;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpProjectLayout;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.RequiresArtifacts;
import com.ai.skills.mcp.contract.ValidatesMcpContract;
import com.ai.skills.mcp.rules.ui.GeneratedUiApiStructureRule;
import com.ai.skills.mcp.rules.ui.NoFrameworkReportingInGeneratedUiTestsRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.mcp.tests.support.McpNegativeFixture.cloneLatestPackageSourceSpine;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.ai.skills.mcp.validation.McpViolationValidation.hasCode;
import static com.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("ui-negative-contracts")
@RequiresArtifacts({
        "review/root-source-thin-no-index.diff"
})
@McpSuiteRef("mcp.suite.mcp-ui-negative-contract-test")
public class McpUiNegativeContractTest {
    private static final String GENERATED_UI_TEST =
            McpProjectLayout.TEST_JAVA_ROOT + "/com/ai/ui/tests/GeneratedNoReportingTests.java";

    @Test
    @ValidatesMcpContract("ui.test-architecture.framework-reporting-enabled")
    @McpCheckRef("ui.test-architecture.framework-reporting-enabled.negative")
    public void rejectsGeneratedUiTestsThatDisableFrameworkReporting() throws IOException {
        Path projectRoot = Files.createTempDirectory("mcp-ui-reporting-contract-");
        Path packageRoot = cloneLatestPackageSourceSpine();
        writeRuntime(projectRoot);
        writeGeneratedUiTest(projectRoot, true);
        appendRootDiff(packageRoot, "+@NoFrameworkReporting");

        McpPackageValidationResult result = validateWithRules(projectRoot, packageRoot,
                new NoFrameworkReportingInGeneratedUiTestsRule());

        logValidatorScenario(
                "Reject generated UI test hidden from framework reporting",
                packageRoot,
                "Injected @NoFrameworkReporting into a diff-touched UI test source.",
                "Expected violation: ui-test.architecture.framework-reporting-disabled.",
                "Asserted artifact: review/root-source-thin-no-index.diff and generated UI test source.",
                result);
        requireCode(result, "ui-test.architecture.framework-reporting-disabled");
    }

    @Test
    @ValidatesMcpContract("ui.test-architecture.framework-reporting-enabled")
    @McpCheckRef("ui.test-architecture.framework-reporting-enabled.comment-safe")
    public void ignoresNoFrameworkReportingMentionsThatAreNotAnnotations() throws IOException {
        Path projectRoot = Files.createTempDirectory("mcp-ui-reporting-comment-contract-");
        Path packageRoot = cloneLatestPackageSourceSpine();
        writeRuntime(projectRoot);
        writeGeneratedUiTest(projectRoot, false);
        appendRootDiff(packageRoot, "+// @NoFrameworkReporting is documented here, not applied.");

        McpPackageValidationResult result = validateWithRules(projectRoot, packageRoot,
                new NoFrameworkReportingInGeneratedUiTestsRule());

        logValidatorScenario(
                "Ignore documentation-only NoFrameworkReporting mention",
                packageRoot,
                "Mentioned @NoFrameworkReporting in a comment/string but did not annotate the generated UI test.",
                "Expected invariant: no ui-test.architecture.framework-reporting-disabled violation.",
                "Asserted artifact: review/root-source-thin-no-index.diff and generated UI test source.",
                result);
        if (hasCode(result, "ui-test.architecture.framework-reporting-disabled")) {
            throw new AssertionError("Documentation-only @NoFrameworkReporting mention must not trigger the MCP blocker: "
                    + result.violations());
        }
    }

    @Test
    @ValidatesMcpContract("ui-api-generated.structure.modern-layout")
    @McpCheckRef("ui-generated.structure.legacy-path.negative")
    public void rejectsGeneratedUiLegacyPaths() throws IOException {
        Path projectRoot = Files.createTempDirectory("mcp-ui-modern-structure-contract-");
        Path packageRoot = cloneLatestPackageSourceSpine();
        writeRuntime(projectRoot);
        appendRootDiffFor(packageRoot, McpProjectLayout.MAIN_BASE_PATH + "/ui/pages/LegacyAccountPage.java",
                "+public final class LegacyAccountPage {}");
        appendRootDiffFor(packageRoot, McpProjectLayout.TEST_BASE_PATH + "/ui/tests/AdminOrderStatusRun01OnHoldTests.java",
                "+public final class AdminOrderStatusRun01OnHoldTests {}");

        McpPackageValidationResult result = validateWithRules(projectRoot, packageRoot,
                new GeneratedUiApiStructureRule());

        logValidatorScenario(
                "Reject legacy generated UI paths",
                packageRoot,
                "Added old-style ui/pages and flat ui/tests sources to the root diff.",
                "Expected violation: ui-generated.structure.legacy-path.",
                "Asserted artifact: review/root-source-thin-no-index.diff.",
                result);
        requireCode(result, "ui-generated.structure.legacy-path");
    }

    @Test
    @ValidatesMcpContract("ui-api-generated.structure.modern-layout")
    @McpCheckRef("api-generated.structure.legacy-path.negative")
    public void rejectsGeneratedApiLegacyPaths() throws IOException {
        Path projectRoot = Files.createTempDirectory("mcp-api-modern-structure-contract-");
        Path packageRoot = cloneLatestPackageSourceSpine();
        writeRuntime(projectRoot);
        appendRootDiffFor(packageRoot, McpProjectLayout.MAIN_BASE_PATH + "/api/model/LegacyUserRecord.java",
                "+public final class LegacyUserRecord {}");
        appendRootDiffFor(packageRoot, McpProjectLayout.TEST_BASE_PATH + "/api/tests/LegacyApiSmokeTest.java",
                "+public final class LegacyApiSmokeTest {}");

        McpPackageValidationResult result = validateWithRules(projectRoot, packageRoot,
                new GeneratedUiApiStructureRule());

        logValidatorScenario(
                "Reject legacy generated API paths",
                packageRoot,
                "Added old-style api/model and flat api/tests sources to the root diff.",
                "Expected violation: api-generated.structure.legacy-path.",
                "Asserted artifact: review/root-source-thin-no-index.diff.",
                result);
        requireCode(result, "api-generated.structure.legacy-path");
    }

    private void writeRuntime(Path projectRoot) throws IOException {
        Path runtime = projectRoot.resolve(McpArtifactPaths.RUNTIME_PROPERTIES);
        Files.createDirectories(runtime.getParent());
        Files.writeString(runtime, String.join(System.lineSeparator(),
                "RUN_EXPECTED_DELTA_QUALITY=92",
                "ROUND_TARGET_TEST_COUNT=25",
                "META_ITERATION_COUNT=5",
                "UI_TEST_ROOT=" + McpProjectLayout.TEST_JAVA_ROOT + "/com/ai/ui/tests",
                "SEED_RECORDING_CLASS=",
                ""));
    }

    private void writeGeneratedUiTest(Path projectRoot, boolean annotated) throws IOException {
        Path source = projectRoot.resolve(GENERATED_UI_TEST);
        Files.createDirectories(source.getParent());
        Files.writeString(source, String.join(System.lineSeparator(),
                "package com.ai.ui.tests;",
                "",
                "import com.ai.core.reporting.listener.NoFrameworkReporting;",
                "import com.ai.ui.core.runtime.BaseTestUI;",
                "import com.ai.ui.support.actions.UIActions;",
                "import org.testng.annotations.Test;",
                "",
                "// @NoFrameworkReporting must stay documentation-only unless explicitly enabled by this fixture.",
                annotated ? "@NoFrameworkReporting" : "",
                "public final class GeneratedNoReportingTests extends BaseTestUI {",
                "    private static final String CONTRACT_NOTE = \"NoFrameworkReporting is forbidden for generated UI tests\";",
                "",
                "    @Test",
                "    public void generated_ui_test_should_stay_reported() {",
                "        UIActions.getText(\"body\", \"visible business proof\");",
                "    }",
                "}",
                ""));
    }

    private void appendRootDiff(Path packageRoot, String addedLine) throws IOException {
        appendRootDiffFor(packageRoot, GENERATED_UI_TEST, addedLine);
    }

    private void appendRootDiffFor(Path packageRoot, String file, String addedLine) throws IOException {
        Path rootDiff = packageRoot.resolve(McpArtifactPaths.ROOT_DIFF);
        Files.writeString(rootDiff, Files.readString(rootDiff)
                + System.lineSeparator()
                + "diff --git a/" + file + " b/" + file + System.lineSeparator()
                + "--- a/" + file + System.lineSeparator()
                + "+++ b/" + file + System.lineSeparator()
                + addedLine + System.lineSeparator());
    }
}
