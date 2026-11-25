package com.ai.atf.core.reporting.support;

import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.adapters.AllureAdapter;
import com.ai.core.reporting.internal.ReportingPaths;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.FileSystemResultsWriter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.TestResult;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

public final class AllureResultFixture implements AutoCloseable {

    public static final String ALLURE_FIXTURE_ROOT_PROPERTY = "atf.allure.fixture.root";
    public static final String ALLURE_RESULT_JSON_SUFFIX = "-result.json";
    public static final Path DEFAULT_ALLURE_FIXTURE_ROOT = Path.of(".codex-artifacts", "allure-contract-fixtures");

    @Getter
    @Accessors(fluent = true)
    private final AtfRuntimeConfigSandbox config;
    private final AllureLifecycle previousLifecycle;
    @Getter
    @Accessors(fluent = true)
    private final Path resultsDir;

    private AllureResultFixture(
            AtfRuntimeConfigSandbox config,
            AllureLifecycle previousLifecycle,
            Path resultsDir
    ) {
        this.config = config;
        this.previousLifecycle = previousLifecycle;
        this.resultsDir = resultsDir;
    }

    public static AllureResultFixture open(String fixtureName) throws IOException {
        Path resultsDir = fixtureRoot().resolve(fixtureName).toAbsolutePath();
        deleteRecursively(resultsDir);
        Files.createDirectories(resultsDir);

        AtfRuntimeConfigSandbox config = AtfRuntimeConfigSandbox.open()
                .setConfig(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_ALLURE)
                .setSystemProperty(ReportingPaths.ALLURE_RESULTS_DIRECTORY_PROPERTY, resultsDir.toString());

        AllureLifecycle previousLifecycle = Allure.getLifecycle();
        Allure.setLifecycle(new AllureLifecycle(new FileSystemResultsWriter(resultsDir)));

        return new AllureResultFixture(config, previousLifecycle, resultsDir);
    }

    public static Path fixtureRoot() {
        return Path.of(System.getProperty(
                        ALLURE_FIXTURE_ROOT_PROPERTY,
                        DEFAULT_ALLURE_FIXTURE_ROOT.toString()
                ))
                .toAbsolutePath();
    }

    public AllureAdapter adapter() {
        return AllureAdapter.getInstance();
    }

    public String startTestCase(String testName, String description) {
        String uuid = startRawTestCase(testName);
        adapter().startTest(testName, description);
        return uuid;
    }

    public String startRawTestCase(String testName) {
        String uuid = UUID.randomUUID().toString();
        Allure.getLifecycle().scheduleTestCase(new TestResult()
                .setUuid(uuid)
                .setName(testName));
        Allure.getLifecycle().startTestCase(uuid);
        return uuid;
    }

    public void writePassedTestCase(String uuid) {
        writeTestCase(uuid, Status.PASSED);
    }

    public void writeTestCase(String uuid, Status status) {
        Allure.getLifecycle().updateTestCase(uuid, result -> result.setStatus(status));
        Allure.getLifecycle().stopTestCase(uuid);
        Allure.getLifecycle().writeTestCase(uuid);
    }

    public JsonObject resultJson(String testName) throws IOException {
        try (Stream<Path> stream = Files.walk(resultsDir)) {
            return stream
                    .filter(path -> path.getFileName().toString().endsWith(ALLURE_RESULT_JSON_SUFFIX))
                    .map(AllureResultFixture::readQuietly)
                    .map(JsonParser::parseString)
                    .map(JsonElement::getAsJsonObject)
                    .filter(json -> testName.equals(string(json, "name")))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Missing Allure result JSON for " + testName));
        }
    }

    public int countOccurrences(String needle) throws IOException {
        try (Stream<Path> stream = Files.walk(resultsDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(AllureResultFixture::readQuietly)
                    .mapToInt(content -> content.contains(needle) ? 1 : 0)
                    .sum();
        }
    }

    public long countFiles(String suffix) throws IOException {
        try (Stream<Path> stream = Files.walk(resultsDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .count();
        }
    }

    public Path firstFileWithSuffix(String suffix) throws IOException {
        try (Stream<Path> stream = Files.walk(resultsDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No " + suffix + " artifact found in " + resultsDir));
        }
    }

    @Override
    public void close() {
        config.close();
        Allure.setLifecycle(previousLifecycle);
        try {
            deleteRecursively(resultsDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete " + resultsDir, e);
        }
    }

    public static JsonArray parametersForStep(JsonObject result, String stepName) {
        JsonArray steps = array(result, "steps");
        Assert.assertNotNull(steps, "Allure result must contain steps");
        Assert.assertTrue(steps.size() > 0, "Allure result must contain at least one step");
        for (JsonElement element : steps) {
            JsonObject step = element.getAsJsonObject();
            if (stepName.equals(string(step, "name"))) {
                return array(step, "parameters");
            }
        }
        throw new AssertionError("Missing Allure step: " + stepName);
    }

    public static boolean containsParameter(JsonArray parameters, String name, String value) {
        if (parameters == null) {
            return false;
        }
        for (JsonElement element : parameters) {
            JsonObject parameter = element.getAsJsonObject();
            if (name.equals(string(parameter, "name"))
                    && value.equals(string(parameter, "value"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasStep(JsonArray steps, String name) {
        if (steps == null) {
            return false;
        }
        for (JsonElement element : steps) {
            JsonObject step = element.getAsJsonObject();
            if (name.equals(string(step, "name"))) {
                return true;
            }
        }
        return false;
    }

    public static JsonArray array(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element == null || element.isJsonNull() ? null : element.getAsJsonArray();
    }

    public static String string(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }

    private static String readQuietly(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + path, e);
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
