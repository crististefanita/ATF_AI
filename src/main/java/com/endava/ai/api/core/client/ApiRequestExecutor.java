package com.endava.ai.api.core.client;

import io.restassured.response.Response;

public final class ApiRequestExecutor {
    private final ApiRateLimitPolicy retryPolicy;
    private final ApiExecutionReporter reporter;
    private final ApiRetrySleeper sleeper;

    public ApiRequestExecutor(ApiRateLimitPolicy retryPolicy, ApiExecutionReporter reporter, ApiRetrySleeper sleeper) {
        this.retryPolicy = retryPolicy;
        this.reporter = reporter;
        this.sleeper = sleeper;
    }

    public ApiExecutionResult execute(ApiExecutionRequest request, ApiRuntimeOptions options) {
        Response response = null;
        int attempts = 0;
        for (int attempt = 0; attempt <= options.maxRateLimitRetries(); attempt++) {
            attempts = attempt + 1;
            if (attempt > 0) {
                reporter.retryAttempt(attempt);
            }
            reporter.request(request, options);
            response = request.call().run();
            reporter.response(response, options);

            ApiRetryDecision decision = retryPolicy.decide(response, attempt, options);
            if (!decision.retry()) {
                return ApiExecutionResult.completed(response, attempts);
            }

            reporter.retryDelay(decision);
            sleeper.sleep(decision);
        }
        return ApiExecutionResult.completed(response, attempts);
    }
}
