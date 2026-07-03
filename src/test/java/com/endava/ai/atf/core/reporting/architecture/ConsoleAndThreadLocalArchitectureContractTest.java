package com.endava.ai.atf.core.reporting.architecture;

import com.endava.ai.atf.core.reporting.architecture.support.ArchitectureSourceScanner;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.wait.AtfTestWaiter;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.CONSOLE_THREADLOCAL)
@AtfContractRef(AtfContractNarrativeIds.Contract.CONSOLE_THREADLOCAL_CLASS)
@Listeners(TestListener.class)
public class ConsoleAndThreadLocalArchitectureContractTest {
    private static final Path CORE_ROOT = AtfSourcePaths.MAIN_CORE_ROOT;
    private static final Path MAIN_AI_ROOT = AtfSourcePaths.MAIN_AI_ROOT;
    private static final Path ATF_TEST_ROOT = AtfSourcePaths.TEST_ATF_ROOT;
    private static final Path CORE_REPORTING_UTILS = CORE_ROOT.resolve("reporting/utils");
    private static final Path CONSOLE_LOGGER_SOURCE = CORE_REPORTING_UTILS.resolve("ConsoleLogger.java");
    private static final Path STACKTRACE_FORMATTER_SOURCE = CORE_REPORTING_UTILS.resolve("StacktraceFormatter.java");
    private static final Path TEST_LISTENER_SOURCE = CORE_ROOT.resolve("reporting/listener/TestListener.java");
    private static final String TEST_WAITER_SUFFIX = AtfSourcePaths.unixPath(AtfSourcePaths.testJava(AtfTestWaiter.class));
    private static final String CONSOLE_LOGGER_SUFFIX = AtfSourcePaths.unixPath(CONSOLE_LOGGER_SOURCE);
    private static final String THIS_TEST_SUFFIX = atfTestSource(
            "reporting/architecture/ConsoleAndThreadLocalArchitectureContractTest.java"
    );
    private static final String LEGACY_CONSOLE_GATEWAY = "Console" + "Loger";
    private static final String LEGACY_CONSOLE_GATEWAY_SOURCE = LEGACY_CONSOLE_GATEWAY + ".java";
    private static final String CONSOLE_LOGGER_DEFAULT_PUBLISH_TOKEN = "System.out.println(message);";
    private static final String STACKTRACE_ELEMENT_TOKEN = "StackTraceElement";
    private static final String ROOT_CAUSE_DETAIL_TOKEN = "Root cause detail";
    private static final String FAILURE_CHAIN_TOKEN = "Failure chain:";
    private static final String ROOT_CAUSE_DETAIL_LABEL = "Root cause detail:";
    private static final Pattern DIRECT_CONSOLE_PATTERN = Pattern.compile("System\\s*\\.\\s*(out|err)");
    private static final Pattern DIRECT_SLEEP_PATTERN = Pattern.compile("\\bThread\\s*\\.\\s*sleep\\s*\\(");
    private static final Pattern THREAD_LOCAL_PATTERN = Pattern.compile("\\bThreadLocal\\b");
    private static final List<String> THREAD_LOCAL_CLEANUP_TOKENS = List.of(
            ".remove()",
            " void clear(",
            " void reset(",
            " void endTest(",
            " void flush(",
            " void stopStep("
    );
    private static final List<String> EXPECTED_THREAD_LOCAL_OWNERS = List.of(
            coreSource("reporting/adapters/ExtentAdapter.java"),
            coreSource("reporting/adapters/allure/AllureStepBuffer.java"),
            coreSource("reporting/adapters/allure/AllureStepContext.java"),
            coreSource("reporting/adapters/extent/ExtentTestContext.java"),
            coreSource("reporting/attachment/FailureAttachmentRegistry.java"),
            coreSource("reporting/listener/internal/lifecycle/TestInvocationSession.java"),
            coreSource("reporting/listener/internal/step/StepBufferRegistry.java"),
            coreSource("reporting/step/StepExecutionContext.java"),
            coreSource("reporting/step/StepReportDelegate.java"),
            coreSource("reporting/utils/ConsoleLogger.java"),
            coreSource("support/contract/ids/AtfContractNarrationState.java")
    );

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.CONSOLE_THREADLOCAL_GATEWAY)
    public void coreReportingDoesNotWriteDirectlyToConsole() throws IOException {
        List<String> directConsoleUsage = ArchitectureSourceScanner.sourceMatches(
                CORE_ROOT,
                DIRECT_CONSOLE_PATTERN
        ).stream()
                .filter(path -> !isSource(path, CONSOLE_LOGGER_SUFFIX))
                .collect(Collectors.toList());

        String consoleLogger = ArchitectureSourceScanner.read(CONSOLE_LOGGER_SOURCE);
        Assert.assertTrue(consoleLogger.contains(CONSOLE_LOGGER_DEFAULT_PUBLISH_TOKEN),
                "ConsoleLogger is the approved core console gateway and must publish StepLogger messages by default.");
        Assert.assertTrue(directConsoleUsage.isEmpty(),
                "Core must not write directly to console APIs outside ConsoleLogger: " + directConsoleUsage);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.CONSOLE_THREADLOCAL_STACKTRACE_BOUNDARY)
    public void consoleLoggerDoesNotOwnStacktraceFormatting() throws IOException {
        String consoleLogger = ArchitectureSourceScanner.read(CONSOLE_LOGGER_SOURCE);
        String stacktraceFormatter = ArchitectureSourceScanner.read(STACKTRACE_FORMATTER_SOURCE);

        Assert.assertFalse(consoleLogger.contains(STACKTRACE_ELEMENT_TOKEN),
                "ConsoleLogger must not own stacktrace traversal.");
        Assert.assertFalse(consoleLogger.contains(ROOT_CAUSE_DETAIL_TOKEN),
                "ConsoleLogger must not own failure narrative formatting.");
        Assert.assertTrue(stacktraceFormatter.contains(FAILURE_CHAIN_TOKEN),
                "StacktraceFormatter must own the failure-chain narrative.");
        Assert.assertTrue(stacktraceFormatter.contains(ROOT_CAUSE_DETAIL_LABEL),
                "StacktraceFormatter must own the root-cause narrative.");
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.CONSOLE_THREADLOCAL_NAMING)
    public void consoleLoggerIsTheOnlyUsedConsoleGatewayName() throws IOException {
        Assert.assertFalse(
                java.nio.file.Files.exists(CORE_REPORTING_UTILS.resolve(LEGACY_CONSOLE_GATEWAY_SOURCE)),
                "Legacy console gateway wrapper must be deleted, not kept deprecated."
        );

        List<String> legacyReferences = new ArrayList<>();
        legacyReferences.addAll(ArchitectureSourceScanner.sourceMatches(
                        MAIN_AI_ROOT,
                        Pattern.compile("\\b" + LEGACY_CONSOLE_GATEWAY + "\\b")
                ).stream()
                .collect(Collectors.toList()));
        legacyReferences.addAll(ArchitectureSourceScanner.sourceMatches(
                ATF_TEST_ROOT,
                Pattern.compile("\\b" + LEGACY_CONSOLE_GATEWAY + "\\b")
        ).stream()
                .filter(path -> !isSource(path, THIS_TEST_SUFFIX))
                .collect(Collectors.toList()));

        Assert.assertTrue(legacyReferences.isEmpty(),
                "ATF code must use ConsoleLogger instead of the legacy typo: " + legacyReferences);
    }

    @Test
    public void testContractsUseCentralizedWaiterInsteadOfDirectThreadSleep() throws IOException {
        List<String> directSleeps = ArchitectureSourceScanner.sourceMatches(
                        ATF_TEST_ROOT,
                        DIRECT_SLEEP_PATTERN
                ).stream()
                .filter(path -> !isSource(path, TEST_WAITER_SUFFIX))
                .collect(Collectors.toList());

        Assert.assertTrue(directSleeps.isEmpty(),
                "ATF contract tests must use AtfTestWaiter instead of direct Thread.sleep: " + directSleeps);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.CONSOLE_THREADLOCAL_CLEANUP)
    public void coreThreadLocalOwnersExposeCleanupContract() throws IOException {
        List<String> missingCleanup = new ArrayList<>();
        try (Stream<Path> files = java.nio.file.Files.walk(CORE_ROOT)) {
            files.filter(AtfSourcePaths::isJavaSource)
                    .filter(path -> ArchitectureSourceScanner.readUnchecked(path).contains("ThreadLocal"))
                    .forEach(path -> {
                        String source = ArchitectureSourceScanner.readUnchecked(path);
                        if (!hasCleanupPath(source)) {
                            missingCleanup.add(path.toString());
                        }
                    });
        }

        Assert.assertTrue(missingCleanup.isEmpty(),
                "ThreadLocal owners must expose cleanup lifecycle methods: " + missingCleanup);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.CONSOLE_THREADLOCAL_OWNER_CATALOG)
    public void coreThreadLocalOwnersStayCatalogued() throws IOException {
        List<String> owners = ArchitectureSourceScanner.sourceMatches(
                        CORE_ROOT,
                        THREAD_LOCAL_PATTERN
                ).stream()
                .map(path -> AtfSourcePaths.unixPath(path))
                .collect(Collectors.toList());

        Assert.assertEquals(owners, EXPECTED_THREAD_LOCAL_OWNERS,
                "ThreadLocal owners must stay explicit and reviewed.");
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.CONSOLE_THREADLOCAL_INVOCATION_SESSION)
    public void testInvocationSessionIsClearedAtSuiteStartAndFinish() throws IOException {
        String source = ArchitectureSourceScanner.read(TEST_LISTENER_SOURCE);

        Assert.assertTrue(methodBody(source, "onStart").contains("session.clear")
                        || methodBody(source, "onStart").contains("session::clear"),
                "TestListener.onStart must clear invocation session state before a new suite.");
        Assert.assertTrue(methodBody(source, "onFinish").contains("session.clear")
                        || methodBody(source, "onFinish").contains("session::clear"),
                "TestListener.onFinish must clear invocation session state after publishing suite artifacts.");
    }

    private static boolean hasCleanupPath(String source) {
        return THREAD_LOCAL_CLEANUP_TOKENS.stream().anyMatch(source::contains);
    }

    private static String methodBody(String source, String methodName) {
        int methodIndex = source.indexOf("void " + methodName + "(");
        if (methodIndex < 0) {
            return "";
        }
        int openBrace = source.indexOf('{', methodIndex);
        if (openBrace < 0) {
            return "";
        }
        int depth = 0;
        for (int i = openBrace; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(openBrace, i + 1);
                }
            }
        }
        return source.substring(openBrace);
    }

    private static boolean isSource(String path, String expectedUnixSuffix) {
        return AtfSourcePaths.unixPath(path).endsWith(expectedUnixSuffix);
    }

    private static String coreSource(String relativePath) {
        return AtfSourcePaths.unixPath(CORE_ROOT.resolve(relativePath));
    }

    private static String atfTestSource(String relativePath) {
        return AtfSourcePaths.unixPath(ATF_TEST_ROOT.resolve(relativePath));
    }
}
