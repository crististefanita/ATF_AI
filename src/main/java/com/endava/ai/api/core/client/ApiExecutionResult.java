package com.endava.ai.api.core.client;

import io.restassured.response.Response;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiExecutionResult {
    private final Response response;
    private final int attempts;

    public static ApiExecutionResult completed(Response response, int attempts) {
        return new ApiExecutionResult(response, attempts);
    }

}
