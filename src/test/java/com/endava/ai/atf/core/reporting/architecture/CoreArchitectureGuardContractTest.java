package com.endava.ai.atf.core.reporting.architecture;

import com.endava.ai.atf.core.reporting.architecture.support.ArchitectureSourceScanner;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.core.AtfCoreArea;
import com.endava.ai.core.support.contract.core.AtfCoreCheck;
import com.endava.ai.core.support.contract.core.AtfCoreContract;
import com.endava.ai.core.support.contract.core.AtfCoreEvidenceKind;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentSemanticToken;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.CORE_ARCHITECTURE_GUARD_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.CORE_ARCHITECTURE_GUARD_CONTRACT_TEST_CLASS)
@AtfCoreContract(
        id = "core.architecture.reqscore-guardrails",
        area = AtfCoreArea.ARCHITECTURE_BOUNDARY,
        owner = "ATF core architecture",
        invariant = "ReqsCore architecture roles remain enforced by executable contracts.",
        repairHint = "Use typed options/results and role-specific classes instead of adding broad static helpers."
)
@AtfCoreCheck(
        contract = "core.architecture.reqscore-guardrails",
        evidenceKind = AtfCoreEvidenceKind.SOURCE_SCAN,
        evidence = "Scans core source trees for layer leaks, side-effect ownership, factory policy and duplicated report markers."
)
@Listeners(TestListener.class)
public class CoreArchitectureGuardContractTest {

    private static final Path CORE_MAIN = AtfSourcePaths.MAIN_CORE_ROOT;
    private static final Path UI_MAIN = AtfSourcePaths.MAIN_UI_ROOT;
    private static final Path API_CORE = AtfSourcePaths.MAIN_API_CORE_ROOT;
    private static final Path UI_CORE = AtfSourcePaths.MAIN_UI_CORE_ROOT;
    private static final Path EXTENT_ADAPTER = AtfSourcePaths.mainJavaPackage(
            AtfSourcePaths.CORE_ROOT_PACKAGE + ".reporting.adapters.extent");
    private static final Path RUNTIME_AUDIT = AtfSourcePaths.mainJavaPackage(
            AtfSourcePaths.CORE_ROOT_PACKAGE + ".reporting.listener.internal.runtime");

    private static final List<Path> PURE_RENDERERS = List.of(
            RUNTIME_AUDIT.resolve("render/ExtentRuntimeDashboardRenderer.java"),
            RUNTIME_AUDIT.resolve("render/ExtentRuntimeFailureRenderer.java"),
            RUNTIME_AUDIT.resolve("render/ExtentRuntimeTagAuditRenderer.java"),
            RUNTIME_AUDIT.resolve("render/ExtentRuntimeTimelineRenderer.java")
    );

    private static final List<Path> EXTENT_PATCH_PLANNERS = List.of(
            EXTENT_ADAPTER.resolve("patch/component/ExtentComponentOverviewPatchPlanner.java"),
            EXTENT_ADAPTER.resolve("patch/navigation/ExtentNavigationPatchPlanner.java"),
            EXTENT_ADAPTER.resolve("patch/mode/ExtentReportModePatchPlanner.java")
    );

    private static final List<String> REPORT_MODE_TOKENS = List.of(
            ExtentSemanticToken.REPORT_MODE_ATTRIBUTE.value(),
            ExtentSemanticToken.RUNTIME_AUDITED_MODE.value(),
            ExtentSemanticToken.COMPONENT_ADAPTER_MODE.value()
    );

    @Test
    public void uiAndApiCoreDoNotDependOnEachOther() throws IOException {
        List<String> violations = new ArrayList<>();
        violations.addAll(importMatches(API_CORE, "com.endava.ai.ui.core"));
        violations.addAll(importMatches(UI_CORE, "com.endava.ai.api.core"));

        Assert.assertTrue(violations.isEmpty(),
                "api.core and ui.core must remain sibling modules, not dependencies of each other: " + violations);
    }

    @Test
    public void rendererClassesDoNotOwnIoConfigOrCurrentTime() throws IOException {
        List<String> violations = new ArrayList<>();
        List<String> forbidden = forbiddenBoundaryTokens("core.reporting.renderer-purity");
        for (Path renderer : PURE_RENDERERS) {
            collectForbiddenTokens(renderer, forbidden, violations);
        }

        Assert.assertTrue(violations.isEmpty(),
                "Renderers must stay aligned with core.reporting.renderer-purity YAML: " + violations);
    }

    @Test
    public void extentPatchPlannersArePureAndApplierOwnsPatchMutation() throws IOException {
        List<String> violations = new ArrayList<>();
        List<String> forbidden = forbiddenBoundaryTokens("core.reporting.patch-planner-purity");
        for (Path planner : EXTENT_PATCH_PLANNERS) {
            collectForbiddenTokens(planner, forbidden, violations);
        }

        String postProcessor = ArchitectureSourceScanner.read(
                EXTENT_ADAPTER.resolve("spark/ExtentSparkHtmlPostProcessor.java")
        );
        Assert.assertTrue(postProcessor.contains("ExtentHtmlPatchApplier.apply"),
                "ExtentSparkHtmlPostProcessor must delegate patch mutation to ExtentHtmlPatchApplier.");
        Assert.assertTrue(violations.isEmpty(),
                "Patch planners must stay aligned with core.reporting.patch-planner-purity YAML: " + violations);
    }

    @Test
    public void componentOverviewPatchKeepsPlanningInventoryAndRenderingSeparated() throws IOException {
        String planner = ArchitectureSourceScanner.read(
                EXTENT_ADAPTER.resolve("patch/component/ExtentComponentOverviewPatchPlanner.java")
        );
        String inventory = ArchitectureSourceScanner.read(
                EXTENT_ADAPTER.resolve("patch/component/ExtentComponentInventory.java")
        );
        String renderer = ArchitectureSourceScanner.read(
                EXTENT_ADAPTER.resolve("patch/component/ExtentComponentOverviewAuditRenderer.java")
        );

        Assert.assertTrue(planner.contains("ExtentComponentOverviewAuditRenderer.render"),
                "Component overview planner must delegate HTML assembly to the component renderer.");
        Assert.assertFalse(planner.contains(ExtentDataAttributes.COMPONENT_TAG_TABLE)
                        || planner.contains("distributionTable(")
                        || planner.contains("Pattern TEST_ITEM"),
                "Component overview planner must not own table markup or test-item inventory parsing.");
        Assert.assertTrue(inventory.contains("Pattern TEST_ITEM")
                        && inventory.contains("classCounts")
                        && inventory.contains("packageCounts"),
                "Component overview inventory must own Spark test item extraction.");
        Assert.assertFalse(inventory.contains("<table")
                        || inventory.contains(ExtentDataAttributes.COMPONENT_TAG_TABLE),
                "Component overview inventory must not render HTML.");
        Assert.assertTrue(renderer.contains("ExtentHtmlStructure.dataAttributePrefix")
                        && renderer.contains("ExtentDataAttributes.COMPONENT_TAG_TABLE")
                        && renderer.contains("distributionTable("),
                "Component overview renderer must own ATF tag-table markup through semantic HTML helpers.");
        Assert.assertFalse(renderer.contains("Pattern TEST_ITEM"),
                "Component overview renderer must receive inventory, not parse Spark test items.");
    }

    @Test
    public void uiEngineFactoryDelegatesConstructionToRegistryWithoutConcreteEnginePolicy() throws IOException {
        String factory = ArchitectureSourceScanner.read(UI_CORE.resolve("engine/UIEngineFactory.java"));

        Assert.assertFalse(factory.contains("new PlaywrightEngine"),
                "UIEngineFactory must not construct PlaywrightEngine directly.");
        Assert.assertFalse(factory.contains("new SeleniumEngine"),
                "UIEngineFactory must not construct SeleniumEngine directly.");
        Assert.assertFalse(factory.contains("switch ("),
                "UIEngineFactory must not branch on engine type; UiEngineRegistry owns provider selection.");
        Assert.assertTrue(factory.contains("UiEngineRegistry.defaults()"),
                "UIEngineFactory should stay a facade over UiEngineRegistry.");
    }

    @Test
    public void apiFacadeAndValidationFacadeStayThin() throws IOException {
        String apiActions = ArchitectureSourceScanner.read(API_CORE.resolve("client/ApiActions.java"));
        String validator = ArchitectureSourceScanner.read(API_CORE.resolve("validation/ResponseValidator.java"));

        Assert.assertTrue(apiActions.contains("ApiRequestExecutor"),
                "ApiActions must delegate HTTP execution and retry behavior.");
        Assert.assertFalse(apiActions.contains("Thread.sleep"),
                "ApiActions must not own retry sleeping directly.");
        Assert.assertFalse(apiActions.contains("Retry-After"),
                "ApiActions must not parse rate-limit headers directly.");

        Assert.assertFalse(validator.contains("Assert."),
                "ResponseValidator facade must delegate assertions to focused assertion families.");
        Assert.assertFalse(validator.contains("WaitUtils.waitUntil"),
                "ResponseValidator facade must delegate polling to PollingAssertions.");
    }

    @Test
    public void extentReporterDelegatesLifecycleToReportSession() throws IOException {
        String reporter = ArchitectureSourceScanner.read(EXTENT_ADAPTER.resolve("ExtentReporter.java"));
        String session = ArchitectureSourceScanner.read(EXTENT_ADAPTER.resolve("ExtentReportSession.java"));

        Assert.assertTrue(reporter.contains("ExtentReportSession"),
                "ExtentReporter must keep report lifecycle inside ExtentReportSession.");
        Assert.assertFalse(reporter.contains("new ExtentReports"),
                "ExtentReporter facade must not construct ExtentReports directly.");
        Assert.assertFalse(reporter.contains("ExtentSparkReportFinalizer.standardizeQuietly"),
                "ExtentReporter facade must not own post-flush finalization directly.");
        Assert.assertTrue(session.contains("new ExtentReports"),
                "ExtentReportSession owns the concrete ExtentReports instance.");
        Assert.assertTrue(session.contains("flushAndStandardize"),
                "ExtentReportSession owns flush and post-processing lifecycle.");
    }

    @Test
    public void reportModeTokensStayBehindSemanticOwners() throws IOException {
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(CORE_MAIN)) {
            List<Path> javaFiles = files
                    .filter(AtfSourcePaths::isJavaSource)
                    .collect(Collectors.toList());
            for (String token : REPORT_MODE_TOKENS) {
                List<Path> owners = javaFiles.stream()
                        .filter(path -> ArchitectureSourceScanner.readUnchecked(path).contains("\"" + token + "\""))
                        .collect(Collectors.toList());
                if (owners.size() > 1) {
                    violations.add(token + " appears as a raw literal in " + owners);
                }
            }
        }

        Assert.assertTrue(violations.isEmpty(),
                "Report mode tokens must be raw literals in one semantic owner only: " + violations);
    }

    @Test
    public void runtimeAuditRenderersUseTypedModelsAndSemanticSections() throws IOException {
        String dashboardRenderer = ArchitectureSourceScanner.read(
                RUNTIME_AUDIT.resolve("render/ExtentRuntimeDashboardRenderer.java")
        );
        String failureRenderer = ArchitectureSourceScanner.read(
                RUNTIME_AUDIT.resolve("render/ExtentRuntimeFailureRenderer.java")
        );
        String tagRenderer = ArchitectureSourceScanner.read(
                RUNTIME_AUDIT.resolve("render/ExtentRuntimeTagAuditRenderer.java")
        );
        String patchPlan = ArchitectureSourceScanner.read(
                RUNTIME_AUDIT.resolve("audit/ExtentHtmlPatchPlan.java")
        );

        Assert.assertTrue(dashboardRenderer.contains("DashboardSummary.from(summary)"),
                "Runtime dashboard rendering should read totals from DashboardSummary, not recalculate inline.");
        Assert.assertTrue(failureRenderer.contains("FailureGroup.from(summary.failures)"),
                "Failure renderer should group failed test cases through FailureGroup.");
        Assert.assertTrue(failureRenderer.contains("FailureSignature.from(summary.failures)"),
                "Failure renderer should group root causes through FailureSignature.");
        Assert.assertTrue(failureRenderer.contains("RuntimeAuditSection.FAILURE_GROUPS.wrap"),
                "Failure renderer should use the semantic runtime-audit section owner.");
        Assert.assertTrue(tagRenderer.contains("RuntimeAuditSection.TAG_AUDIT.wrap"),
                "Tag renderer should use the semantic runtime-audit section owner.");
        Assert.assertTrue(patchPlan.contains("RuntimeAuditSection.CORE_FINAL_AUDIT")
                        && patchPlan.contains("RuntimeAuditSection.TAG_AUDIT")
                        && patchPlan.contains("RuntimeAuditSection.FAILURE_GROUPS"),
                "Runtime audit patch removal must share the same section owner as renderers.");
    }

    @Test
    public void typedOptionsAreParsedNearRuntimeBoundaries() throws IOException {
        String uiFactory = ArchitectureSourceScanner.read(UI_CORE.resolve("engine/UIEngineFactory.java"));
        String apiActions = ArchitectureSourceScanner.read(API_CORE.resolve("client/ApiActions.java"));
        String apiOptions = ArchitectureSourceScanner.read(API_CORE.resolve("client/ApiRuntimeOptions.java"));

        Assert.assertTrue(uiFactory.contains("UiRuntimeOptions.load()"),
                "UI engine creation must parse config into UiRuntimeOptions at the boundary.");
        Assert.assertTrue(apiActions.contains("ApiRuntimeOptions.load()"),
                "API execution must parse config into ApiRuntimeOptions at the boundary.");
        Assert.assertTrue(apiOptions.contains("static ApiRuntimeOptions load()"),
                "ApiRuntimeOptions must expose the single API config parsing boundary.");
    }

    @Test
    public void uiFixedSleepsStayBehindUiSleeperPolicy() throws IOException {
        List<String> directSleeps = sourceMatches(UI_MAIN, Pattern.compile("\\bThread\\.sleep\\s*\\("))
                .stream()
                .filter(path -> !AtfSourcePaths.unixPath(path).endsWith("/ui/core/wait/UiSleeper.java"))
                .collect(Collectors.toList());

        String sleeper = ArchitectureSourceScanner.read(UI_MAIN.resolve("core/wait/UiSleeper.java"));
        Assert.assertTrue(sleeper.contains("Thread.sleep"),
                "UiSleeper must remain the single UI fixed-sleep boundary.");
        Assert.assertTrue(sleeper.contains("Thread.currentThread().interrupt()"),
                "UiSleeper must preserve interrupted status.");
        Assert.assertTrue(directSleeps.isEmpty(),
                "UI main code must use UiSleeper instead of direct Thread.sleep: " + directSleeps);
    }

    @Test
    public void thirdPartyClientConstructionStaysInsideAdaptersAndProviders() throws IOException {
        List<String> directClientConstruction = new ArrayList<>();
        directClientConstruction.addAll(sourceMatches(AtfSourcePaths.MAIN_AI_ROOT,
                Pattern.compile("new\\s+Playwright\\b")));
        directClientConstruction.addAll(sourceMatches(AtfSourcePaths.MAIN_AI_ROOT,
                Pattern.compile("new\\s+Selenium\\b")));

        List<String> outsideAllowedAdapters = directClientConstruction.stream()
                .filter(path -> !AtfSourcePaths.unixPath(path).contains("/ui/core/engine/"))
                .collect(Collectors.toList());

        Assert.assertTrue(outsideAllowedAdapters.isEmpty(),
                "Third-party UI client construction must stay inside ui.core engine adapters/providers: " + outsideAllowedAdapters);
    }

    private static List<String> importMatches(Path root, String forbiddenPackage) throws IOException {
        return sourceMatches(root, Pattern.compile("import\\s+" + Pattern.quote(forbiddenPackage)));
    }

    private static List<String> sourceMatches(Path root, Pattern pattern) throws IOException {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        return ArchitectureSourceScanner.sourceMatches(root, pattern);
    }

    private static List<String> forbiddenBoundaryTokens(String contractId) {
        return AtfCoreYamlContractCatalog.requireBoundary(contractId).forbidden();
    }

    private static void collectForbiddenTokens(Path sourceFile, List<String> forbiddenTokens, List<String> violations)
            throws IOException {
        String source = ArchitectureSourceScanner.read(sourceFile);
        forbiddenTokens.stream()
                .filter(source::contains)
                .map(token -> sourceFile + " contains forbidden token " + token)
                .forEach(violations::add);
    }
}
