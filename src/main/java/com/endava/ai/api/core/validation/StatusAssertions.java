package com.endava.ai.api.core.validation;

import com.endava.ai.core.reporting.StepLogger;
import io.restassured.response.Response;
import org.testng.Assert;

final class StatusAssertions {
    private StatusAssertions() {
    }

    static void statusIs(Response resp, int expected) {
        ResponseValidationStep.run("Validate: status=" + expected, () -> {
            int actual = resp.getStatusCode();
            StepLogger.logDetail("actual=" + actual);
            Assert.assertEquals(actual, expected, "Unexpected HTTP status");
        });
    }
}
