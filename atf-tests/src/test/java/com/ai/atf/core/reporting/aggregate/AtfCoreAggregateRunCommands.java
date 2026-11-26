package com.ai.atf.core.reporting.aggregate;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.atf.core.reporting.config.AtfCoreAggregateConfig;
import com.ai.atf.core.reporting.config.AtfCoreAggregateEngine;
import com.ai.core.support.config.ReportingConfigKeys;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class AtfCoreAggregateRunCommands {
    private static final String CORE_SUITE_FILE = "framework-aggregate" + AtfSourcePaths.XML_EXTENSION;

    public static final String CORE_SUITE_XML = "src/test/resources/suites/" + CORE_SUITE_FILE;
    public static final String FORK_COUNT_ZERO = "0";
    public static final String SUREFIRE_TEST_FILTER_KEY = "test";

    private AtfCoreAggregateRunCommands() {
    }

    public static String aggregateEngine(AtfCoreAggregateEngine engine) {
        return mavenTest(
                resetSurefireTestFilterProperty(),
                property(AtfCoreAggregateConfig.SUREFIRE_SUITE_XML_FILES_KEY, CORE_SUITE_XML),
                property(AtfCoreAggregateConfig.FORK_COUNT_KEY, FORK_COUNT_ZERO),
                property(AtfCoreAggregateConfig.ENABLED_KEY, Boolean.TRUE.toString()),
                property(AtfCoreAggregateConfig.ENGINE_KEY, engine.configValue())
        );
    }

    public static String resetSurefireTestFilterProperty() {
        return property(SUREFIRE_TEST_FILTER_KEY, "");
    }



    public static String mavenQuietTest(String... properties) {
        return maven("mvn -q -pl atf-tests -am test", properties);
    }

    public static String mavenTest(String... properties) {
        return maven("mvn -pl atf-tests -am test", properties);
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
