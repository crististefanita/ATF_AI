package com.ai.core.reporting.listener.internal.runtime.model;



public final class ExtentRuntimeHtml {
    private ExtentRuntimeHtml() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String packageName(String className) {
        int lastDot = className == null ? -1 : className.lastIndexOf('.');
        return lastDot < 0 ? "" : className.substring(0, lastDot);
    }

    public static String simpleClassName(String className) {
        int lastDot = className == null ? -1 : className.lastIndexOf('.');
        return lastDot < 0 ? className : className.substring(lastDot + 1);
    }
}
