package com.ai.atf.core.reporting.regression;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.api.core.client.ApiActions;
import com.ai.api.core.client.ApiExecutionReporter;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlPolicySpec;
import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.step.StepConsolePublisher;
import com.ai.core.reporting.utils.ConsoleLogger;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.CONSOLE_SIGNATURE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.CONSOLE_SIGNATURE_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class ConsoleSignatureContractTest {

    private final List<String> console = new ArrayList<>();
    private AtfRuntimeConfigSandbox config;

    @BeforeMethod
    public void captureConsole() {
        StepLogger.clear();
        console.clear();
        ConsoleLogger.setSinkForTests(console::add);
        config = AtfRuntimeConfigSandbox.open()
                .setConfig(UiRuntimeOptions.CONSOLE_DETAILS_ENABLED_KEY, "true");
    }

    @AfterMethod(alwaysRun = true)
    public void resetConsole() {
        StepLogger.clear();
        ConsoleLogger.resetSinkForTests();
        if (config != null) {
            config.close();
            config = null;
        }
    }

    @Test
    public void api_positive_user_tests_keep_baseline_console_signature() {
        logApiCall("POST", "/public/v2/users", "201");
        logApiCall("POST", "/public/v2/users", "201");
        logApiCall("DELETE", "/public/v2/users/123456", "204");

        assertContainsInOrder(
                "\u25B6 POST /public/v2/users",
                "  \u2022 method=POST",
                "  \u2022 url=/public/v2/users",
                "  \u2022 response.status=201",
                "  \u2705 PASS: Call completed",
                "\u25B6 DELETE /public/v2/users/123456",
                "  \u2022 method=DELETE",
                "  \u2022 url=/public/v2/users/123456",
                "  \u2022 response.status=204",
                "  \u2705 PASS: Call completed"
        );
        assertNoFrameworkNarration();
    }

    @Test
    public void ui_registration_tests_keep_baseline_console_signature() {
        StepLogger.startStep("Registration preconditions");
        StepLogger.pass("Ready");
        StepLogger.startStep("Navigate to URL: https://practicesoftwaretesting.com/auth/register");
        StepLogger.logDetail("base.url=https://practicesoftwaretesting.com");
        StepLogger.logDetail("relativePath=/auth/register");
        ConsoleLogger.console("  \u2022 Playwright pages after open: 1");
        StepLogger.logDetail("Wait for URL contains: /auth/register");
        StepLogger.pass("Navigated");

        assertContainsInOrder(
                "\u25B6 Registration preconditions",
                "  \u2705 PASS: Ready",
                "\u25B6 Navigate to URL: https://practicesoftwaretesting.com/auth/register",
                "  \u2022 base.url=https://practicesoftwaretesting.com",
                "  \u2022 relativePath=/auth/register",
                "  \u2022 Playwright pages after open: 1",
                "  \u2022 Wait for URL contains: /auth/register",
                "  \u2705 PASS: Navigated"
        );
        assertNoFrameworkNarration();
    }

    @Test
    public void api_payload_bodies_stay_visible_in_console_and_report_code_blocks() throws Exception {
        String source = Files.readString(AtfSourcePaths.mainJava(ApiExecutionReporter.class));
        String requestJson = "{\"name\":\"John Doe\",\"status\":\"active\"}";
        String responseJson = "{\"id\":123,\"status\":\"active\"}";
        Response response = mock(Response.class, RETURNS_DEEP_STUBS);
        when(response.getStatusCode()).thenReturn(201);
        when(response.getBody().asPrettyString()).thenReturn(responseJson);

        ApiActions.execute(
                "POST",
                "/public/v2/users",
                "/public/v2/users",
                requestJson,
                () -> response
        );

        assertContainsInOrder(
                "\u25B6 POST /public/v2/users",
                "  \u2022 request.body",
                "  - " + requestJson,
                "  \u2022 response.body",
                "  - " + responseJson,
                "  \u2705 PASS: Call completed"
        );
        Assert.assertTrue(source.contains("ConsoleLogger.console(\"  - \" + bodyJson)"),
                "API payload bodies must stay visible in console for live debugging.");
        Assert.assertTrue(source.contains("StepLogger.logCodeBlock(bodyJson)"),
                "API payload bodies must remain available in the structured report code block.");
    }

    @Test
    public void console_test_sink_is_thread_scoped_for_parallel_contract_safety() throws Exception {
        List<String> mainThreadConsole = new ArrayList<>();
        List<String> workerThreadConsole = new ArrayList<>();
        AtomicReference<Throwable> workerFailure = new AtomicReference<>();

        ConsoleLogger.setSinkForTests(mainThreadConsole::add);
        Thread worker = new Thread(() -> {
            try {
                ConsoleLogger.setSinkForTests(workerThreadConsole::add);
                ConsoleLogger.console("worker-only");
            } catch (Throwable t) {
                workerFailure.set(t);
            } finally {
                ConsoleLogger.resetSinkForTests();
            }
        }, "console-sink-contract-worker");

        worker.start();
        worker.join();
        ConsoleLogger.console("main-only");

        Assert.assertNull(workerFailure.get(), "Worker thread console capture must not fail.");
        Assert.assertEquals(workerThreadConsole, List.of("worker-only"),
                "Worker thread must capture only its own console messages.");
        Assert.assertEquals(mainThreadConsole, List.of("main-only"),
                "Main thread capture must not receive worker thread messages.");
    }

    @Test
    public void step_console_publisher_requires_execution_context() {
        NullPointerException failure = Assert.expectThrows(
                NullPointerException.class,
                () -> new StepConsolePublisher(null)
        );

        Assert.assertTrue(failure.getMessage().contains("context"),
                "Step console publisher should fail fast with a clear dependency name.");
    }

    private void logApiCall(String method, String path, String status) {
        StepLogger.startStep(method + " " + path);
        StepLogger.logDetail("method=" + method);
        StepLogger.logDetail("url=" + path);
        StepLogger.logDetail("response.status=" + status);
        StepLogger.pass("Call completed");
    }

    private void assertContainsInOrder(String... expectedMessages) {
        int cursor = 0;
        for (String expected : expectedMessages) {
            int index = console.subList(cursor, console.size()).indexOf(expected);
            Assert.assertTrue(index >= 0,
                    "Console output must contain message after index " + cursor + ": " + expected
                            + "\nActual console:\n" + String.join("\n", console));
            cursor += index + 1;
        }
    }

    private void assertNoFrameworkNarration() {
        List<String> forbidden = AtfCoreYamlContractCatalog.consoleSignatureContracts().stream()
                .filter(spec -> "forbidden-fragment".equals(spec.kind()))
                .map(AtfCoreYamlPolicySpec::fragment)
                .collect(Collectors.toUnmodifiableList());
        for (String message : console) {
            for (String forbiddenFragment : forbidden) {
                Assert.assertFalse(message.contains(forbiddenFragment),
                        "Baseline console must not include framework-only narration: " + message);
            }
        }
    }
}
