package com.ai.api.core.validation;

import com.ai.api.core.validation.model.ApiErrorMessage;
import com.ai.api.core.validation.model.ApiValidationError;
import com.ai.core.reporting.StepLogger;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class ResponseJsonAssertions {
    private ResponseJsonAssertions() {
    }

    static ApiErrorMessage errorMessage(Response response) {
        return response.as(ApiErrorMessage.class);
    }

    static List<ApiValidationError> validationErrors(Response response) {
        ApiValidationError[] errors = response.as(ApiValidationError[].class);
        return Arrays.asList(errors);
    }

    static List<?> rootArray(Response response) {
        return response.jsonPath().getList("$");
    }

    static void assertEveryFieldEquals(Response response, String jsonPath, Object expectedValue) {
        List<?> values = response.jsonPath().getList(jsonPath);
        Assert.assertNotNull(values, "Expected values for path: " + jsonPath);
        Assert.assertFalse(values.isEmpty(), "Expected at least one value for path: " + jsonPath);

        for (Object actual : values) {
            StepLogger.logDetail("actual=" + actual);
            Assert.assertTrue(
                    Objects.equals(actual, expectedValue),
                    "Expected every " + jsonPath + " to equal " + expectedValue + " but found " + actual
            );
        }
    }

    static void assertValidationErrorContains(
            Response response,
            String fieldName,
            String expectedMessageFragment
    ) {
        List<ApiValidationError> errors = validationErrors(response);
        Assert.assertNotNull(errors, "Expected validation errors array");
        Assert.assertFalse(errors.isEmpty(), "Expected at least one validation error");

        for (ApiValidationError error : errors) {
            String actualField = error.field;
            String actualMessage = error.message;
            StepLogger.logDetail("field=" + actualField + ", message=" + actualMessage);

            if (Objects.equals(actualField, fieldName)
                    && actualMessage != null
                    && actualMessage.contains(expectedMessageFragment)) {
                return;
            }
        }

        Assert.fail("Expected validation error for field " + fieldName
                + " containing message fragment: " + expectedMessageFragment);
    }
}
