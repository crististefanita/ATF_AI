package com.ai.api.core.validation;

import io.restassured.response.Response;

import java.time.Duration;
import java.util.function.Supplier;

final class PollingAssertions {
    private PollingAssertions() {
    }

    static Response waitForStatus(Supplier<Response> call, int expected) {
        Response resp = com.ai.api.core.utils.WaitUtils.waitUntil(
                call,
                r -> r.getStatusCode() == expected,
                Duration.ofMillis(500)
        );
        StatusAssertions.statusIs(resp, expected);
        return resp;
    }
}
