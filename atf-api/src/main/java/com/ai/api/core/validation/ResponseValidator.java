package com.ai.api.core.validation;

import com.ai.api.core.validation.model.ApiErrorMessage;
import com.ai.api.core.validation.model.ApiValidationError;
import io.restassured.response.Response;

import java.util.List;
import java.util.function.Supplier;

public final class ResponseValidator {

    private ResponseValidator() {
    }

    public static void statusIs(Response resp, int expected) {
        StatusAssertions.statusIs(resp, expected);
    }

    public static void bodyContains(Response resp, String expectedText) {
        BodyAssertions.bodyContains(resp, expectedText);
    }

    public static void contentTypeContains(Response resp, String expectedFragment) {
        HeaderAssertions.contentTypeContains(resp, expectedFragment);
    }

    public static void headerPresent(Response resp, String headerName) {
        HeaderAssertions.headerPresent(resp, headerName);
    }

    public static void headerEquals(Response resp, String headerName, String expectedValue) {
        HeaderAssertions.headerEquals(resp, headerName, expectedValue);
    }

    public static void fieldEquals(Response resp, String jsonPath, Object expectedValue) {
        JsonFieldAssertions.fieldEquals(resp, jsonPath, expectedValue);
    }

    public static void fieldNotBlank(Response resp, String jsonPath) {
        JsonFieldAssertions.fieldNotBlank(resp, jsonPath);
    }

    public static void messageEquals(Response resp, String expectedMessage) {
        ErrorPayloadAssertions.messageEquals(resp, expectedMessage);
    }

    public static void arrayNotEmpty(Response resp) {
        JsonArrayAssertions.arrayNotEmpty(resp);
    }

    public static void arraySizeAtMost(Response resp, int maxSize) {
        JsonArrayAssertions.arraySizeAtMost(resp, maxSize);
    }

    public static void arraySizeEquals(Response resp, int expectedSize) {
        JsonArrayAssertions.arraySizeEquals(resp, expectedSize);
    }

    public static void everyFieldEquals(Response resp, String jsonPath, Object expectedValue) {
        JsonFieldAssertions.everyFieldEquals(resp, jsonPath, expectedValue);
    }

    public static void errorFieldContains(Response resp, String fieldName, String expectedMessageFragment) {
        ErrorPayloadAssertions.errorFieldContains(resp, fieldName, expectedMessageFragment);
    }

    public static ApiErrorMessage errorMessage(Response resp) {
        return ResponseJsonAssertions.errorMessage(resp);
    }

    public static List<ApiValidationError> validationErrors(Response resp) {
        return ResponseJsonAssertions.validationErrors(resp);
    }

    public static Response waitForStatus(Supplier<Response> call, int expected) {
        return PollingAssertions.waitForStatus(call, expected);
    }
}
