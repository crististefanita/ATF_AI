package com.ai.api.core.client;

import io.restassured.response.Response;

import java.time.Duration;

public final class RetryAfterParser {
    private RetryAfterParser() {
    }

    public static Duration parse(Response response, ApiRuntimeOptions options) {
        String retryAfterHeader = firstNonBlank(
                response.getHeader("Retry-After"),
                response.getHeader("retry-after"),
                response.getHeader("x-ratelimit-reset"),
                response.getHeader("X-RateLimit-Reset")
        );
        if (retryAfterHeader == null) {
            return options.defaultRateLimitDelay();
        }
        try {
            long seconds = Long.parseLong(retryAfterHeader.trim());
            if (seconds <= 0) {
                return options.defaultRateLimitDelay();
            }
            Duration parsed = Duration.ofSeconds(seconds);
            return parsed.compareTo(options.maxRateLimitDelay()) > 0
                    ? options.maxRateLimitDelay()
                    : parsed;
        } catch (NumberFormatException unparseable) {
            return options.defaultRateLimitDelay();
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
