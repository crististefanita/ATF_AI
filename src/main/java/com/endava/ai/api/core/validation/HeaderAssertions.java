package com.endava.ai.api.core.validation;

import com.endava.ai.core.reporting.StepLogger;
import io.restassured.response.Response;
import org.testng.Assert;

final class HeaderAssertions {
    private HeaderAssertions() {
    }

    static void contentTypeContains(Response resp, String expectedFragment) {
        ResponseValidationStep.run("Validate: content-type~=" + expectedFragment, () -> {
            String actual = resp.getContentType();
            StepLogger.logDetail("actual=" + actual);
            Assert.assertTrue(
                    actual != null && actual.contains(expectedFragment),
                    "Expected content type to contain: " + expectedFragment
            );
        });
    }

    static void headerPresent(Response resp, String headerName) {
        ResponseValidationStep.run("Validate: header present=" + headerName, () -> {
            String actual = resp.getHeader(headerName);
            StepLogger.logDetail("actual=" + actual);
            Assert.assertNotNull(actual, "Expected header to be present: " + headerName);
            Assert.assertFalse(actual.isBlank(), "Expected header to be non-blank: " + headerName);
        });
    }

    static void headerEquals(Response resp, String headerName, String expectedValue) {
        ResponseValidationStep.run("Validate: header " + headerName + "=" + expectedValue, () -> {
            String actual = resp.getHeader(headerName);
            StepLogger.logDetail("actual=" + actual);
            Assert.assertEquals(actual, expectedValue, "Unexpected header value for: " + headerName);
        });
    }
}
