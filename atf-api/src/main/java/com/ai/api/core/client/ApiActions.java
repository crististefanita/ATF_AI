package com.ai.api.core.client;

import io.restassured.response.Response;

public final class ApiActions {
    private static final ApiRequestExecutor EXECUTOR = new ApiRequestExecutor(
            new ApiRateLimitPolicy(),
            new ApiExecutionReporter(),
            new ApiRetrySleeper()
    );

    private ApiActions() {
    }

    public static Response execute(
            String method,
            String fullUrl,
            String relativePath,
            String requestBodyJson,
            ApiCall call) {

        ApiExecutionRequest request = new ApiExecutionRequest(method, fullUrl, relativePath, requestBodyJson, call);
        ApiExecutionReporter reporter = new ApiExecutionReporter();
        reporter.start(request);
        try {
            ApiExecutionResult result = EXECUTOR.execute(request, ApiRuntimeOptions.load());
            reporter.pass();
            return result.response();

        } catch (Exception e) {
            reporter.fail(e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface ApiCall {
        Response run();
    }
}
