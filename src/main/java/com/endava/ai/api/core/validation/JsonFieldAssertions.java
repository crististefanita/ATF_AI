package com.endava.ai.api.core.validation;

import com.endava.ai.core.reporting.StepLogger;
import io.restassured.response.Response;
import org.testng.Assert;

final class JsonFieldAssertions {
    private JsonFieldAssertions() {
    }

    static void fieldEquals(Response resp, String jsonPath, Object expectedValue) {
        ResponseValidationStep.run("Validate: " + jsonPath + "=" + expectedValue, () -> {
            Object actual = resp.jsonPath().get(jsonPath);
            StepLogger.logDetail("actual=" + actual);
            Assert.assertEquals(actual, expectedValue, "Unexpected value for path: " + jsonPath);
        });
    }

    static void fieldNotBlank(Response resp, String jsonPath) {
        ResponseValidationStep.run("Validate: " + jsonPath + " is non-blank", () -> {
            Object actual = resp.jsonPath().get(jsonPath);
            StepLogger.logDetail("actual=" + actual);
            Assert.assertNotNull(actual, "Expected non-null value for path: " + jsonPath);
            Assert.assertFalse(actual.toString().trim().isEmpty(), "Expected non-blank value for path: " + jsonPath);
        });
    }

    static void everyFieldEquals(Response resp, String jsonPath, Object expectedValue) {
        ResponseValidationStep.run("Validate: every " + jsonPath + "=" + expectedValue,
                () -> ResponseJsonAssertions.assertEveryFieldEquals(resp, jsonPath, expectedValue));
    }
}
