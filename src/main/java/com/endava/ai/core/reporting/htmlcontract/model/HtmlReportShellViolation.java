package com.endava.ai.core.reporting.htmlcontract.model;

public final class HtmlReportShellViolation {
    private final String code;
    private final String message;

    private HtmlReportShellViolation(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static HtmlReportShellViolation of(String code, String message) {
        return new HtmlReportShellViolation(code, message);
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
