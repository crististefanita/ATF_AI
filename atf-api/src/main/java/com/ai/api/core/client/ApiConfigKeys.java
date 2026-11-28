package com.ai.api.core.client;

public final class ApiConfigKeys {
    public static final String CONSOLE_DETAILS_ENABLED = "console.details.enabled";
    public static final String MAX_RATE_LIMIT_RETRIES = "api.rate.limit.max.retries";
    public static final String DEFAULT_RATE_LIMIT_DELAY_SECONDS = "api.rate.limit.retry.default.delay.seconds";
    public static final String MAX_RATE_LIMIT_DELAY_SECONDS = "api.rate.limit.retry.max.delay.seconds";

    private ApiConfigKeys() {
    }
}
