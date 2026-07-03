package com.endava.ai.atf.core.reporting.adapters.allure;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.annotations.Listeners;

import com.aventstack.extentreports.ExtentTest;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.adapters.AllureAdapter;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.adapters.extent.ExtentTestContext;
import com.endava.ai.core.reporting.internal.ReportingPaths;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.FileSystemResultsWriter;
import io.qameta.allure.model.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EMPTY_PAYLOAD_ATTACHMENT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EMPTY_PAYLOAD_ATTACHMENT_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class EmptyPayloadAttachmentContractTest {

    private AtfRuntimeConfigSandbox config;
    private AllureLifecycle previousLifecycle;

    @BeforeMethod
    public void setUp() {
        config = AtfRuntimeConfigSandbox.open();
        previousLifecycle = Allure.getLifecycle();
    }

    @AfterMethod
    public void tearDown() {
        if (config != null) {
            config.close();
            config = null;
        }

        if (previousLifecycle != null) {
            Allure.setLifecycle(previousLifecycle);
        }
    }

    @Test
    public void allure_does_not_attach_empty_payloads() throws Exception {
        Path resultsDir = Files.createTempDirectory("atf-allure-empty-payload-contract-");

        try {
            config.setConfig(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_ALLURE)
                    .setSystemProperty(ReportingPaths.ALLURE_RESULTS_DIRECTORY_PROPERTY, resultsDir.toString());
            Allure.setLifecycle(new AllureLifecycle(new FileSystemResultsWriter(resultsDir)));

            String uuid = UUID.randomUUID().toString();
            Allure.getLifecycle().scheduleTestCase(
                    new io.qameta.allure.model.TestResult()
                            .setUuid(uuid)
                            .setName("empty-payload-allure")
            );
            Allure.getLifecycle().startTestCase(uuid);

            AllureAdapter adapter = AllureAdapter.getInstance();
            adapter.startTest("empty-payload-allure", "contract");
            adapter.startStep("api step");
            adapter.logCodeBlock("");
            adapter.pass("");
            adapter.endTest(TestStatus.PASS);

            Allure.getLifecycle().updateTestCase(uuid, result -> result.setStatus(Status.PASSED));
            Allure.getLifecycle().stopTestCase(uuid);
            Allure.getLifecycle().writeTestCase(uuid);

            Assert.assertEquals(
                    countOccurrences(resultsDir, "\"name\":\"Payload\""),
                    0,
                    "Allure must not attach an empty payload"
            );
        } finally {
            deleteRecursively(resultsDir);
        }
    }

    @Test
    public void extent_does_not_log_empty_payload_code_blocks() throws Exception {
        config.setConfig(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT);

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest("empty-payload-extent", "contract");
        adapter.startStep("api step");
        adapter.logCodeBlock("");
        adapter.pass("");

        ExtentTest extentTest = currentExtentTest(adapter);
        Assert.assertNotNull(extentTest, "Expected active Extent test context");

        com.aventstack.extentreports.model.Test model = extentTest.getModel();
        Assert.assertEquals(model.getChildren().size(), 1, "Expected a single step node");
        Assert.assertTrue(
                model.getChildren().get(0).getLogs().isEmpty(),
                "Extent must not log an empty payload as a code block"
        );

        adapter.endTest(TestStatus.PASS);
        adapter.flush();
    }

    private static ExtentTest currentExtentTest(ExtentAdapter adapter) throws ReflectiveOperationException {
        Field field = ExtentAdapter.class.getDeclaredField("testContext");
        field.setAccessible(true);
        ExtentTestContext context = (ExtentTestContext) field.get(adapter);
        return context.get();
    }

    private static int countOccurrences(Path root, String needle) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(EmptyPayloadAttachmentContractTest::readQuietly)
                    .mapToInt(content -> content.contains(needle) ? 1 : 0)
                    .sum();
        }
    }

    private static String readQuietly(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException ignored) {
            return "";
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(root)) {
            stream.sorted(Comparator.comparingInt(Path::getNameCount).reversed())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("Failed to delete " + path, e);
                        }
                    });
        }
    }

}
