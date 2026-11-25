package com.ai.api.core.validation;

import com.ai.core.reporting.StepLogger;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;

final class JsonArrayAssertions {
    private JsonArrayAssertions() {
    }

    static void arrayNotEmpty(Response resp) {
        ResponseValidationStep.run("Validate: array not empty", () -> {
            List<?> items = ResponseJsonAssertions.rootArray(resp);
            int size = items == null ? 0 : items.size();
            StepLogger.logDetail("actual.size=" + size);
            Assert.assertTrue(items != null && !items.isEmpty(), "Expected response array to be non-empty");
        });
    }

    static void arraySizeAtMost(Response resp, int maxSize) {
        ResponseValidationStep.run("Validate: array size <= " + maxSize, () -> {
            List<?> items = ResponseJsonAssertions.rootArray(resp);
            int size = items == null ? 0 : items.size();
            StepLogger.logDetail("actual.size=" + size);
            Assert.assertTrue(
                    size <= maxSize,
                    "Expected response array size to be <= " + maxSize + " but was " + size
            );
        });
    }

    static void arraySizeEquals(Response resp, int expectedSize) {
        ResponseValidationStep.run("Validate: array size = " + expectedSize, () -> {
            List<?> items = ResponseJsonAssertions.rootArray(resp);
            int size = items == null ? 0 : items.size();
            StepLogger.logDetail("actual.size=" + size);
            Assert.assertEquals(size, expectedSize, "Unexpected response array size");
        });
    }
}
