package com.ai.skills.atlas.validation;

import java.util.Objects;

final class AtlasContractAssertions {
    private AtlasContractAssertions() {
    }

    static void notNull(Object value, String message) {
        if (value == null) {
            fail(message);
        }
    }

    static void equals(Object actual, Object expected, String message) {
        if (!Objects.equals(actual, expected)) {
            fail(message + " expected=[" + expected + "] actual=[" + actual + "]");
        }
    }

    static void isTrue(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    static void isFalse(boolean condition, String message) {
        if (condition) {
            fail(message);
        }
    }

    static void fail(String message) {
        throw new AssertionError(message);
    }
}
