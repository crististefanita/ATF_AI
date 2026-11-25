package com.ai.api.core.validation;

import io.restassured.response.Response;
import org.testng.Assert;

final class BodyAssertions {
    private BodyAssertions() {
    }

    static void bodyContains(Response resp, String expectedText) {
        ResponseValidationStep.run("Validate: body~=" + expectedText, () ->
                Assert.assertTrue(
                        resp.getBody().asString().contains(expectedText),
                        "Expected response body to contain: " + expectedText
                )
        );
    }
}
