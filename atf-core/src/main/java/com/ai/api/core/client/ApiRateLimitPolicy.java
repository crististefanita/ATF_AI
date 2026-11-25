package com.ai.api.core.client;

import io.restassured.response.Response;

public final class ApiRateLimitPolicy {
    public ApiRetryDecision decide(Response response, int attempt, ApiRuntimeOptions options) {
        if (response.getStatusCode() != 429) {
            return ApiRetryDecision.complete("HTTP status is not rate limited");
        }
        if (attempt >= options.maxRateLimitRetries()) {
            return ApiRetryDecision.complete("Rate limit retry budget exhausted");
        }
        return ApiRetryDecision.retry(
                RetryAfterParser.parse(response, options),
                "HTTP 429 rate limit response"
        );
    }
}
