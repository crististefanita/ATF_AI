package com.ai.core.reporting;

public final class ReportText {
    public static final String NOT_AVAILABLE = "n/a";

    private ReportText() {
    }

    public static String printable(String value) {
        return value == null || value.isBlank() ? NOT_AVAILABLE : value;
    }

    public static String packageName(String className) {
        String value = valueOrEmpty(className);
        int lastDot = value.lastIndexOf('.');
        return lastDot < 0 ? "" : value.substring(0, lastDot);
    }

    public static String simpleClassName(String className) {
        String value = valueOrEmpty(className);
        int lastDot = value.lastIndexOf('.');
        return lastDot < 0 ? value : value.substring(lastDot + 1);
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
