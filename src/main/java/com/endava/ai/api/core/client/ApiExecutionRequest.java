package com.endava.ai.api.core.client;

import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class ApiExecutionRequest {
    private final String method;
    private final String fullUrl;
    private final String relativePath;
    private final String requestBodyJson;
    private final ApiActions.ApiCall call;

    public ApiExecutionRequest(
            String method,
            String fullUrl,
            String relativePath,
            String requestBodyJson,
            ApiActions.ApiCall call
    ) {
        this.method = method == null ? "" : method;
        this.fullUrl = fullUrl == null ? "" : fullUrl;
        this.relativePath = relativePath == null ? "" : relativePath;
        this.requestBodyJson = requestBodyJson;
        this.call = Objects.requireNonNull(call, "call");
    }

}
