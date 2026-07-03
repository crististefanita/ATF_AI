package com.endava.ai.core.reporting.adapters.extent.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public final class ExtentReportFiles {

    public static final String STABLE_REPORT_FILE_NAME = "ExtentReport.html";
    public static final String TIMESTAMP_FILE_PREFIX   = "ExtentReport_";
    public static final String REPORT_FILE_EXTENSION   = ".html";
    private static final Pattern SAFE_HTML_FILE_NAME    =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*\\.html");

    private ExtentReportFiles() {
    }

    public static String fileName(boolean timestampEnabled, String timestampFormat, Date timestamp) {
        if (!timestampEnabled) {
            return STABLE_REPORT_FILE_NAME;
        }
        return TIMESTAMP_FILE_PREFIX
                + new SimpleDateFormat(timestampFormat).format(timestamp)
                + REPORT_FILE_EXTENSION;
    }

    public static String configuredFileName(
            String explicitFileName,
            boolean timestampEnabled,
            String timestampFormat,
            Date timestamp
    ) {
        if (explicitFileName == null || explicitFileName.isBlank()) {
            return fileName(timestampEnabled, timestampFormat, timestamp);
        }
        String trimmed = explicitFileName.trim();
        if (!SAFE_HTML_FILE_NAME.matcher(trimmed).matches()
                || trimmed.contains("..")
                || trimmed.contains("/")
                || trimmed.contains("\\")) {
            throw new IllegalArgumentException("Invalid Extent report file name: " + explicitFileName);
        }
        return trimmed;
    }

    public static boolean isExtentHtmlReport(String fileName) {
        return STABLE_REPORT_FILE_NAME.equals(fileName)
                || (fileName != null
                && fileName.startsWith(TIMESTAMP_FILE_PREFIX)
                && fileName.endsWith(REPORT_FILE_EXTENSION));
    }
}
