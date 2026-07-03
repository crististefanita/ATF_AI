package com.endava.ai.atf.core.reporting.architecture;

import com.endava.ai.atf.core.reporting.architecture.support.ArchitectureSourceScanner;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlReportingArchitectureSpec;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlReportingOwnershipSpec;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlSizeBudgetSpec;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.endava.ai.core.reporting.listener.TestListener;
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

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REPORTING_PACKAGE_SHAPE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_CLASS)
@Listeners(TestListener.class)
public class AtfReportingPackageShapeContractTest {
    private static final String REPORTING_PACKAGE_SHAPE_RESOURCE = "skills/reporting/contracts/reporting-package-shape.yml";
    private static final List<String> REQUIRED_BUDGET_KINDS = List.of(
            "package-root", "package-cap", "legacy-package-cap", "file-max-lines", "recursive-file-max-lines",
            "direct-file-max-lines"
    );
    private static final Pattern STATIC_REPORT_LOGGER_FIELD =
            Pattern.compile("static\\s+(?!final\\s+ReportingSession\\s+SESSION)\\S*\\s*ReportLogger\\s+\\w+\\s*[=;]");
    private static final Pattern BROAD_LOMBOK_MUTABILITY_ANNOTATION = Pattern.compile("@(?:Data|Setter)\\b");
    private static final Pattern PUBLIC_LOMBOK_ALL_ARGS_CONSTRUCTOR =
            Pattern.compile("@AllArgsConstructor\\s*\\(\\s*access\\s*=\\s*AccessLevel\\.PUBLIC\\s*\\)");
    private static final Pattern DISALLOWED_LOMBOK_IMPORT = Pattern.compile(
            "import\\s+lombok\\.(?!(?:AccessLevel|AllArgsConstructor|Builder|Getter|NoArgsConstructor|NonNull|"
                    + "RequiredArgsConstructor|Value|With|experimental\\.Accessors);)\\S+;"
    );
    private static final Path CORE_ROOT = AtfSourcePaths.MAIN_CORE_ROOT;
    private static final Path ATF_TEST_ROOT = AtfSourcePaths.TEST_ATF_ROOT;
    private static final Path CORE_REPORTING_ROOT = AtfSourcePaths.MAIN_CORE_REPORTING_ROOT;
    private static final Path REPORT_LOGGER_SOURCE = CORE_REPORTING_ROOT.resolve("ReportLogger.java");
    private static final Path REPORTING_MANAGER_SOURCE = CORE_REPORTING_ROOT.resolve("ReportingManager.java");
    private static final Path REPORTING_SESSION_SOURCE = CORE_REPORTING_ROOT.resolve("ReportingSession.java");
    private static final Path TEST_LISTENER_SOURCE = CORE_REPORTING_ROOT.resolve("listener/TestListener.java");
    private static final Path EXTENT_SPARK_COLLAPSE_POLICY_SOURCE = CORE_REPORTING_ROOT.resolve(
            "adapters/extent/spark/style/ExtentSparkCollapsePolicy.java");
    private static final Path NON_BLOCKING_DIAGNOSTIC_SOURCE = CORE_REPORTING_ROOT.resolve(
            "diagnostic/NonBlockingDiagnostic.java");
    private static final int LEGACY_TEST_CAP_MAX_SLACK_LINES = 50;

    @Test
    public void reportingPackageShapeManifestRoutesOwnershipToSizeBudgets() throws IOException {
        List<java.util.Map<String, String>> entries = SimpleYamlContractLoader.load(REPORTING_PACKAGE_SHAPE_RESOURCE);
        Assert.assertFalse(entries.isEmpty(), "reporting-package-shape.yml must not be empty.");
        Assert.assertEquals(
                entries.stream().map(entry -> required(entry, "id")).collect(Collectors.toSet()).size(),
                entries.size(),
                "Core Reporting package-shape policy ids must be unique."
        );

        java.util.Map<String, String> source = entries.stream()
                .filter(entry -> "budget-source".equals(required(entry, "kind")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Core Reporting package-shape must declare budget-source."));
        Assert.assertEquals(required(source, "path"), "src/main/resources/skills/codequality/contracts/size-budgets.yml",
                "Core Reporting package-shape must not duplicate numeric caps outside size-budgets.yml.");
        Assert.assertTrue(Files.isRegularFile(AtfSourcePaths.workspaceRoot().resolve(required(source, "path"))),
                "Core Reporting package-shape budget source must exist.");

        List<String> declaredBudgetKinds = AtfCoreYamlContractCatalog.sizeBudgets().stream()
                .map(AtfCoreYamlSizeBudgetSpec::kind)
                .distinct()
                .collect(Collectors.toList());
        for (String budgetKind : REQUIRED_BUDGET_KINDS) {
            Assert.assertTrue(declaredBudgetKinds.contains(budgetKind),
                    "Core size-budgets.yml must declare budget kind `" + budgetKind + "`.");
            Assert.assertTrue(entries.stream().anyMatch(entry -> budgetKind.equals(entry.get("budgetKind"))),
                    "Core Reporting package-shape.yml must explain required budget kind `" + budgetKind + "`.");
        }

        for (java.util.Map<String, String> entry : entries) {
            Assert.assertFalse(required(entry, "reason").isBlank(),
                    "Every Core Reporting package-shape entry must explain why it exists: " + required(entry, "id"));
            if ("required-readme".equals(required(entry, "kind"))) {
                Path readme = AtfSourcePaths.workspaceRoot().resolve(required(entry, "path"));
                Assert.assertTrue(Files.isRegularFile(readme),
                        "Core Reporting package-shape README path must exist for " + required(entry, "id"));
                if (required(entry, "path").startsWith("src/main/java/")) {
                    String violation = ArchitectureSourceScanner.operationalReadmeShapeViolation(readme, required(entry, "id"));
                    Assert.assertTrue(violation.isBlank(), violation);
                }
            }
            if ("forbidden-import".equals(required(entry, "kind"))) {
                Assert.assertTrue(Files.isDirectory(AtfSourcePaths.workspaceRoot().resolve(required(entry, "root"))),
                        "Core Reporting package-shape root must exist for " + required(entry, "id"));
            }
        }
    }

    @Test
    public void reportingPackagesStayUnderClassLimit() throws IOException {
        List<String> overloadedPackages = new ArrayList<>();
        for (AtfCoreYamlSizeBudgetSpec rootBudget : sizeBudgets("package-root")) {
            overloadedPackages.addAll(ArchitectureSourceScanner.packageClassCountViolations(
                    rootBudget.asPath(),
                    rootBudget.max(),
                    packageCapOverrides()
            ));
        }

        Assert.assertTrue(
                overloadedPackages.isEmpty(),
                "Reporting packages must stay below the readability limit: " + overloadedPackages
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_CLASS_SIZE)
    public void coreReportingClassesStayWithinRoleLimits() throws IOException {
        List<String> oversized = new ArrayList<>();

        for (AtfCoreYamlSizeBudgetSpec budget : sizeBudgets("file-max-lines")) {
            ArchitectureSourceScanner.requireMaxLines(oversized, budget.asPath(), budget.max());
        }
        for (AtfCoreYamlSizeBudgetSpec budget : sizeBudgets("recursive-file-max-lines")) {
            oversized.addAll(ArchitectureSourceScanner.filesAboveLimit(budget.asPath(), budget.max()));
        }
        for (AtfCoreYamlSizeBudgetSpec budget : sizeBudgets("direct-file-max-lines")) {
            oversized.addAll(ArchitectureSourceScanner.directJavaFilesAboveLimit(budget.asPath(), budget.max()));
        }

        Assert.assertTrue(oversized.isEmpty(),
                "Core reporting classes exceeded role-based size limits: " + oversized);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_EXTENT_PACKAGE_MAP)
    public void extentSparkPackagesStayMappedAndCapped() throws IOException {
        List<String> violations = new ArrayList<>();

        for (AtfCoreYamlSizeBudgetSpec budget : sizeBudgets("package-cap")) {
            Path directory = budget.asPath();
            Assert.assertTrue(Files.isDirectory(directory), "Missing Extent Spark ownership package: " + directory);
            long classes = ArchitectureSourceScanner.directJavaFileCount(directory);
            if (classes > budget.max()) {
                violations.add(directory + " has " + classes + " direct classes, cap " + budget.max());
            }
        }

        Assert.assertTrue(violations.isEmpty(),
                "Extent Spark package ownership lanes exceeded caps: " + violations);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_AGGREGATE_OWNERSHIP)
    public void aggregateRuntimeArtifactWritersLiveInMainNotContractTests() {
        for (Path file : reportingOwnership("expected-main")) {
            Assert.assertTrue(Files.isRegularFile(file), "Reusable aggregate runtime file must live in main: " + file);
        }
        for (Path file : reportingOwnership("forbidden-test")) {
            Assert.assertFalse(Files.exists(file), "Reusable aggregate runtime code must not remain in test: " + file);
        }
        for (Path file : reportingOwnership("test-side-exception")) {
            Assert.assertTrue(Files.isRegularFile(file),
                    "TestNG/@AtfContract extraction model stays test-side because it depends on contract-test annotations: "
                            + file);
        }
    }

    @Test
    public void coreReportingArchitectureRowsMapPublicApiInternalBoundaryAndAuditSurface() {
        for (AtfCoreYamlReportingArchitectureSpec spec : AtfCoreYamlContractCatalog.reportingArchitecture()) {
            Assert.assertTrue(Files.exists(spec.sourceRootPath()),
                    "Reporting architecture source root must exist: " + spec.id());
            Assert.assertTrue(Files.exists(spec.repairTargetPath()),
                    "Reporting architecture repair target must exist: " + spec.id());
            Assert.assertTrue(Files.exists(Path.of(spec.suiteXml())),
                    "Reporting architecture suite must exist: " + spec.id());
            Assert.assertTrue(Files.exists(AtfSourcePaths.testJava(spec.contractTest())),
                    "Reporting architecture contract test must exist: " + spec.id());
            Assert.assertTrue(spec.publicApi().length() > 10,
                    "Reporting architecture public API must be explicit: " + spec.id());
            Assert.assertTrue(spec.internalBoundary().length() > 10,
                    "Reporting architecture internal boundary must be explicit: " + spec.id());
            Assert.assertTrue(spec.auditSurface().length() > 10,
                    "Reporting architecture audit surface must be explicit: " + spec.id());
        }
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_TEST_SIZE)
    public void atfContractTestsDoNotIntroduceNewGiantClasses() throws IOException {
        AtfCoreYamlSizeBudgetSpec testRoot = singleSizeBudget("test-root");
        List<String> violations = ArchitectureSourceScanner.oversizedTestClassViolations(
                testRoot.asPath(),
                testRoot.max(),
                ArchitectureSourceScanner.legacyLargeTestCaps()
        );

        Assert.assertTrue(violations.isEmpty(),
                "ATF contract test size guardrail failed: " + violations);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_LEGACY_TEST_RATCHETS)
    public void legacyTestCapsStayCloseToCurrentFileSize() throws IOException {
        List<String> staleCaps = new ArrayList<>();
        for (AtfCoreYamlSizeBudgetSpec budget : sizeBudgets("legacy-test-cap")) {
            Path file = budget.asPath();
            Assert.assertTrue(Files.isRegularFile(file), "Missing legacy-capped reporting test file: " + file);
            long lines = ArchitectureSourceScanner.lineCount(file);
            if (budget.max() > lines + LEGACY_TEST_CAP_MAX_SLACK_LINES) {
                staleCaps.add(file + " has cap " + budget.max() + " but only " + lines
                        + " lines; lower the cap near current size.");
            }
        }

        Assert.assertTrue(staleCaps.isEmpty(),
                "Legacy reporting test caps must ratchet down after refactors: " + staleCaps);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_NO_UI_DEPENDENCY)
    public void coreReportingDoesNotDependOnUiPackages() throws IOException {
        List<String> directUiDependencies = ArchitectureSourceScanner.sourceMatches(
                CORE_ROOT,
                Pattern.compile("com\\.endava\\.ai\\.ui")
        );

        Assert.assertTrue(
                directUiDependencies.isEmpty(),
                "ATF core must not import UI packages directly: " + directUiDependencies
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_TYPED_END_STATUS)
    public void reportLoggerUsesTypedEndStatusAsCanonicalContract() throws IOException {
        String source = ArchitectureSourceScanner.read(REPORT_LOGGER_SOURCE);

        Assert.assertTrue(source.contains("void endTest(TestStatus status);"),
                "ReportLogger must require typed status handling.");
        Assert.assertFalse(source.contains("endTest(String"),
                "ReportLogger must not keep the legacy String status overload.");

        List<String> legacyStringApis = ArchitectureSourceScanner.sourceMatches(
                CORE_ROOT,
                Pattern.compile("void\\s+endTest\\s*\\(\\s*String\\s+status\\s*\\)")
        );

        Assert.assertTrue(legacyStringApis.isEmpty(),
                "Core reporting must not expose legacy endTest(String): " + legacyStringApis);

        List<String> literalStatusCloses = new ArrayList<>();
        literalStatusCloses.addAll(ArchitectureSourceScanner.sourceMatches(
                CORE_ROOT,
                Pattern.compile("\\.endTest\\s*\\(\\s*\"(PASS|FAIL|SKIP|RESET)\"\\s*\\)")
        ));
        literalStatusCloses.addAll(ArchitectureSourceScanner.sourceMatches(
                ATF_TEST_ROOT,
                Pattern.compile("\\.endTest\\s*\\(\\s*\"(PASS|FAIL|SKIP|RESET)\"\\s*\\)")
        ));

        Assert.assertTrue(literalStatusCloses.isEmpty(),
                "Reporting code and fixtures must close tests through TestStatus, not string literals: "
                        + literalStatusCloses);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_MANAGER_SESSION)
    public void reportingManagerDoesNotOwnMutableLoggerState() throws IOException {
        String source = ArchitectureSourceScanner.read(REPORTING_MANAGER_SOURCE);
        String session = ArchitectureSourceScanner.read(REPORTING_SESSION_SOURCE);
        String listener = ArchitectureSourceScanner.read(TEST_LISTENER_SOURCE);

        Assert.assertTrue(source.contains("private static final ReportingSession SESSION"),
                "ReportingManager must delegate state to ReportingSession.");
        Assert.assertFalse(STATIC_REPORT_LOGGER_FIELD.matcher(source).find(),
                "ReportingManager must not reintroduce direct static ReportLogger state.");
        Assert.assertTrue(source.contains("beginSuite()") && source.contains("endSuite()"),
                "ReportingManager must expose explicit suite lifecycle operations.");
        Assert.assertTrue(session.contains("boolean suiteActive") && session.contains("long generation"),
                "ReportingSession must keep observable suite lifecycle state.");
        Assert.assertTrue(ArchitectureSourceScanner.containsReportingLifecycleCall(listener, "beginSuite")
                        && ArchitectureSourceScanner.containsReportingLifecycleCall(listener, "endSuite"),
                "TestListener must route TestNG suite lifecycle into ReportingManager.");
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_COLLAPSE_COMPOSER)
    public void extentSparkCollapsePolicyRemainsComposerOnly() throws IOException {
        String source = ArchitectureSourceScanner.read(EXTENT_SPARK_COLLAPSE_POLICY_SOURCE);
        long lines = ArchitectureSourceScanner.lineCount(EXTENT_SPARK_COLLAPSE_POLICY_SOURCE);

        Assert.assertTrue(lines <= 40,
                "ExtentSparkCollapsePolicy must remain a tiny composer, actual lines=" + lines);
        Assert.assertTrue(source.contains("sections.collapse.ExtentSparkCollapseScriptSections"),
                "Collapse behavior must be delegated to sections/collapse.");
        Assert.assertFalse(source.contains("function normalizeStatusMenu"),
                "Raw collapse JavaScript must not return to the public policy class.");
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_NO_IGNORED_CATCHES)
    public void coreDoesNotUseIgnoredCatchVariables() throws IOException {
        List<String> ignoredCatches = ArchitectureSourceScanner.sourceMatches(
                CORE_ROOT,
                Pattern.compile("catch\\s*\\([^)]*\\bignored\\b[^)]*\\)")
        );

        Assert.assertTrue(ignoredCatches.isEmpty(),
                "Core must not hide caught exceptions behind ignored variables: " + ignoredCatches);
    }

    @Test
    public void coreLombokUsageStaysBroadDisciplinedAndImmutable() throws IOException {
        List<String> broadLombokShortcuts = new ArrayList<>();
        broadLombokShortcuts.addAll(ArchitectureSourceScanner.sourceMatches(
                CORE_ROOT,
                BROAD_LOMBOK_MUTABILITY_ANNOTATION
        ));
        broadLombokShortcuts.addAll(ArchitectureSourceScanner.sourceMatches(
                CORE_ROOT,
                PUBLIC_LOMBOK_ALL_ARGS_CONSTRUCTOR
        ));
        broadLombokShortcuts.addAll(ArchitectureSourceScanner.sourceMatches(
                CORE_ROOT,
                DISALLOWED_LOMBOK_IMPORT
        ));

        Assert.assertTrue(broadLombokShortcuts.isEmpty(),
                "Core may use Lombok to reduce boilerplate in immutable/spec/DTO models; avoid broad mutable shortcuts: "
                        + broadLombokShortcuts);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_DIAGNOSTIC_SINK)
    public void nonBlockingDiagnosticsUseSinkBoundary() throws IOException {
        String source = ArchitectureSourceScanner.read(NON_BLOCKING_DIAGNOSTIC_SOURCE);

        Assert.assertTrue(source.contains("interface Sink"),
                "NonBlockingDiagnostic must expose a sink boundary for tests and reporting integration.");
        Assert.assertTrue(source.contains("sink.publish(warning)"),
                "Warnings must be routed through the configured sink.");
        Assert.assertTrue(source.contains("setSinkForTests"),
                "Tests must be able to replace the diagnostic sink without touching System.err.");
    }

    private static List<AtfCoreYamlSizeBudgetSpec> sizeBudgets(String kind) {
        return AtfCoreYamlContractCatalog.sizeBudgets().stream()
                .filter(budget -> kind.equals(budget.kind()))
                .collect(Collectors.toUnmodifiableList());
    }

    private static AtfCoreYamlSizeBudgetSpec singleSizeBudget(String kind) {
        List<AtfCoreYamlSizeBudgetSpec> budgets = sizeBudgets(kind);
        Assert.assertEquals(budgets.size(), 1, "Expected exactly one size budget of kind " + kind);
        return budgets.get(0);
    }

    private static List<AtfCoreYamlSizeBudgetSpec> packageCapOverrides() {
        return AtfCoreYamlContractCatalog.sizeBudgets().stream()
                .filter(budget -> "legacy-package-cap".equals(budget.kind()) || "package-cap".equals(budget.kind()))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Path> reportingOwnership(String kind) {
        return AtfCoreYamlContractCatalog.reportingOwnership().stream()
                .filter(entry -> kind.equals(entry.kind()))
                .map(AtfCoreYamlReportingOwnershipSpec::asPath)
                .collect(Collectors.toUnmodifiableList());
    }

    private static String required(java.util.Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, REPORTING_PACKAGE_SHAPE_RESOURCE);
    }

}
