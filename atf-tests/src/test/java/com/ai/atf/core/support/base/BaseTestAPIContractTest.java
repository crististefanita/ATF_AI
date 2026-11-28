package com.ai.atf.core.support.base;

import com.ai.api.core.BaseTestAPI;
import com.ai.api.core.utils.ApiSleeper;
import com.ai.api.core.validation.model.ApiErrorMessage;
import com.ai.api.core.validation.model.ApiValidationError;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.io.IOException;
import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.BASE_API_RUNTIME)
@AtfContractRef(AtfContractNarrativeIds.Contract.BASE_API_RUNTIME_CLASS)
@Listeners(TestListener.class)
public class BaseTestAPIContractTest {

    private static final String AUTH_TOKEN = "auth.token";
    private static final Path MAIN_API_ROOT = AtfSourcePaths.MAIN_API_ROOT;
    private static final Path API_CORE_ROOT = MAIN_API_ROOT.resolve("core");
    private static final Path API_CORE_CLIENT_ROOT = API_CORE_ROOT.resolve("client");
    private static final Path API_CORE_VALIDATION_ROOT = API_CORE_ROOT.resolve("validation");
    private static final Path API_CORE_VALIDATION_MODEL_ROOT = API_CORE_VALIDATION_ROOT.resolve("model");
    private static final Path API_CORE_UTILS_ROOT = API_CORE_ROOT.resolve("utils");
    private static final Path API_SUPPORT_ROOT = AtfSourcePaths.supportJavaPackage("com.ai.api.support");
    private static final String API_CORE_VALIDATION_MODEL_PACKAGE =
            AtfSourcePaths.API_CORE_PACKAGE + ".validation.model";
    private static final List<String> LEGACY_API_ROOT_PACKAGES = List.of(
            "client",
            "factory",
            "model",
            "service",
            "steps",
            "utils",
            "validation"
    );
    private static final List<String> ILLEGAL_SUPPORT_GENERIC_PACKAGES = List.of(
            "client",
            "utils",
            "validation"
    );
    private static final List<String> UI_RUNTIME_MARKERS = List.of(
            "UIEngine",
            "UIEngineFactory",
            "DriverManager",
            "BaseTestUI",
            UiRuntimeOptions.UI_ENGINE_KEY,
            UiRuntimeOptions.UI_HEADLESS_KEY
    );

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.BASE_API_LISTENER_SETUP)
    public void baseTestApiWiresListenersAndClassScopedSetup() throws Exception {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "baseTestApiWiresListenersAndClassScopedSetup"
        );
        AtfContractSteps.expected(contract);

        Listeners listeners = BaseTestAPI.class.getAnnotation(Listeners.class);
        Assert.assertNotNull(listeners, "BaseTestAPI must declare TestNG listeners");
        Assert.assertEquals(Arrays.asList(listeners.value()), List.of(TestListener.class));
        Assert.assertTrue(Arrays.asList(listeners.value()).contains(TestListener.class));

        Method baseSetup = BaseTestAPI.class.getDeclaredMethod("baseSetup");
        BeforeClass beforeClass = baseSetup.getAnnotation(BeforeClass.class);
        Assert.assertNotNull(beforeClass, "baseSetup must be @BeforeClass");
        Assert.assertTrue(beforeClass.alwaysRun(), "baseSetup must always run");

        AtfContractSteps.observed("@Listeners contains the Core TestListener facade; Extent is reached through the facade | baseSetup=@BeforeClass(alwaysRun=true)");
        AtfContractSteps.verdict(
                "API tests inherit reporting and runtime readiness from one deterministic base hook.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.BASE_API_AUTH_READINESS)
    public void baseTestApiRequiresTokenForWriteFlowsWithoutHiddenSideEffects() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "baseTestApiRequiresTokenForWriteFlowsWithoutHiddenSideEffects"
        );
        AtfContractSteps.expected(contract);

        ExposedBaseTestAPI base = new ExposedBaseTestAPI();

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()) {
            ignored.setConfig(AUTH_TOKEN, "");
            SkipException skip = Assert.expectThrows(SkipException.class, base::requireToken);
            Assert.assertTrue(skip.getMessage().contains("auth.token"));

            ignored.setConfig(AUTH_TOKEN, "token-for-contract");
            base.requireToken();
        }

        AtfContractSteps.observed("blank auth.token -> SkipException | non-blank auth.token -> write flow allowed");
        AtfContractSteps.verdict(
                "API write readiness is explicit and recoverable in reports.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.BASE_API_NO_UI_RUNTIME)
    public void apiCoreDoesNotInitializeUiEngineRuntime() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "apiCoreDoesNotInitializeUiEngineRuntime"
        );
        AtfContractSteps.expected(contract);

        List<Path> scannedRoots = List.of(
                AtfSourcePaths.MAIN_API_ROOT,
                AtfSourcePaths.TEST_ATF_API_ROOT,
                AtfSourcePaths.supportJavaPackage("com.ai.atf.api")
        );
        List<String> violations = scannedRoots.stream()
                .flatMap(BaseTestAPIContractTest::javaFiles)
                .flatMap(BaseTestAPIContractTest::uiRuntimeViolations)
                .collect(Collectors.toList());

        Assert.assertTrue(violations.isEmpty(), "API core must not reference UI runtime: " + violations);

        AtfContractSteps.observed("scanned.roots=" + scannedRoots + " | forbidden.markers=" + UI_RUNTIME_MARKERS);
        AtfContractSteps.verdict(
                "API core remains HTTP/API-only and does not initialize UI engine runtime.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.BASE_API_PACKAGE_ARCHITECTURE)
    public void apiMainPackagesSeparateRuntimeFoundationFromTestSupport() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "apiMainPackagesSeparateRuntimeFoundationFromTestSupport"
        );
        AtfContractSteps.expected(contract);

        List<Path> legacyPaths = LEGACY_API_ROOT_PACKAGES.stream()
                .map(MAIN_API_ROOT::resolve)
                .filter(Files::exists)
                .collect(Collectors.toList());
        List<Path> genericSupportPaths = ILLEGAL_SUPPORT_GENERIC_PACKAGES.stream()
                .map(API_SUPPORT_ROOT::resolve)
                .filter(Files::exists)
                .collect(Collectors.toList());

        Assert.assertTrue(Files.isDirectory(API_CORE_ROOT), "Missing API core root: " + API_CORE_ROOT);
        Assert.assertTrue(Files.isDirectory(API_CORE_CLIENT_ROOT), "Missing generic API client root: " + API_CORE_CLIENT_ROOT);
        Assert.assertTrue(Files.isDirectory(API_CORE_VALIDATION_ROOT), "Missing generic API validation root: " + API_CORE_VALIDATION_ROOT);
        Assert.assertTrue(Files.isDirectory(API_CORE_UTILS_ROOT), "Missing generic API utils root: " + API_CORE_UTILS_ROOT);
        Assert.assertTrue(Files.isDirectory(API_SUPPORT_ROOT), "Missing API support root: " + API_SUPPORT_ROOT);
        Assert.assertTrue(
                legacyPaths.isEmpty(),
                "Legacy root-level API support packages must move under api.support: " + legacyPaths
        );
        Assert.assertTrue(
                genericSupportPaths.isEmpty(),
                "Generic reusable API packages must live under api.core, not api.support: " + genericSupportPaths
        );

        AtfContractSteps.observed("core.root=" + API_CORE_ROOT
                + " | core.client.root=" + API_CORE_CLIENT_ROOT
                + " | core.validation.root=" + API_CORE_VALIDATION_ROOT
                + " | core.utils.root=" + API_CORE_UTILS_ROOT
                + " | support.root=" + API_SUPPORT_ROOT
                + " | legacy.paths=" + legacyPaths
                + " | generic.support.paths=" + genericSupportPaths);
        AtfContractSteps.verdict(
                "API package ownership is explicit: generic HTTP framework pieces stay core, current domain flows stay support.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.BASE_API_CONSOLE_HYGIENE)
    public void apiCoreDoesNotWriteDirectlyToSystemOut() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "apiCoreDoesNotWriteDirectlyToSystemOut"
        );
        AtfContractSteps.expected(contract);

        List<String> violations = javaFiles(API_CORE_ROOT)
                .filter(path -> sourceContains(path, "System.out"))
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());

        Assert.assertTrue(violations.isEmpty(), "API core must not write directly to System.out: " + violations);

        AtfContractSteps.observed("api.core.system.out.violations=" + violations);
        AtfContractSteps.verdict(
                "API core diagnostics are routed through framework reporting utilities.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.BASE_API_WAIT_CENTRALIZATION)
    public void apiCoreCentralizesThreadSleepUsage() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "apiCoreCentralizesThreadSleepUsage"
        );
        AtfContractSteps.expected(contract);

        List<String> violations = javaFiles(API_CORE_ROOT)
                .filter(path -> !path.endsWith("ApiSleeper.java"))
                .filter(path -> sourceContains(path, "Thread.sleep"))
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());

        Assert.assertTrue(violations.isEmpty(), "API core waits must use ApiSleeper: " + violations);

        AtfContractSteps.observed("api.core.direct.thread.sleep.violations=" + violations);
        AtfContractSteps.verdict(
                "API core waits now share one interrupt policy.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.BASE_API_WAIT_INTERRUPT_POLICY)
    public void apiSleeperPreservesWaitPolicy() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "apiSleeperPreservesWaitPolicy"
        );
        AtfContractSteps.expected(contract);

        Assert.expectThrows(
                IllegalArgumentException.class,
                () -> ApiSleeper.sleep(Duration.ofMillis(-1), "negative duration")
        );

        try {
            Thread.currentThread().interrupt();
            Assert.expectThrows(
                    IllegalStateException.class,
                    () -> ApiSleeper.sleep(Duration.ZERO, "interrupted api wait")
            );
            Assert.assertTrue(Thread.currentThread().isInterrupted(), "ApiSleeper must restore interrupted status");
        } finally {
            Thread.interrupted();
        }

        AtfContractSteps.observed("negative.duration=rejected | interrupted.status=preserved");
        AtfContractSteps.verdict(
                "API wait behavior is explicit, reusable, and test-covered.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.BASE_API_VALIDATION_MODEL_BOUNDARY)
    public void apiCoreValidationModelsUseCorePackageNamespace() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "apiCoreValidationModelsUseCorePackageNamespace"
        );
        AtfContractSteps.expected(contract);

        Path errorModel = API_CORE_VALIDATION_MODEL_ROOT.resolve("ApiErrorMessage.java");
        Path validationModel = API_CORE_VALIDATION_MODEL_ROOT.resolve("ApiValidationError.java");
        Path responseValidator = API_CORE_VALIDATION_ROOT.resolve("ResponseValidator.java");

        Assert.assertEquals(declaredPackage(errorModel), API_CORE_VALIDATION_MODEL_PACKAGE);
        Assert.assertEquals(declaredPackage(validationModel), API_CORE_VALIDATION_MODEL_PACKAGE);
        Assert.assertEquals(ApiErrorMessage.class.getPackageName(), API_CORE_VALIDATION_MODEL_PACKAGE);
        Assert.assertEquals(ApiValidationError.class.getPackageName(), API_CORE_VALIDATION_MODEL_PACKAGE);
        Assert.assertFalse(
                Files.readString(responseValidator).contains("com.ai.api.support.model.Api"),
                "ResponseValidator must not import generic validation DTOs from api.support.model"
        );

        AtfContractSteps.observed("validation.model.package=" + API_CORE_VALIDATION_MODEL_PACKAGE
                + " | errorModel=" + errorModel
                + " | validationModel=" + validationModel
                + " | responseValidator=" + responseValidator);
        AtfContractSteps.verdict(
                "API validation models are physically and logically owned by api.core.",
                contract.repairHint()
        );
    }

    private static final class ExposedBaseTestAPI extends BaseTestAPI {
        private void requireToken() {
            requireTokenForWrites();
        }
    }

    private static Stream<Path> javaFiles(Path root) {
        try {
            if (!Files.exists(root)) {
                return Stream.empty();
            }
            return Files.walk(root)
                    .filter(Files::isRegularFile)
                    .filter(AtfSourcePaths::isJavaSource);
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading " + root, e);
        }
    }

    private static Stream<String> uiRuntimeViolations(Path file) {
        try {
            String source = Files.readString(file);
            return UI_RUNTIME_MARKERS.stream()
                    .filter(source::contains)
                    .map(marker -> file + " -> " + marker);
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading " + file, e);
        }
    }

    private static boolean sourceContains(Path file, String marker) {
        try {
            return Files.readString(file).contains(marker);
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading " + file, e);
        }
    }

    private static String declaredPackage(Path file) throws IOException {
        return Files.readAllLines(file).stream()
                .map(String::trim)
                .filter(line -> line.startsWith("package "))
                .findFirst()
                .map(line -> line.substring("package ".length(), line.length() - 1))
                .orElseThrow(() -> new AssertionError("Missing package declaration in " + file));
    }

}
