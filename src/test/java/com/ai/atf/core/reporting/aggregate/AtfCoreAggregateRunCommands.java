package com.ai.atf.core.reporting.aggregate;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.atf.core.reporting.config.AtfCoreAggregateConfig;
import com.ai.atf.core.reporting.config.AtfCoreAggregateEngine;
import com.ai.core.support.config.ReportingConfigKeys;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class AtfCoreAggregateRunCommands {
    private static final String CORE_SUITE_FILE = "testng-atf-core" + AtfSourcePaths.XML_EXTENSION;
    private static final String ALLURE_SUITE_FILE = "testng-atf-allure" + AtfSourcePaths.XML_EXTENSION;
    private static final String ALLURE_TESTNG_SPI_LISTENER = "io.qameta.allure.testng.AllureTestNg";

    public static final String CORE_SUITE_XML = AtfSourcePaths.unixPath(
            AtfSourcePaths.CORE_TEST_RESOURCES_ROOT.resolve(CORE_SUITE_FILE));
    public static final String ALLURE_SUITE_XML = AtfSourcePaths.unixPath(
            AtfSourcePaths.CORE_TEST_RESOURCES_ROOT.resolve(ALLURE_SUITE_FILE));
    public static final String FORK_COUNT_ZERO = "0";
    public static final String SUREFIRE_TEST_FILTER_KEY = "test";
    public static final String SPI_LISTENERS_TO_SKIP_KEY = "spilistenerstoskip";

    private AtfCoreAggregateRunCommands() {
    }

    public static String aggregateEngine(AtfCoreAggregateEngine engine) {
        return mavenTest(
                resetSurefireTestFilterProperty(),
                property(AtfCoreAggregateConfig.SUREFIRE_SUITE_XML_FILES_KEY, CORE_SUITE_XML),
                property(SPI_LISTENERS_TO_SKIP_KEY, ALLURE_TESTNG_SPI_LISTENER),
                property(AtfCoreAggregateConfig.FORK_COUNT_KEY, FORK_COUNT_ZERO),
                property(AtfCoreAggregateConfig.ENGINE_KEY, engine.configValue())
        );
    }

    public static String resetSurefireTestFilterProperty() {
        return property(SUREFIRE_TEST_FILTER_KEY, "");
    }

    public static String skipAllureSpiListenerProperty() {
        return property(SPI_LISTENERS_TO_SKIP_KEY, ALLURE_TESTNG_SPI_LISTENER);
    }

    public static String allureProfile() {
        return mavenTest(
                resetSurefireTestFilterProperty(),
                property(AtfCoreAggregateConfig.SUREFIRE_SUITE_XML_FILES_KEY, ALLURE_SUITE_XML),
                property(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_ALLURE)
        );
    }

    public static String mavenQuietTest(String... properties) {
        return maven("mvn -q test", properties);
    }

    public static String mavenTest(String... properties) {
        return maven("mvn test", properties);
    }

    public static String property(String key, Object value) {
        return "'-D" + key + "=" + value + "'";
    }

    private static String maven(String prefix, String... properties) {
        if (properties == null || properties.length == 0) {
            return prefix;
        }
        return prefix + " " + Arrays.stream(properties).collect(Collectors.joining(" "));
    }
}
