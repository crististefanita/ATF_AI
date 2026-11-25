package com.ai.atf.core.reporting.config;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.config.TestExecutionConfigKeys;

import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtfCoreAggregateConfig {
    public static final String SUREFIRE_SUITE_XML_FILES_KEY = TestExecutionConfigKeys.SUREFIRE_SUITE_XML_FILES;
    public static final String FORK_COUNT_KEY = TestExecutionConfigKeys.FORK_COUNT;
    public static final String ENABLED_KEY = "atf.core.aggregate.report.enabled";
    public static final String ENGINE_KEY = "atf.core.aggregate.report.engine";
    public static final String ROOT_KEY = "atf.core.aggregate.report.root";
    public static final String CLEAN_KEY = "atf.core.aggregate.report.clean";
    public static final String PUBLISH_ENABLED_KEY = "atf.core.aggregate.report.publish.enabled";
    public static final String PUBLISH_DIR_KEY = "atf.core.aggregate.report.publish.dir";

    public static final Path DEFAULT_ROOT_PATH = AtfSourcePaths.TARGET_ROOT.resolve("atf-core-aggregate-report");
    public static final String DEFAULT_ROOT = AtfSourcePaths.unixPath(DEFAULT_ROOT_PATH);
    public static final String DEFAULT_ENGINE = "both";
    public static final String DEFAULT_CLEAN = "true";
    public static final String DEFAULT_DISABLED = "false";

    private final AtfCoreAggregateEngine engine;
    private final Path root;
    private final boolean clean;

    public static boolean enabled() {
        return Boolean.parseBoolean(System.getProperty(ENABLED_KEY, DEFAULT_DISABLED));
    }

    public static String engineName() {
        return System.getProperty(ENGINE_KEY, DEFAULT_ENGINE);
    }

    public static AtfCoreAggregateConfig fromSystemProperties() {
        return new AtfCoreAggregateConfig(
                AtfCoreAggregateEngine.parse(System.getProperty(ENGINE_KEY, DEFAULT_ENGINE)),
                Path.of(System.getProperty(ROOT_KEY, DEFAULT_ROOT)).toAbsolutePath(),
                Boolean.parseBoolean(System.getProperty(CLEAN_KEY, DEFAULT_CLEAN))
        );
    }
}
