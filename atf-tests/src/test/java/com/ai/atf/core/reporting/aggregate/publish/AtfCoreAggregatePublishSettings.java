package com.ai.atf.core.reporting.aggregate.publish;

import com.ai.atf.core.reporting.aggregate.AtfCoreAggregateArtifacts;
import com.ai.atf.core.reporting.config.AtfCoreAggregateConfig;

import java.nio.file.Path;

final class AtfCoreAggregatePublishSettings {
    private final boolean enabled;
    private final Path directory;

    private AtfCoreAggregatePublishSettings(boolean enabled, Path directory) {
        this.enabled = enabled;
        this.directory = directory.toAbsolutePath().normalize();
    }

    static AtfCoreAggregatePublishSettings fromSystemProperties() {
        String configuredDirectory = System.getProperty(AtfCoreAggregateConfig.PUBLISH_DIR_KEY);
        boolean enabled = Boolean.parseBoolean(System.getProperty(
                AtfCoreAggregateConfig.PUBLISH_ENABLED_KEY,
                AtfCoreAggregateConfig.DEFAULT_DISABLED
        )) || configuredDirectory != null;
        Path directory = configuredDirectory == null || configuredDirectory.isBlank()
                ? AtfCoreAggregateArtifacts.defaultPublishedValidationDir()
                : Path.of(configuredDirectory);
        return new AtfCoreAggregatePublishSettings(enabled, directory);
    }

    boolean enabled() {
        return enabled;
    }

    Path directory() {
        return directory;
    }
}
