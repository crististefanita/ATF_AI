package com.ai.api.core.validation;

import com.ai.api.core.validation.model.ApiErrorMessage;
import com.ai.core.reporting.StepLogger;
import io.restassured.response.Response;
import org.testng.Assert;

final class ErrorPayloadAssertions {
    private ErrorPayloadAssertions() {
    }

    static void messageEquals(Response resp, String expectedMessage) {
        ResponseValidationStep.run("Validate: message=" + expectedMessage, () -> {
            ApiErrorMessage actual = ResponseJsonAssertions.errorMessage(resp);
            StepLogger.logDetail("actual=" + actual.message);
            Assert.assertEquals(actual.message, expectedMessage, "Unexpected error message");
        });
    }

    static void errorFieldContains(Response resp, String fieldName, String expectedMessageFragment) {
        ResponseValidationStep.run("Validate: error " + fieldName + "~=" + expectedMessageFragment,
                () -> ResponseJsonAssertions.assertValidationErrorContains(resp, fieldName, expectedMessageFragment));
    }
}
