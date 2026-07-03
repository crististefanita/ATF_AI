package com.ai.api.core.client;

import com.ai.core.support.config.ConfigManager;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.ai.core.support.config.ConfigManager.require;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
public final class ApiRuntimeOptions {
    private static final int DEFAULT_MAX_RATE_LIMIT_RETRIES = 2;
    private static final long DEFAULT_RATE_LIMIT_DELAY_SECONDS = 5L;
    private static final long DEFAULT_MAX_RATE_LIMIT_DELAY_SECONDS = 65L;

    private final int maxRateLimitRetries;
    private final Duration defaultRateLimitDelay;
    private final Duration maxRateLimitDelay;
    private final boolean consoleDetailsEnabled;

    public static ApiRuntimeOptions load() {
        return new ApiRuntimeOptions(
                configuredInt(ApiConfigKeys.MAX_RATE_LIMIT_RETRIES, DEFAULT_MAX_RATE_LIMIT_RETRIES),
                configuredDuration(
                        ApiConfigKeys.DEFAULT_RATE_LIMIT_DELAY_SECONDS,
                        DEFAULT_RATE_LIMIT_DELAY_SECONDS
                ),
                configuredDuration(
                        ApiConfigKeys.MAX_RATE_LIMIT_DELAY_SECONDS,
                        DEFAULT_MAX_RATE_LIMIT_DELAY_SECONDS
                ),
                Boolean.parseBoolean(require(ApiConfigKeys.CONSOLE_DETAILS_ENABLED))
        );
    }

    public static ApiRuntimeOptions of(
            int maxRateLimitRetries,
            Duration defaultRateLimitDelay,
            Duration maxRateLimitDelay,
            boolean consoleDetailsEnabled
    ) {
        return new ApiRuntimeOptions(
                maxRateLimitRetries,
                defaultRateLimitDelay,
                maxRateLimitDelay,
                consoleDetailsEnabled
        );
    }

    private static int configuredInt(String key, int defaultValue) {
        String value = ConfigManager.get(key, Integer.toString(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid integer config key: " + key + "=" + value, e);
        }
    }

    private static Duration configuredDuration(String key, long defaultSeconds) {
        String value = ConfigManager.get(key, Long.toString(defaultSeconds));
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new IllegalStateException("Config key must be > 0: " + key + "=" + value);
            }
            return Duration.ofSeconds(parsed);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid duration config key: " + key + "=" + value, e);
        }
    }
}
