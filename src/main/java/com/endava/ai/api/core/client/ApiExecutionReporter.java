package com.endava.ai.api.core.client;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.core.reporting.utils.ConsoleLogger;
import io.restassured.response.Response;

public final class ApiExecutionReporter {
    public void start(ApiExecutionRequest request) {
        StepLogger.startStep(request.method() + " " + request.relativePath());
    }

    public void retryAttempt(int attempt) {
        StepLogger.logDetail("rate-limit.retry.attempt=" + attempt);
    }

    public void retryDelay(ApiRetryDecision decision) {
        StepLogger.logDetail("rate-limit.retry.delay.ms=" + decision.delay().toMillis());
    }

    public void request(ApiExecutionRequest request, ApiRuntimeOptions options) {
        StepLogger.logDetail("method=" + request.method());
        StepLogger.logDetail("url=" + request.fullUrl());
        String bodyJson = request.requestBodyJson();
        if (bodyJson != null && !bodyJson.isBlank()) {
            StepLogger.logDetail("request.body");
            if (options.consoleDetailsEnabled()) {
                ConsoleLogger.console("  - " + bodyJson);
            }
            StepLogger.logCodeBlock(bodyJson);
        }
    }

    public void response(Response response, ApiRuntimeOptions options) {
        StepLogger.logDetail("response.status=" + response.getStatusCode());
        StepLogger.logDetail("response.body");
        String bodyJson = response.getBody().asPrettyString();
        if (options.consoleDetailsEnabled()) {
            ConsoleLogger.console("  - " + bodyJson);
        }
        StepLogger.logCodeBlock(bodyJson);
    }

    public void pass() {
        StepLogger.pass("Call completed");
    }

    public void fail(Exception failure) {
        StepLogger.fail("API call failed", failure);
    }
}
