package com.ai.atf.core.support.contract.core.yaml;

import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlBoundarySpec;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlContractAnnotationSpec;
import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlPolicySpec;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlProfileSpec;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlReportingOwnershipSpec;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlSizeBudgetSpec;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlUiEngineSpec;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ai.atf.core.support.contract.core.yaml.AtfCoreYamlProfileSuiteScanner.publicProfileSuiteRoots;
import static com.ai.atf.core.support.contract.core.yaml.AtfCoreYamlProfileSuiteScanner.publicSuitesUnder;

@Listeners(TestListener.class)
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_MANIFEST)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_MANIFEST_CATALOG)
public class AtfCoreYamlContractManifestTest {
    private static final Set<String> ALLOWED_CORE_SEVERITY = Set.of("BLOCKER", "WARNING");
    private static final Set<String> ALLOWED_STABILITY = Set.of("stable", "diagnostic", "smoke");
    private static final Set<String> ALLOWED_BROWSER = Set.of("false", "partial", "true");
    private static final Set<String> ALLOWED_SIZE_BUDGET_KINDS = Set.of(
            "package-root",
            "legacy-package-cap",
            "file-max-lines",
            "recursive-file-max-lines",
            "direct-file-max-lines",
            "package-cap",
            "test-root",
            "support-root",
            "legacy-test-cap"
    );
    private static final Set<String> ALLOWED_REPORTING_OWNERSHIP_KINDS = Set.of(
            "expected-main",
            "forbidden-test",
            "test-side-exception"
    );
    private static final Set<String> ALLOWED_UI_ENGINE_KINDS = Set.of(
            "required-action",
            "headless-override-marker",
            "headless-restore-marker",
            "ui-default"
    );
    private static final Set<String> ALLOWED_REPORTING_SHELL_KINDS = Set.of(
            "required-token", "required-mode", "publish-policy", "report-kind",
            "html-ex-current-standard-corpus", "forbidden-visible-token", "forbidden-visible-fallback");
    private static final Set<String> ALLOWED_CONSOLE_SIGNATURE_KINDS = Set.of(
            "required-fragment",
            "forbidden-fragment"
    );
    private static final Set<String> ALLOWED_RUNTIME_OPTION_KINDS = Set.of(
            "canonical-key",
            "fallback-key"
    );
    private static final Set<String> ALLOWED_CONTRACT_ANNOTATION_KINDS = Set.of(
            "profile",
            "area"
    );
    private static final Set<String> ALLOWED_CONTRACT_ANNOTATION_ENGINES = Set.of(
            ReportingConfigKeys.ENGINE_EXTENT,
            ReportingConfigKeys.ENGINE_ALLURE
    );
    private static final String TEST_JAVA_ROOT_PREFIX = AtfSourcePaths.unixPath(AtfSourcePaths.TEST_JAVA_ROOT) + "/";

    @Test
    public void profileManifestHasValidSchemaAndExistingSuites() {
        Assert.assertFalse(AtfCoreYamlContractCatalog.profiles().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateProfileIds().isEmpty());

        Path root = workspaceRoot();
        for (AtfCoreYamlProfileSpec profile : AtfCoreYamlContractCatalog.profiles()) {
            Assert.assertTrue(profile.id().startsWith("atf-"), "Unexpected ATF profile id: " + profile.id());
            Assert.assertTrue(Files.isRegularFile(root.resolve(profile.suiteXml())),
                    "Missing suiteXml for profile " + profile.id() + ": " + profile.suiteXml());
            Assert.assertTrue(ALLOWED_STABILITY.contains(profile.stability()),
                    "Invalid stability for " + profile.id() + ": " + profile.stability());
            Assert.assertTrue(ALLOWED_BROWSER.contains(profile.browser()),
                    "Invalid browser flag for " + profile.id() + ": " + profile.browser());
            Assert.assertFalse(profile.role().isBlank(), "Missing role for " + profile.id());
            Assert.assertFalse(profile.areas().isEmpty(), "Missing areas for " + profile.id());
            Assert.assertFalse(profile.closeoutRole().isBlank(), "Missing closeoutRole for " + profile.id());
            Assert.assertEquals(profile.allowedSkipped(), 0,
                    "ATF core profiles must not normalize skipped tests: " + profile.id());
            Assert.assertFalse(profile.forbiddenClassFragments().isEmpty(),
                    "Profiles must declare forbidden validator/package fragments: " + profile.id());
            Assert.assertFalse(profile.expectedClasses().isEmpty() && profile.expectedPackages().isEmpty(),
                    "Profile must declare either expectedClasses or expectedPackages: " + profile.id());
        }
    }

    @Test
    public void profileManifestCoversEveryCoreTestNgSuite() throws IOException {
        Path root = workspaceRoot();
        Set<String> actualSuites = publicProfileSuiteRoots().stream()
                .flatMap(suiteRoot -> publicSuitesUnder(root.resolve(suiteRoot)).stream())
                .collect(Collectors.toUnmodifiableSet());

        Set<String> declaredSuites = AtfCoreYamlContractCatalog.profiles().stream()
                .map(AtfCoreYamlProfileSpec::suiteXml)
                .collect(Collectors.toUnmodifiableSet());

        Assert.assertEquals(declaredSuites, actualSuites,
                "profiles.yml must describe every ATF core TestNG suite exactly once.");
    }

    @Test
    public void boundaryManifestHasValidSchemaAndExistingSourceRoots() {
        Assert.assertFalse(AtfCoreYamlContractCatalog.boundaries().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateBoundaryIds().isEmpty());

        Path root = workspaceRoot();
        for (AtfCoreYamlBoundarySpec boundary : AtfCoreYamlContractCatalog.boundaries()) {
            Assert.assertTrue(boundary.id().startsWith("core."), "Unexpected boundary id: " + boundary.id());
            Assert.assertTrue(Files.exists(root.resolve(boundary.sourceRoot())),
                    "Missing sourceRoot for boundary " + boundary.id() + ": " + boundary.sourceRoot());
            for (String scanRoot : boundary.scanRoots()) {
                Assert.assertTrue(Files.exists(root.resolve(scanRoot)),
                        "Missing scanRoot for boundary " + boundary.id() + ": " + scanRoot);
            }
            Assert.assertTrue(ALLOWED_CORE_SEVERITY.contains(boundary.severity()),
                    "Invalid severity for " + boundary.id() + ": " + boundary.severity());
            Assert.assertFalse(boundary.owner().isBlank(), "Missing owner for " + boundary.id());
            Assert.assertFalse(boundary.protectedRoots().isEmpty(), "Missing protected roots for " + boundary.id());
            Assert.assertFalse(boundary.forbidden().isEmpty(), "Missing forbidden tokens for " + boundary.id());
            Assert.assertFalse(boundary.repair().isBlank(), "Missing repair guidance for " + boundary.id());
        }
    }

    @Test
    public void boundaryManifestKeepsConsoleLoggerAndProtectedRootRulesVisible() {
        Set<String> ids = AtfCoreYamlContractCatalog.boundaries().stream()
                .map(AtfCoreYamlBoundarySpec::id)
                .collect(Collectors.toUnmodifiableSet());

        Assert.assertTrue(ids.contains("core.console-logger-single-entrypoint"));
        Assert.assertTrue(ids.contains("core.architecture.no-mcp-dependency"));
        Assert.assertTrue(ids.contains("core.reporting.patch-planner-purity"));
        Assert.assertTrue(ids.contains("core.reporting.renderer-purity"));
        Assert.assertTrue(ids.contains("core.ui-api.sibling-boundary"));
        Assert.assertTrue(ids.contains("core.reporting-artifact-immutability"));
    }

    @Test
    public void sizeBudgetManifestHasValidSchemaAndExistingTargets() {
        Assert.assertFalse(AtfCoreYamlContractCatalog.sizeBudgets().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateSizeBudgetIds().isEmpty());

        Path root = workspaceRoot();
        for (AtfCoreYamlSizeBudgetSpec budget : AtfCoreYamlContractCatalog.sizeBudgets()) {
            Assert.assertTrue(budget.id().startsWith("core."), "Unexpected size-budget id: " + budget.id());
            Assert.assertTrue(ALLOWED_SIZE_BUDGET_KINDS.contains(budget.kind()),
                    "Invalid size-budget kind for " + budget.id() + ": " + budget.kind());
            Assert.assertTrue(budget.max() > 0, "Size-budget max must be positive for " + budget.id());
            Assert.assertFalse(budget.owner().isBlank(), "Missing owner for " + budget.id());
            Assert.assertFalse(budget.repair().isBlank(), "Missing repair guidance for " + budget.id());
            Assert.assertTrue(Files.exists(root.resolve(budget.path())),
                    "Missing size-budget target for " + budget.id() + ": " + budget.path());
        }
    }

    @Test
    public void reportingOwnershipManifestHasValidSchemaAndExpectedRuntimeFiles() {
        Assert.assertFalse(AtfCoreYamlContractCatalog.reportingOwnership().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateReportingOwnershipIds().isEmpty());

        Path root = workspaceRoot();
        for (AtfCoreYamlReportingOwnershipSpec ownership : AtfCoreYamlContractCatalog.reportingOwnership()) {
            Assert.assertTrue(ownership.id().startsWith("core."), "Unexpected reporting-ownership id: " + ownership.id());
            Assert.assertTrue(ALLOWED_REPORTING_OWNERSHIP_KINDS.contains(ownership.kind()),
                    "Invalid reporting-ownership kind for " + ownership.id() + ": " + ownership.kind());
            Assert.assertFalse(ownership.owner().isBlank(), "Missing owner for " + ownership.id());
            Assert.assertFalse(ownership.repair().isBlank(), "Missing repair guidance for " + ownership.id());

            if ("expected-main".equals(ownership.kind()) || "test-side-exception".equals(ownership.kind())) {
                Assert.assertTrue(Files.isRegularFile(root.resolve(ownership.path())),
                        "Missing required reporting ownership file for " + ownership.id() + ": " + ownership.path());
            }
            if ("forbidden-test".equals(ownership.kind())) {
                Assert.assertTrue(ownership.path().startsWith(TEST_JAVA_ROOT_PREFIX),
                        "Forbidden reporting ownership entries must point at old test-side paths: " + ownership.path());
            }
        }
    }

    @Test
    public void uiEngineManifestHasValidSchemaAndRequiredDefaultGroups() {
        Assert.assertFalse(AtfCoreYamlContractCatalog.uiEngineContracts().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateUiEngineContractIds().isEmpty());

        Set<String> kinds = AtfCoreYamlContractCatalog.uiEngineContracts().stream()
                .map(AtfCoreYamlUiEngineSpec::kind)
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertTrue(kinds.contains("required-action"), "UI engine YAML must publish required actions.");
        Assert.assertTrue(kinds.contains("headless-override-marker"), "UI engine YAML must publish override markers.");
        Assert.assertTrue(kinds.contains("headless-restore-marker"), "UI engine YAML must publish restore markers.");
        Assert.assertTrue(kinds.contains("ui-default"), "UI engine YAML must publish framework.properties defaults.");

        for (AtfCoreYamlUiEngineSpec spec : AtfCoreYamlContractCatalog.uiEngineContracts()) {
            Assert.assertTrue(spec.id().startsWith("core."), "Unexpected UI engine contract id: " + spec.id());
            Assert.assertTrue(ALLOWED_UI_ENGINE_KINDS.contains(spec.kind()),
                    "Invalid UI engine contract kind for " + spec.id() + ": " + spec.kind());
            Assert.assertFalse(spec.key().isBlank(), "Missing key for " + spec.id());
            Assert.assertFalse(spec.value().isBlank(), "Missing value for " + spec.id());
            Assert.assertFalse(spec.owner().isBlank(), "Missing owner for " + spec.id());
            Assert.assertFalse(spec.repair().isBlank(), "Missing repair guidance for " + spec.id());
        }
    }

    @Test
    public void reportingShellManifestHasValidSchemaAndExistingTokens() throws IOException, ClassNotFoundException {
        Assert.assertFalse(AtfCoreYamlContractCatalog.reportingShellContracts().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateReportingShellIds().isEmpty());

        Path root = workspaceRoot();
        for (AtfCoreYamlPolicySpec spec : AtfCoreYamlContractCatalog.reportingShellContracts()) {
            Assert.assertTrue(spec.id().startsWith("core.reporting-shell."), "Unexpected reporting-shell id: " + spec.id());
            Assert.assertTrue(ALLOWED_REPORTING_SHELL_KINDS.contains(spec.kind()),
                    "Invalid reporting-shell kind for " + spec.id() + ": " + spec.kind());
            Assert.assertFalse(spec.surface().isBlank(), "Missing reporting-shell surface for " + spec.id());
            Assert.assertFalse(spec.owner().isBlank(), "Missing reporting-shell owner for " + spec.id());
            Assert.assertFalse(spec.repair().isBlank(), "Missing reporting-shell repair guidance for " + spec.id());
            Path source = root.resolve(spec.source());
            if ("html-ex-current-standard-corpus".equals(spec.kind())) {
                Assert.assertTrue(Files.isDirectory(source),
                        "html-ex corpus contract must point at the corpus root, not a concrete example folder: "
                                + spec.id() + ": " + source);
                Assert.assertFalse(spec.token().isBlank(), "html-ex corpus contract must declare relative report path.");
                Assert.assertFalse(spec.source().matches(".*/[^/]+-\\d{4}-\\d{2}-\\d{2}.*"),
                        "html-ex corpus contract must not pin a dated concrete example folder: " + spec.source());
                try (var directories = Files.list(source)) {
                    boolean hasReport = directories
                            .filter(Files::isDirectory)
                            .anyMatch(directory -> Files.isRegularFile(directory.resolve(spec.token())));
                    Assert.assertTrue(hasReport,
                            "html-ex corpus root must contain at least one folder with " + spec.token());
                }
            } else {
                Assert.assertTrue(Files.isRegularFile(source),
                        "Missing reporting-shell source for " + spec.id() + ": " + source);
                Assert.assertTrue(Files.readString(source).contains(spec.token()),
                        "Reporting-shell token must be owned by declared source for " + spec.id() + ": " + spec.token());
            }
            assertCoreVisibleOrSkillOwnedValidator(spec.validator());
        }
    }

    private static void assertCoreVisibleOrSkillOwnedValidator(String validator)
            throws ClassNotFoundException {
        if (validator.startsWith("com.ai.atf.")) {
            AtfCoreYamlContractClassLoader.load(validator);
            return;
        }
        Assert.assertTrue(validator.startsWith("com.ai.skills."),
                "External reporting-shell validators must stay under skills ownership: " + validator);
    }

    @Test
    public void consoleSignatureManifestHasValidSchemaAndForbiddenFragments() throws ClassNotFoundException {
        Assert.assertFalse(AtfCoreYamlContractCatalog.consoleSignatureContracts().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateConsoleSignatureIds().isEmpty());

        Set<String> kinds = AtfCoreYamlContractCatalog.consoleSignatureContracts().stream()
                .map(AtfCoreYamlPolicySpec::kind)
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertTrue(kinds.contains("required-fragment"), "Console signature YAML must declare required fragments.");
        Assert.assertTrue(kinds.contains("forbidden-fragment"), "Console signature YAML must declare forbidden fragments.");

        for (AtfCoreYamlPolicySpec spec : AtfCoreYamlContractCatalog.consoleSignatureContracts()) {
            Assert.assertTrue(spec.id().startsWith("core.console."), "Unexpected console-signature id: " + spec.id());
            Assert.assertTrue(ALLOWED_CONSOLE_SIGNATURE_KINDS.contains(spec.kind()),
                    "Invalid console-signature kind for " + spec.id() + ": " + spec.kind());
            Assert.assertFalse(spec.surface().isBlank(), "Missing console-signature surface for " + spec.id());
            Assert.assertFalse(spec.owner().isBlank(), "Missing console-signature owner for " + spec.id());
            Assert.assertFalse(spec.fragment().isBlank(), "Missing console-signature fragment for " + spec.id());
            Assert.assertFalse(spec.repair().isBlank(), "Missing console-signature repair guidance for " + spec.id());
            AtfCoreYamlContractClassLoader.load(spec.validator());
        }
    }

    @Test
    public void runtimeOptionsManifestMatchesUiRuntimeOptionsConstants() throws Exception {
        Assert.assertFalse(AtfCoreYamlContractCatalog.runtimeOptionsContracts().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateRuntimeOptionsIds().isEmpty());

        Set<String> keys = AtfCoreYamlContractCatalog.runtimeOptionsContracts().stream()
                .map(AtfCoreYamlPolicySpec::key)
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertTrue(keys.contains(UiRuntimeOptions.UI_ENGINE_KEY));
        Assert.assertTrue(keys.contains(UiRuntimeOptions.UI_HEADLESS_KEY));
        Assert.assertTrue(keys.contains(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY));
        Assert.assertTrue(keys.contains(UiRuntimeOptions.LEGACY_WAIT_TIMEOUT_SECONDS_KEY));
        Assert.assertTrue(keys.contains(UiRuntimeOptions.CONSOLE_DETAILS_ENABLED_KEY));

        for (AtfCoreYamlPolicySpec spec : AtfCoreYamlContractCatalog.runtimeOptionsContracts()) {
            Assert.assertTrue(spec.id().startsWith("core.runtime-options."), "Unexpected runtime-options id: " + spec.id());
            Assert.assertTrue(ALLOWED_RUNTIME_OPTION_KINDS.contains(spec.kind()),
                    "Invalid runtime-options kind for " + spec.id() + ": " + spec.kind());
            Assert.assertFalse(spec.owner().isBlank(), "Missing runtime-options owner for " + spec.id());
            Assert.assertFalse(spec.defaultValue().isBlank(), "Missing runtime-options default value for " + spec.id());
            Assert.assertFalse(spec.repair().isBlank(), "Missing runtime-options repair guidance for " + spec.id());
            Assert.assertEquals(uiRuntimeOptionConstant(spec.constant()), spec.key(),
                    "runtime-options YAML key must match UiRuntimeOptions constant for " + spec.id());
            AtfCoreYamlContractClassLoader.load(spec.validator());
        }
    }

    @Test
    public void contractAnnotationManifestHasValidSchemaAndProfileBoundaries() {
        Assert.assertFalse(AtfCoreYamlContractCatalog.contractAnnotationSpecs().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateContractAnnotationIds().isEmpty());

        Assert.assertFalse(AtfCoreYamlContractCatalog.contractAnnotationProfiles().isEmpty(),
                "contract-annotations.yml must declare AtfContractProfile rows.");
        Assert.assertFalse(AtfCoreYamlContractCatalog.contractAnnotationAreas().isEmpty(),
                "contract-annotations.yml must declare AtfContractArea rows.");

        for (AtfCoreYamlContractAnnotationSpec spec : AtfCoreYamlContractCatalog.contractAnnotationSpecs()) {
            Assert.assertTrue(spec.id().startsWith("core.contract-"),
                    "Unexpected contract annotation id: " + spec.id());
            Assert.assertTrue(ALLOWED_CONTRACT_ANNOTATION_KINDS.contains(spec.kind()),
                    "Invalid contract annotation kind for " + spec.id() + ": " + spec.kind());
            Assert.assertFalse(spec.name().isBlank(), "Missing annotation name for " + spec.id());
            Assert.assertFalse(spec.owner().isBlank(), "Missing annotation owner for " + spec.id());
            Assert.assertFalse(spec.purpose().isBlank(), "Missing annotation purpose for " + spec.id());

            if (spec.isProfile()) {
                Assert.assertTrue(ALLOWED_CONTRACT_ANNOTATION_ENGINES.contains(spec.recommendedEngine()),
                        "Invalid recommended engine for " + spec.id() + ": " + spec.recommendedEngine());
                Assert.assertFalse(spec.pureFramework() && spec.liveBoundary(),
                        "Contract annotation profile cannot be both pure framework and live boundary: " + spec.name());
            }
        }
    }

    @Test
    public void genericPolicySpecDoesNotExposeRawYamlDictionaryApi() {
        Set<String> forbiddenNames = Set.of("field", "optional", "hasField");
        List<String> offenders = Arrays.stream(AtfCoreYamlPolicySpec.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> forbiddenNames.contains(method.getName())
                        || method.getGenericReturnType().getTypeName().contains("java.util.Map"))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(offenders.isEmpty(),
                "ATF core YAML policy specs must expose semantic getters, not raw YAML dictionaries: " + offenders);
    }

    @Test
    public void yamlContractCatalogExposesIndexedLookupsForCanonicalContracts() {
        Assert.assertEquals(
                AtfCoreYamlContractCatalog.requireProfile("atf-core-fast").suiteXml(),
                AtfSourcePaths.unixPath(AtfSourcePaths.testResource("atf/core/suites/core/testng-atf-core-fast.xml"))
        );
        Assert.assertEquals(
                AtfCoreYamlContractCatalog.requireBoundary("core.console-logger-single-entrypoint").owner(),
                "ATF core console logging"
        );
        Assert.assertEquals(
                AtfCoreYamlContractCatalog.requireSizeBudget("core.reporting.class.listener").path(),
                AtfSourcePaths.unixPath(AtfSourcePaths.mainJava(TestListener.class))
        );
        Assert.assertEquals(
                AtfCoreYamlContractCatalog.requireRuntimeOptionsContract("core.runtime-options.ui-engine").key(),
                UiRuntimeOptions.UI_ENGINE_KEY
        );
        Assert.assertEquals(
                AtfCoreYamlContractCatalog.requireContractAnnotation("core.contract-profile.core").name(),
                "CORE_CONTRACT"
        );

        Assert.expectThrows(
                IllegalArgumentException.class,
                () -> AtfCoreYamlContractCatalog.requireBoundary("core.missing-contract")
        );
    }

    @Test
    public void contractCatalogTestsUseRobustClassLoaderInsteadOfSurefireContextOnly() throws IOException {
        Path root = workspaceRoot();
        List<String> offenders = new ArrayList<>();
        String forbiddenClassLoad = "Class.forName" + "(";
        String forbiddenContextLoader = "Thread.currentThread()" + ".getContextClassLoader()";
        for (Path sourceRoot : List.of(AtfSourcePaths.TEST_ATF_ROOT, AtfSourcePaths.TEST_MCP_ROOT)) {
            try (var files = Files.walk(root.resolve(sourceRoot))) {
                files.filter(AtfSourcePaths::isJavaSource)
                        .filter(path -> !path.endsWith("AtfCoreYamlContractManifestTest.java"))
                        .filter(path -> !path.endsWith("AtfCoreYamlContractClassLoader.java"))
                        .filter(path -> !path.endsWith("ContractAnnotationBestPracticeTest.java"))
                        .filter(path -> !path.endsWith("McpYamlContractSyncTest.java"))
                        .filter(path -> !path.endsWith("McpReportProfileClassloadingContractTest.java"))
                        .filter(path -> !path.endsWith("McpPackageShapeContractTest.java"))
                        .filter(path -> {
                            String source = read(path);
                            return source.contains(forbiddenClassLoad)
                                    && source.contains(forbiddenContextLoader);
                        })
                        .map(path -> root.relativize(path).toString())
                        .forEach(offenders::add);
            }
        }

        Assert.assertTrue(offenders.isEmpty(),
                "Contract catalog tests must load scanned validators/classes through a robust local loader, "
                        + "not the Surefire context classloader alone: " + offenders);
    }

    private static Path workspaceRoot() {
        return AtfSourcePaths.workspaceRoot();
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect contract test source: " + path, exception);
        }
    }

    private static String uiRuntimeOptionConstant(String constantName) throws ReflectiveOperationException {
        Field field = UiRuntimeOptions.class.getField(constantName);
        return String.valueOf(field.get(null));
    }
}
